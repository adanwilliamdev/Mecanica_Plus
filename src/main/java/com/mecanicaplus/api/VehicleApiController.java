package com.mecanicaplus.api;

import com.mecanicaplus.dto.VehicleRequest;
import com.mecanicaplus.dto.VehicleResponse;
import com.mecanicaplus.security.Access;
import com.mecanicaplus.service.VehicleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Veículos")
@RestController
@RequestMapping("/api/vehicles")
public class VehicleApiController {

    private final VehicleService vehicles;

    public VehicleApiController(VehicleService vehicles) {
        this.vehicles = vehicles;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.VEHICLE_WRITE)
    public VehicleResponse create(@Valid @RequestBody VehicleRequest data) {
        return vehicles.create(data);
    }

    @GetMapping
    @PreAuthorize(Access.VEHICLE_READ)
    public List<VehicleResponse> list() {
        return vehicles.list();
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize(Access.VEHICLE_READ)
    public List<VehicleResponse> listByClient(@PathVariable UUID clientId) {
        return vehicles.listByClient(clientId);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Access.VEHICLE_READ)
    public VehicleResponse get(@PathVariable UUID id) {
        return vehicles.getVehicleResponse(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Access.VEHICLE_WRITE)
    public VehicleResponse update(@PathVariable UUID id, @Valid @RequestBody VehicleRequest data) {
        return vehicles.update(id, data);
    }

    @PatchMapping("/{id}/mileage")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.VEHICLE_MILEAGE)
    public void updateMileage(@PathVariable UUID id, @RequestParam @Min(0) int mileage) {
        vehicles.updateMileage(id, mileage);
    }

    /** Exclusão lógica: marca o veículo como inativo. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.VEHICLE_WRITE)
    public void delete(@PathVariable UUID id) {
        vehicles.deactivate(id);
    }
}
