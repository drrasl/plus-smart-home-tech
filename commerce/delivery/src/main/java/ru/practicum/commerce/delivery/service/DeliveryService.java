package ru.practicum.commerce.delivery.service;

import ru.yandex.practicum.commerce.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {

    // Создать доставку
    DeliveryDto createDelivery(DeliveryDto deliveryDto);

    // Рассчитать стоимость доставки
    BigDecimal calculateDeliveryCost(OrderDto orderDto);

    // Обработать получение товара в доставку
    void processDeliveryPicked(UUID orderId);

    // Обработать успешную доставку
    void processDeliverySuccess(UUID orderId);

    // Обработать неудачную доставку
    void processDeliveryFailed(UUID orderId);

    // Получить доставку по ID
    DeliveryDto getDeliveryById(UUID deliveryId);

    // Получить доставку по ID заказа
    DeliveryDto getDeliveryByOrderId(UUID orderId);
}
