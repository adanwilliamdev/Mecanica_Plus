package com.torquegestao.dto;

import com.torquegestao.domain.InventoryMovement;
import com.torquegestao.domain.MovementType;
import java.time.Instant;
import java.util.UUID;

public record MovementResponse(
        UUID id,
        UUID partId,
        String partName,
        MovementType type,
        int quantity,
        int previousQuantity,
        int currentQuantity,
        String reason,
        String referenceId,
        UUID userId,
        Instant createdAt) {

    public static MovementResponse from(InventoryMovement m) {
        return new MovementResponse(
                m.getId(), m.getPart().getId(), m.getPart().getName(), m.getType(), m.getQuantity(),
                m.getPreviousQuantity(), m.getCurrentQuantity(), m.getReason(), m.getReferenceId(),
                m.getUserId(), m.getCreatedAt());
    }
}
