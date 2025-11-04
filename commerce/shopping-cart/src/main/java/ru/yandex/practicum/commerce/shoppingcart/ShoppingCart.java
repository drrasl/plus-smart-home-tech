package ru.yandex.practicum.commerce.shoppingcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ShoppingCart {
    public static void main(String[] args) {
        SpringApplication.run(ShoppingCart.class, args);
    }
}
