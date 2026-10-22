package com.mecanicaplus.domain;

public enum Role {
    ADMIN("Administrador"),
    RECEPTIONIST("Recepcionista"),
    MECHANIC("Mecânico"),
    MANAGER("Gerente");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
