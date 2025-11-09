package ru.yandex.practicum.commerce.shopping.cart.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import ru.yandex.practicum.commerce.shopping.cart.exception.InternalServerErrorFromWarehouseException;
import ru.yandex.practicum.commerce.shopping.cart.exception.ProductNotFoundInWarehouseException;

public class CustomErrorDecoder implements ErrorDecoder {

    // используем стандартный декодер для всех кодов, которые не обработаем явно
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // обработка ошибки 404 (Not Found)
         if (response.status() == 404) {
            return new ProductNotFoundInWarehouseException("Resource not found for method: " + methodKey);
        }

        // обработка ошибки 500 (Internal Server Error)
        if (response.status() == 500) {
            return new InternalServerErrorFromWarehouseException("Server error occurred");
        }

        // для других кодов используем стандартное поведение
        return defaultDecoder.decode(methodKey, response);
    }
}
