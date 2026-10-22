package com.torquegestao.web.form;

import com.torquegestao.domain.Vehicle;
import com.torquegestao.dto.VehicleRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleForm {

    @NotNull(message = "Selecione um cliente")
    private UUID clientId;
    @NotBlank(message = "Placa é obrigatória")
    private String plate;
    @NotBlank(message = "Marca é obrigatória")
    private String brand;
    @NotBlank(message = "Modelo é obrigatório")
    private String model;
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano inválido")
    private Integer year;
    @Min(value = 0, message = "Quilometragem não pode ser negativa")
    private Integer mileage;
    private String fuelType;

    public static VehicleForm from(Vehicle v) {
        VehicleForm f = new VehicleForm();
        f.clientId = v.getClient().getId();
        f.plate = v.getPlate();
        f.brand = v.getBrand();
        f.model = v.getModel();
        f.year = v.getYear();
        f.mileage = v.getMileage();
        f.fuelType = v.getFuelType();
        return f;
    }

    public VehicleRequest toRequest() {
        return new VehicleRequest(plate, brand, model, year, mileage, fuelType, clientId);
    }
}
