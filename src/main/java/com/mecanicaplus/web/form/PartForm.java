package com.mecanicaplus.web.form;

import com.mecanicaplus.domain.Part;
import com.mecanicaplus.dto.PartRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartForm {

    @NotBlank(message = "Nome é obrigatório")
    private String name;
    @NotBlank(message = "Código é obrigatório")
    private String code;
    private String manufacturer;
    @NotNull(message = "Preço de compra é obrigatório")
    @DecimalMin(value = "0", inclusive = false, message = "Preço de compra deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Preço de compra inválido")
    private BigDecimal purchasePrice;
    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0", inclusive = false, message = "Preço de venda deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Preço de venda inválido")
    private BigDecimal salePrice;
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 0, message = "Estoque não pode ser negativo")
    private Integer stockQuantity;
    @NotNull(message = "Estoque mínimo é obrigatório")
    @Min(value = 1, message = "Estoque mínimo deve ser maior que zero")
    private Integer minimumStock = PartRequest.DEFAULT_MINIMUM_STOCK;

    public static PartForm from(Part p) {
        PartForm f = new PartForm();
        f.name = p.getName();
        f.code = p.getCode();
        f.manufacturer = p.getManufacturer();
        f.purchasePrice = p.getPurchasePrice();
        f.salePrice = p.getSalePrice();
        f.stockQuantity = p.getStockQuantity();
        f.minimumStock = p.getMinimumStock();
        return f;
    }

    public PartRequest toRequest() {
        return new PartRequest(name, code, manufacturer, purchasePrice, salePrice, stockQuantity, minimumStock);
    }
}
