package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.contract.warehouse.WarehouseClient;
import ru.yandex.practicum.commerce.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.order.OrderState;
import ru.yandex.practicum.commerce.dto.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.order.dal.OrderItemRepository;
import ru.yandex.practicum.commerce.order.dal.OrderRepository;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundBusinessException;
import ru.yandex.practicum.commerce.order.exception.NotAuthorizedBusinessException;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.model.OrderItemEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WarehouseClient warehouseClient;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        validateUsername(username);
        log.info("Getting orders for user: {}", username);
        List<OrderEntity> orders = orderRepository.findByUsernameOrderByCreatedAtDesc(username);

        return orders.stream()
                .map(order -> {
                    List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
                    return OrderMapper.toDto(order, items);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(String username, CreateNewOrderRequest request) {
        log.info("Creating new order from shopping cart: {}", request.getShoppingCart().getShoppingCartId());
        validateUsername(username);
        //Проверка на складе уже была при наполнении корзины, но возможно она долго собиралась, проверим еще раз.
        // Создаем временную корзину для проверки на складе
        ShoppingCartDto tempCart = ShoppingCartDto.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(new HashMap<>(request.getShoppingCart().getProducts()))
                .build();

        // Проверяем доступность товаров на складе через Feign клиент
        try {
            warehouseClient.checkProductQuantityEnoughForShoppingCart(tempCart);
            log.debug("Products availability confirmed by warehouse");
        } catch (Exception e) {
            log.error("Failed to check product availability in warehouse: {}", e.getMessage());
            throw new RuntimeException("Product availability check failed: " + e.getMessage(), e);
        }

        OrderEntity order = OrderEntity.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                //paymentId, deliveryId еще не создавались -> дозаполним после нужных эндпоинтов
                .username(username)
                .orderState(OrderState.NEW)
                //deliveryWeight, deliveryVolume, fragile еще не создавались -> дозаполним после нужных эндпоинтов
                .country(request.getDeliveryAddress().getCountry())
                .city(request.getDeliveryAddress().getCity())
                .street(request.getDeliveryAddress().getStreet())
                .house(request.getDeliveryAddress().getHouse())
                .flat(request.getDeliveryAddress().getFlat())
                .fragile(false) // По дефолту делаем FALSE
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        // Save order items
        List<OrderItemEntity> orderItems = OrderMapper.toOrderItemEntities(
                savedOrder,
                request.getShoppingCart().getProducts()
        );
        orderItemRepository.saveAll(orderItems);

        OrderDto result = OrderMapper.toDto(savedOrder, orderItems);
        log.info("Created new order with id: {}", result.getOrderId());

        return result;
    }

    @Override
    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Processing product return for order: {}", request.getOrderId());

        OrderEntity order = getOrderEntity(request.getOrderId());
        order.setOrderState(OrderState.PRODUCT_RETURNED);

        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(updatedOrder.getOrderId());

        // Возвращаем товары обратно на склад.
        // Используем существующий метод addProductToWarehouse для каждого товара через Feign клиент
        // TODO: Проверь, возможно будет новый метод на складе - возврат товара!
        try {
            for (Map.Entry<UUID, Integer> productEntry : request.getProducts().entrySet()) {
                UUID productId = productEntry.getKey();
                Integer quantity = productEntry.getValue();

                AddProductToWarehouseRequest addRequest = AddProductToWarehouseRequest.builder()
                        .productId(productId)
                        .quantity(quantity.longValue())
                        .build();

                warehouseClient.addProductToWarehouse(addRequest);
                log.debug("Product {} returned to warehouse with quantity: {}", productId, quantity);
            }
            log.debug("All products successfully returned to warehouse");
        } catch (Exception e) {
            log.error("Failed to return products to warehouse: {}", e.getMessage());
            throw new RuntimeException("Product return to warehouse failed: " + e.getMessage(), e);
        }
        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        log.info("Processing payment for order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        order.setOrderState(OrderState.PAID);

        // TODO: Integrate with payment service
        // UUID paymentId = paymentService.processPayment(orderId);
        // order.setPaymentId(paymentId);

        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(updatedOrder.getOrderId());

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Processing payment failure for order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        order.setOrderState(OrderState.PAYMENT_FAILED);

        // TODO: Integrate with payment service ????

        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(updatedOrder.getOrderId());

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto delivery(UUID orderId) {
        log.info("Processing delivery for order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        order.setOrderState(OrderState.DELIVERED);

        // TODO: Integrate with delivery service ????

        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(updatedOrder.getOrderId());

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Processing delivery failure for order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        order.setOrderState(OrderState.DELIVERY_FAILED);

        // TODO: Integrate with delivery service ????

        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(updatedOrder.getOrderId());

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto complete(UUID orderId) {
        log.info("Completing order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        order.setOrderState(OrderState.COMPLETED);

        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(updatedOrder.getOrderId());

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Calculating total cost for order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(orderId);

        // TODO: Integrate with payment service for total cost calculation
        // BigDecimal totalCost = paymentService.calculateTotalCost(orderId);

        BigDecimal totalCost = BigDecimal.valueOf(0); // TODO: Placeholder

        order.setTotalPrice(totalCost);
        OrderEntity updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Calculating delivery cost for order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(orderId);

        // TODO: Integrate with delivery service for cost calculation
        // BigDecimal deliveryCost = deliveryService.calculateDeliveryCost(orderId);

        BigDecimal deliveryCost = BigDecimal.valueOf(0); // TODO: Placeholder

        order.setDeliveryPrice(deliveryCost);
        OrderEntity updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto assembly(UUID orderId) {
        log.info("Processing assembly for order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        order.setOrderState(OrderState.ASSEMBLED);

        // TODO: Integrate with warehouse service for assembly
        // TODO: Здесь мы добавим метод сборки на складе скорее всего потом

        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(updatedOrder.getOrderId());

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Processing assembly failure for order: {}", orderId);

        OrderEntity order = getOrderEntity(orderId);
        order.setOrderState(OrderState.ASSEMBLY_FAILED);

        // TODO: Integrate with warehouse service for assembly ?????

        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(updatedOrder.getOrderId());

        return OrderMapper.toDto(updatedOrder, items);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID orderId) {
        OrderEntity order = getOrderEntity(orderId);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(orderId);
        return OrderMapper.toDto(order, items);
    }

    @Override
    @Transactional
    public OrderDto updateOrderState(UUID orderId, String username, OrderState newState) {
        validateUsername(username);

        OrderEntity order = orderRepository.findByOrderIdAndUsername(orderId, username)
                .orElseThrow(() -> new NoOrderFoundBusinessException(orderId,
                        "Order not found for user: " + username));

        order.setOrderState(newState);
        OrderEntity updatedOrder = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderOrderId(orderId);

        return OrderMapper.toDto(updatedOrder, items);
    }

    private OrderEntity getOrderEntity(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundBusinessException(orderId));
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedBusinessException("Username cannot be empty");
        }
    }
}
