package com.mecanicaplus.web;

import com.mecanicaplus.dto.MechanicResponse;
import com.mecanicaplus.exception.AppException;
import com.mecanicaplus.security.Access;
import com.mecanicaplus.service.MechanicService;
import com.mecanicaplus.web.form.MechanicForm;
import jakarta.validation.Valid;
import java.util.UUID;
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
@RequestMapping("/mechanics")
public class MechanicWebController {

    private final MechanicService mechanics;

    public MechanicWebController(MechanicService mechanics) {
        this.mechanics = mechanics;
    }

    @GetMapping
    @PreAuthorize(Access.MECHANIC_READ)
    public String list(Model model) {
        model.addAttribute("mechanics", mechanics.list());
        return "mechanics/list";
    }

    @GetMapping("/new")
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public String create(Model model) {
        model.addAttribute("form", new MechanicForm());
        return formView(model, null);
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public String edit(@PathVariable UUID id, Model model) {
        model.addAttribute("form", MechanicForm.from(mechanics.getMechanic(id)));
        return formView(model, id);
    }

    @PostMapping
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public String save(
            @Valid @ModelAttribute("form") MechanicForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return formView(model, null);
        }
        try {
            mechanics.create(form.toRequest());
        } catch (AppException | DataIntegrityViolationException ex) {
            errors.reject("business", ClientWebController.messageOf(ex));
            return formView(model, null);
        }
        flash.addFlashAttribute("success", "Mecânico criado com sucesso");
        return "redirect:/mechanics";
    }

    @PostMapping("/{id}")
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("form") MechanicForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return formView(model, id);
        }
        try {
            mechanics.update(id, form.toRequest());
        } catch (AppException | DataIntegrityViolationException ex) {
            errors.reject("business", ClientWebController.messageOf(ex));
            return formView(model, id);
        }
        flash.addFlashAttribute("success", "Mecânico atualizado com sucesso");
        return "redirect:/mechanics";
    }

    @PostMapping("/{id}/availability")
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public String toggleAvailability(@PathVariable UUID id, RedirectAttributes flash) {
        MechanicResponse current = mechanics.getMechanicResponse(id);
        mechanics.setAvailability(id, !current.available());
        flash.addFlashAttribute("success", "Disponibilidade atualizada");
        return "redirect:/mechanics";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize(Access.MECHANIC_MANAGE)
    public String delete(@PathVariable UUID id, RedirectAttributes flash) {
        mechanics.deactivate(id);
        flash.addFlashAttribute("success", "Mecânico excluído com sucesso");
        return "redirect:/mechanics";
    }

    private static String formView(Model model, UUID id) {
        model.addAttribute("editingId", id);
        return "mechanics/form";
    }
}
