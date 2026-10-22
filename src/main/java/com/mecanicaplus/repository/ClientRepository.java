package com.mecanicaplus.repository;

import com.mecanicaplus.domain.Client;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findByActiveTrueOrderByCreatedAtDesc();

    @Query("select c from Client c where lower(c.name) like lower(concat('%', :name, '%')) order by c.name")
    List<Client> searchByName(@Param("name") String name);
}
