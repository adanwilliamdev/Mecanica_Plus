package com.mecanicaplus.domain;

public enum MovementType {
    ENTRADA("Entrada"),
    SAIDA("Saída"),
    AJUSTE("Ajuste");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
