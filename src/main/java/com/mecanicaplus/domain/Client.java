package com.mecanicaplus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "clients")
public class Client extends AuditedEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String cpf;

    private String phone;

    private String email;

    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
