package com.mecanicaplus.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "service_orders")
public class ServiceOrder extends AuditedEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mechanic_id")
    private Mechanic mechanic;

    @Column(name = "reported_problem")
    private String reportedProblem;

    private String diagnosis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderStatus status = ServiceOrderStatus.CRIADA;

    /** Mão de obra lançada manualmente; somada às peças compõe o total. */
    @Column(name = "labor_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal laborAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ServiceOrderItem> items = new ArrayList<>();

    public BigDecimal getPartsAmount() {
        return items.stream()
                .map(ServiceOrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void recalculateTotal() {
        totalAmount = laborAmount.add(getPartsAmount());
    }
}
