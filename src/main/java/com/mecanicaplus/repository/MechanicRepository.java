package com.mecanicaplus.repository;

import com.mecanicaplus.domain.Mechanic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MechanicRepository extends JpaRepository<Mechanic, UUID> {

    List<Mechanic> findByActiveTrueOrderByCreatedAtDesc();

    List<Mechanic> findByAvailableTrueAndActiveTrue();

    Optional<Mechanic> findByUserId(UUID userId);
}
