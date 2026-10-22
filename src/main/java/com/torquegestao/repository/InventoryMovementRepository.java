package com.torquegestao.repository;

import com.torquegestao.domain.InventoryMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    @EntityGraph(attributePaths = "part")
    List<InventoryMovement> findByPartIdOrderByCreatedAtDesc(UUID partId);
}
