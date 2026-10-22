package com.mecanicaplus.web.form;

import com.mecanicaplus.dto.ServiceOrderItemRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemForm {

    @NotNull(message = "Selecione uma peça")
    private UUID partId;
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantity = 1;

    public ServiceOrderItemRequest toRequest() {
        return new ServiceOrderItemRequest(partId, quantity);
    }
}
