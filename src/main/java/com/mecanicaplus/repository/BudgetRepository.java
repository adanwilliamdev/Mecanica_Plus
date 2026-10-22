package com.mecanicaplus.repository;

import com.mecanicaplus.domain.Budget;
import com.mecanicaplus.domain.BudgetStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    @EntityGraph(attributePaths = {"client", "vehicle"})
    Optional<Budget> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"client", "vehicle"})
    List<Budget> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"client", "vehicle"})
    List<Budget> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    @EntityGraph(attributePaths = {"client", "vehicle"})
    List<Budget> findByStatusOrderByCreatedAtDesc(BudgetStatus status);

    @Query(value = "select nextval('budget_number_seq')", nativeQuery = true)
    long nextBudgetSequence();
}
