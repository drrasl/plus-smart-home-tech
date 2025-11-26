package ru.yandex.practicum.commerce.payment.service;

import ru.yandex.practicum.commerce.dto.order.OrderDto;
import ru.yandex.practicum.commerce.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    // Рассчитать стоимость товаров в заказе
    BigDecimal calculateProductCost(OrderDto orderDto);

    // Расчёт полной стоимости заказа
    BigDecimal calculateTotalCost(OrderDto orderDto);

    // Формирование оплаты для заказа (переход в платежный шлюз)
    PaymentDto createPayment(OrderDto orderDto);

    // Метод для эмуляции успешной оплаты в платежного шлюза
    void processPaymentSuccess(UUID paymentId);

    // Метод для эмуляции отказа в оплате платежного шлюза
    void processPaymentFailed(UUID paymentId);

    // Получить платеж по ID
    PaymentDto getPaymentById(UUID paymentId);

    // Получить платеж по ID заказа
    PaymentDto getPaymentByOrderId(UUID orderId);
}
