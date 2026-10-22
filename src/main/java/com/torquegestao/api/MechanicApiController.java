package com.torquegestao.api;

import com.torquegestao.dto.MechanicRequest;
import com.torquegestao.dto.MechanicResponse;
import com.torquegestao.security.Access;
import com.torquegestao.service.MechanicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "Mecânicos")
@RestController
@RequestMapping("/api/mechanics")
public class MechanicApiController {

    private final MechanicService mechanics;

    public MechanicApiController(MechanicService mechanics) {
        this.mechanics = mechanics;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public MechanicResponse create(@Valid @RequestBody MechanicRequest data) {
        return mechanics.create(data);
    }

    @GetMapping
    @PreAuthorize(Access.MECHANIC_READ)
    public List<MechanicResponse> list() {
        return mechanics.list();
    }

    @GetMapping("/available")
    @PreAuthorize(Access.MECHANIC_READ)
    public List<MechanicResponse> listAvailable() {
        return mechanics.listAvailable();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Access.MECHANIC_READ)
    public MechanicResponse get(@PathVariable UUID id) {
        return mechanics.getMechanicResponse(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public MechanicResponse update(@PathVariable UUID id, @Valid @RequestBody MechanicRequest data) {
        return mechanics.update(id, data);
    }

    @PatchMapping("/{id}/availability")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public void setAvailability(@PathVariable UUID id, @RequestParam boolean available) {
        mechanics.setAvailability(id, available);
    }

    /** Exclusão lógica: marca o mecânico como inativo. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public void delete(@PathVariable UUID id) {
        mechanics.deactivate(id);
    }
}
