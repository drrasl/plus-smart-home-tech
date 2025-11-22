package ru.yandex.practicum.commerce.contract.warehouse;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.dto.warehouse.NewProductInWarehouseRequest;

@Component
public class WarehouseClientFallback  implements WarehouseClient {
    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        throw new RuntimeException("Warehouse service is unavailable");
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        throw new RuntimeException("Warehouse service is unavailable");
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        throw new RuntimeException("Warehouse service is unavailable");
    }

    @Override
    public AddressDto getWarehouseAddress() {
        throw new RuntimeException("Warehouse service is unavailable");
    }
}
