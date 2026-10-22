package com.mecanicaplus.web.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockForm {

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantity;

    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    private String reason;
}
