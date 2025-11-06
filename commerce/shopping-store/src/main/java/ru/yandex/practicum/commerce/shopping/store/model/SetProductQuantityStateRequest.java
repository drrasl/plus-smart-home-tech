package ru.yandex.practicum.commerce.shopping.store.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.dto.shopping.store.QuantityState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetProductQuantityStateRequest {

    @NotBlank
    private UUID productId;
    @NotNull
    private QuantityState quantityState;
}
