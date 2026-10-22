package com.torquegestao.web.form;

import com.torquegestao.dto.BudgetRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class BudgetForm {

    @NotNull(message = "Selecione um cliente")
    private UUID clientId;
    @NotNull(message = "Selecione um veículo")
    private UUID vehicleId;
    private String description;
    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0", inclusive = false, message = "Valor total deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Valor total inválido")
    private BigDecimal totalAmount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntil;

    /** A validade vale até o fim do dia escolhido (no fuso da oficina). */
    public BudgetRequest toRequest(ZoneId zone) {
        var until = validUntil == null
                ? null
                : validUntil.plusDays(1).atStartOfDay(zone).toInstant().minusSeconds(1);
        return new BudgetRequest(clientId, vehicleId, null, description, totalAmount, until);
    }
}
