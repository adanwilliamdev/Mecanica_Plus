package com.mecanicaplus.repository;

import com.mecanicaplus.domain.Part;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PartRepository extends JpaRepository<Part, UUID> {

    List<Part> findByActiveTrueOrderByCreatedAtDesc();

    boolean existsByCode(String code);

    @Query("select p from Part p where p.active = true and p.stockQuantity <= p.minimumStock order by p.name")
    List<Part> findLowStock();

    @Query("select count(p) from Part p where p.active = true and p.stockQuantity <= p.minimumStock")
    long countLowStock();
}
