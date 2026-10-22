package com.mecanicaplus.web.form;

import com.mecanicaplus.dto.ServiceOrderRequest;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceOrderForm {

    @NotNull(message = "Selecione um cliente")
    private UUID clientId;
    @NotNull(message = "Selecione um veículo")
    private UUID vehicleId;
    private UUID mechanicId;
    private String reportedProblem;

    public ServiceOrderRequest toRequest() {
        return new ServiceOrderRequest(clientId, vehicleId, mechanicId, reportedProblem);
    }
}
