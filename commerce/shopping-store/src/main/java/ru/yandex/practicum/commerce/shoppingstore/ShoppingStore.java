package ru.yandex.practicum.commerce.shoppingstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ShoppingStore {
    public static void main(String[] args) {
        SpringApplication.run(ShoppingStore.class, args);
    }
}
