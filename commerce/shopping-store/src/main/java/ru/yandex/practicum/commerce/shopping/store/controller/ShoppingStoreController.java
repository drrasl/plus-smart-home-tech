package ru.yandex.practicum.commerce.shopping.store.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.contract.shopping.store.ShoppingStoreOperations;
import ru.yandex.practicum.commerce.dto.shopping.store.ProductCategory;
import ru.yandex.practicum.commerce.dto.shopping.store.ProductDto;
import ru.yandex.practicum.commerce.dto.shopping.store.QuantityState;
import ru.yandex.practicum.commerce.shopping.store.model.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shopping.store.service.ShoppingStoreService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
@Validated
public class ShoppingStoreController implements ShoppingStoreOperations {
    private final ShoppingStoreService shoppingStoreService;

    @Override
    @GetMapping
    public Page<ProductDto> getProducts(
            @RequestParam @NotNull ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort) {

        log.debug("Requested products for category: {}, page: {}, size: {}, sort: {}",
                category, page, size, sort);
        Page<ProductDto> productDtos = shoppingStoreService.getProducts(category, page, size, sort);
        log.debug("Got products {} for category: {}", productDtos, category);
        return productDtos;
    }

    @Override
    @PutMapping
    public ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto) {
        log.debug("Received new product to create: {}", productDto);
        ProductDto newProduct = shoppingStoreService.createProduct(productDto);
        log.debug("Return created product : {}", newProduct);
        return newProduct;
    }

    @Override
    @PostMapping
    public ProductDto updateProduct(@RequestBody @Valid ProductDto productDto) {
        log.debug("Updating product with id: {}", productDto.getProductId());
        ProductDto updatedProduct = shoppingStoreService.updateProduct(productDto);
        log.debug("Return updated product : {}", updatedProduct);
        return updatedProduct;
    }

    @Override
    @PostMapping("/removeProductFromStore")
    public boolean removeProductFromStore(@RequestBody @NotNull UUID productId) {
        log.debug("Removing product from store with id: {}", productId);
        return shoppingStoreService.removeProductFromStore(productId);
    }

    @Override
    @PostMapping("/quantityState")
    public boolean setProductQuantityState(
            @RequestParam @NotNull UUID productId,
            @RequestParam @NotNull QuantityState quantityState) {
        log.debug("Setting quantity state for product: {}, state: {}", productId, quantityState);
        SetProductQuantityStateRequest request = SetProductQuantityStateRequest.builder()
                .productId(productId)
                .quantityState(quantityState)
                .build();
        return shoppingStoreService.setProductQuantityState(request);
    }

    @Override
    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable @NotNull UUID productId) {
        log.debug("Getting product with id: {}", productId);
        ProductDto productDto = shoppingStoreService.getProduct(productId);
        log.debug("Return product : {}", productDto);
        return productDto;
    }
}
