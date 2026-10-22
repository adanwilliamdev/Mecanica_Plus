package com.torquegestao.web;

import com.torquegestao.domain.MovementType;
import com.torquegestao.exception.AppException;
import com.torquegestao.security.Access;
import com.torquegestao.security.AppUserDetails;
import com.torquegestao.service.InventoryService;
import com.torquegestao.web.form.PartForm;
import com.torquegestao.web.form.StockForm;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventory")
@PreAuthorize(Access.STOCK)
public class InventoryWebController {

    private final InventoryService inventory;

    public InventoryWebController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("parts", inventory.list());
        return "inventory/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new PartForm());
        return formView(model, null);
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        model.addAttribute("form", PartForm.from(inventory.getPart(id)));
        return formView(model, id);
    }

    @PostMapping
    public String save(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @ModelAttribute("form") PartForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return formView(model, null);
        }
        try {
            inventory.create(form.toRequest(), user.getId());
        } catch (AppException | DataIntegrityViolationException ex) {
            errors.reject("business", ClientWebController.messageOf(ex));
            return formView(model, null);
        }
        flash.addFlashAttribute("success", "Peça criada com sucesso");
        return "redirect:/inventory";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("form") PartForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return formView(model, id);
        }
        try {
            inventory.update(id, form.toRequest());
        } catch (AppException | DataIntegrityViolationException ex) {
            errors.reject("business", ClientWebController.messageOf(ex));
            return formView(model, id);
        }
        flash.addFlashAttribute("success", "Peça atualizada com sucesso");
        return "redirect:/inventory";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes flash) {
        inventory.deactivate(id);
        flash.addFlashAttribute("success", "Peça excluída com sucesso");
        return "redirect:/inventory";
    }

    @GetMapping("/{id}/stock")
    public String stockForm(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "ENTRADA") MovementType kind,
            Model model) {
        model.addAttribute("form", new StockForm());
        return stockView(model, id, kind);
    }

    @PostMapping("/{id}/stock")
    public String stock(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "ENTRADA") MovementType kind,
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @ModelAttribute("form") StockForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return stockView(model, id, kind);
        }
        try {
            if (kind == MovementType.SAIDA) {
                inventory.removeStock(id, form.getQuantity(), form.getReason(), user.getId(), null);
            } else {
                inventory.addStock(id, form.getQuantity(), form.getReason(), user.getId(), null);
            }
        } catch (AppException | ObjectOptimisticLockingFailureException ex) {
            errors.reject("business", ex instanceof AppException
                    ? ex.getMessage()
                    : "O estoque foi alterado por outra operação. Tente novamente");
            return stockView(model, id, kind);
        }
        flash.addFlashAttribute("success", "Movimentação registrada com sucesso");
        return "redirect:/inventory";
    }

    private String formView(Model model, UUID id) {
        model.addAttribute("editingId", id);
        return "inventory/form";
    }

    private String stockView(Model model, UUID id, MovementType kind) {
        model.addAttribute("part", inventory.getPartResponse(id));
        model.addAttribute("kind", kind);
        return "inventory/stock";
    }
}
