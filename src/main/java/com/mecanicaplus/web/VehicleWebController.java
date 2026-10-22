package com.mecanicaplus.web;

import com.mecanicaplus.dto.VehicleRequest;
import com.mecanicaplus.exception.AppException;
import com.mecanicaplus.security.Access;
import com.mecanicaplus.service.ClientService;
import com.mecanicaplus.service.VehicleService;
import com.mecanicaplus.web.form.VehicleForm;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehicles")
public class VehicleWebController {

    private static final Pattern PLATE = Pattern.compile(VehicleRequest.PLATE_PATTERN);

    private final VehicleService vehicles;
    private final ClientService clients;

    public VehicleWebController(VehicleService vehicles, ClientService clients) {
        this.vehicles = vehicles;
        this.clients = clients;
    }

    @GetMapping
    @PreAuthorize(Access.VEHICLE_READ)
    public String list(Model model) {
        model.addAttribute("vehicles", vehicles.list());
        return "vehicles/list";
    }

    @GetMapping("/new")
    @PreAuthorize(Access.VEHICLE_WRITE)
    public String create(Model model) {
        model.addAttribute("form", new VehicleForm());
        return formView(model, null);
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize(Access.VEHICLE_WRITE)
    public String edit(@PathVariable UUID id, Model model) {
        model.addAttribute("form", VehicleForm.from(vehicles.getVehicle(id)));
        return formView(model, id);
    }

    @PostMapping
    @PreAuthorize(Access.VEHICLE_WRITE)
    public String save(
            @Valid @ModelAttribute("form") VehicleForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (invalid(form, errors)) {
            return formView(model, null);
        }
        try {
            vehicles.create(form.toRequest());
        } catch (AppException | DataIntegrityViolationException ex) {
            errors.reject("business", ClientWebController.messageOf(ex));
            return formView(model, null);
        }
        flash.addFlashAttribute("success", "Veículo criado com sucesso");
        return "redirect:/vehicles";
    }

    @PostMapping("/{id}")
    @PreAuthorize(Access.VEHICLE_WRITE)
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("form") VehicleForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (invalid(form, errors)) {
            return formView(model, id);
        }
        try {
            vehicles.update(id, form.toRequest());
        } catch (AppException | DataIntegrityViolationException ex) {
            errors.reject("business", ClientWebController.messageOf(ex));
            return formView(model, id);
        }
        flash.addFlashAttribute("success", "Veículo atualizado com sucesso");
        return "redirect:/vehicles";
    }

    /** Valida a placa já normalizada (maiúsculas, sem hífen) além das regras do formulário. */
    private static boolean invalid(VehicleForm form, BindingResult errors) {
        if (form.getPlate() != null && !form.getPlate().isBlank()
                && !PLATE.matcher(VehicleRequest.normalizePlate(form.getPlate())).matches()) {
            errors.rejectValue("plate", "invalid", "Placa inválida (use ABC1D23 ou ABC1234)");
        }
        return errors.hasErrors();
    }

    private String formView(Model model, UUID id) {
        model.addAttribute("editingId", id);
        model.addAttribute("clients", clients.list());
        return "vehicles/form";
    }
}
