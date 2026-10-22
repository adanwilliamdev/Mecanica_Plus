package com.mecanicaplus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "parts")
public class Part extends AuditedEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String manufacturer;

    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "sale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "minimum_stock", nullable = false)
    private int minimumStock = 5;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Lock otimista: movimentações concorrentes de estoque resultam em conflito (409). */
    @Version
    @Column(nullable = false)
    private int version;

    public boolean isLowStock() {
        return stockQuantity <= minimumStock;
    }
}
