package com.torquegestao.web;

import com.torquegestao.exception.AppException;
import com.torquegestao.security.Access;
import com.torquegestao.service.ClientService;
import com.torquegestao.web.form.ClientForm;
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
@RequestMapping("/clients")
public class ClientWebController {

    private final ClientService clients;

    public ClientWebController(ClientService clients) {
        this.clients = clients;
    }

    @GetMapping
    @PreAuthorize(Access.CLIENT_READ)
    public String list(Model model) {
        model.addAttribute("clients", clients.list());
        return "clients/list";
    }

    @GetMapping("/new")
    @PreAuthorize(Access.CLIENT_WRITE)
    public String create(Model model) {
        model.addAttribute("form", new ClientForm());
        return formView(model, null);
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize(Access.CLIENT_WRITE)
    public String edit(@PathVariable UUID id, Model model) {
        model.addAttribute("form", ClientForm.from(clients.getClient(id)));
        return formView(model, id);
    }

    @PostMapping
    @PreAuthorize(Access.CLIENT_WRITE)
    public String save(
            @Valid @ModelAttribute("form") ClientForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return formView(model, null);
        }
        try {
            clients.create(form.toRequest());
        } catch (AppException | DataIntegrityViolationException ex) {
            errors.reject("business", messageOf(ex));
            return formView(model, null);
        }
        flash.addFlashAttribute("success", "Cliente criado com sucesso");
        return "redirect:/clients";
    }

    @PostMapping("/{id}")
    @PreAuthorize(Access.CLIENT_WRITE)
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("form") ClientForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return formView(model, id);
        }
        try {
            clients.update(id, form.toRequest());
        } catch (AppException | DataIntegrityViolationException ex) {
            errors.reject("business", messageOf(ex));
            return formView(model, id);
        }
        flash.addFlashAttribute("success", "Cliente atualizado com sucesso");
        return "redirect:/clients";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize(Access.CLIENT_WRITE)
    public String delete(@PathVariable UUID id, RedirectAttributes flash) {
        clients.setActive(id, false);
        flash.addFlashAttribute("success", "Cliente excluído com sucesso");
        return "redirect:/clients";
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize(Access.CLIENT_WRITE)
    public String activate(@PathVariable UUID id, RedirectAttributes flash) {
        clients.setActive(id, true);
        flash.addFlashAttribute("success", "Cliente reativado com sucesso");
        return "redirect:/clients";
    }

    private static String formView(Model model, UUID id) {
        model.addAttribute("editingId", id);
        return "clients/form";
    }

    static String messageOf(RuntimeException ex) {
        return ex instanceof AppException ? ex.getMessage() : WebAdvice.INTEGRITY_MESSAGE;
    }
}
