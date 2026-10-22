package com.torquegestao.web.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaborForm {

    @NotNull(message = "Valor da mão de obra é obrigatório")
    @DecimalMin(value = "0", message = "Mão de obra não pode ser negativa")
    @Digits(integer = 10, fraction = 2, message = "Valor inválido")
    private BigDecimal laborAmount;
}
