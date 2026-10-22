package com.mecanicaplus.api;

import com.mecanicaplus.dto.MovementResponse;
import com.mecanicaplus.dto.PartRequest;
import com.mecanicaplus.dto.PartResponse;
import com.mecanicaplus.security.Access;
import com.mecanicaplus.security.AppUserDetails;
import com.mecanicaplus.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Estoque é restrito a ADMIN e MANAGER. */
@Tag(name = "Estoque")
@RestController
@RequestMapping("/api/inventory")
@PreAuthorize(Access.STOCK)
public class InventoryApiController {

    private final InventoryService inventory;

    public InventoryApiController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @PostMapping("/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public PartResponse create(
            @Valid @RequestBody PartRequest data, @AuthenticationPrincipal AppUserDetails user) {
        return inventory.create(data, user.getId());
    }

    @GetMapping("/parts")
    public List<PartResponse> list() {
        return inventory.list();
    }

    @GetMapping("/parts/low-stock")
    public List<PartResponse> listLowStock() {
        return inventory.listLowStock();
    }

    @GetMapping("/parts/{id}")
    public PartResponse get(@PathVariable UUID id) {
        return inventory.getPartResponse(id);
    }

    @PutMapping("/parts/{id}")
    public PartResponse update(@PathVariable UUID id, @Valid @RequestBody PartRequest data) {
        return inventory.update(id, data);
    }

    /** Exclusão lógica: marca a peça como inativa. */
    @DeleteMapping("/parts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        inventory.deactivate(id);
    }

    /** O usuário de auditoria vem sempre do JWT, nunca de um parâmetro do cliente. */
    @PostMapping("/parts/{id}/add-stock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addStock(
            @PathVariable UUID id,
            @RequestParam @Min(1) int quantity,
            @RequestParam(required = false) @Size(max = 255) String reason,
            @AuthenticationPrincipal AppUserDetails user) {
        inventory.addStock(id, quantity, reason, user.getId(), null);
    }

    @PostMapping("/parts/{id}/remove-stock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStock(
            @PathVariable UUID id,
            @RequestParam @Min(1) int quantity,
            @RequestParam(required = false) @Size(max = 255) String reason,
            @AuthenticationPrincipal AppUserDetails user) {
        inventory.removeStock(id, quantity, reason, user.getId(), null);
    }

    @GetMapping("/parts/{id}/movements")
    public List<MovementResponse> movements(@PathVariable UUID id) {
        return inventory.listMovements(id);
    }
}
