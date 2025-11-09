package ru.yandex.practicum.commerce.contract.shopping.store;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "shopping-store-service")
public interface ShoppingStoreClient extends ShoppingStoreOperations {
}
