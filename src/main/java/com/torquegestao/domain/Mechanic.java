package com.torquegestao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mechanics")
public class Mechanic extends AuditedEntity {

    /** Usuário de login vinculado a este mecânico (opcional). */
    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    private String specialty;

    private String phone;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
