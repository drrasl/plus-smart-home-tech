package ru.yandex.practicum.commerce.contract.order;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "order", fallback = OrderClientFallback.class)
public interface OrderClient extends OrderOperations {
}
