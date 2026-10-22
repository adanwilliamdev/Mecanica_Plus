package com.mecanicaplus.dto;

import com.mecanicaplus.domain.ServiceOrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServiceOrderItemResponse(
        UUID id,
        UUID partId,
        String partName,
        String partCode,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        Instant createdAt) {

    public static ServiceOrderItemResponse from(ServiceOrderItem i) {
        return new ServiceOrderItemResponse(
                i.getId(), i.getPart().getId(), i.getPart().getName(), i.getPart().getCode(),
                i.getQuantity(), i.getUnitPrice(), i.getSubtotal(), i.getCreatedAt());
    }
}
