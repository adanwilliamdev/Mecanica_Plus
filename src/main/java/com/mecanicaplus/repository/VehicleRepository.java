package com.mecanicaplus.repository;

import com.mecanicaplus.domain.Vehicle;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    @EntityGraph(attributePaths = "client")
    List<Vehicle> findByActiveTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "client")
    List<Vehicle> findByClientIdAndActiveTrueOrderByCreatedAtDesc(UUID clientId);

    @EntityGraph(attributePaths = "client")
    java.util.Optional<Vehicle> findWithClientById(UUID id);

    boolean existsByPlate(String plate);

    long countByClientId(UUID clientId);

    /** Total de veículos por cliente (inclui os inativos). */
    @Query("select v.client.id, count(v) from Vehicle v group by v.client.id")
    List<Object[]> countGroupedByClient();

    default java.util.Map<UUID, Integer> vehicleCountsByClient() {
        java.util.Map<UUID, Integer> result = new java.util.HashMap<>();
        for (Object[] row : countGroupedByClient()) {
            result.put((UUID) row[0], ((Number) row[1]).intValue());
        }
        return result;
    }
}
