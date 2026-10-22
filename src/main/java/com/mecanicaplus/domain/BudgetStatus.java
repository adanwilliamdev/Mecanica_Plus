package com.mecanicaplus.domain;

public enum BudgetStatus {
    PENDENTE("Pendente"),
    APROVADO("Aprovado"),
    RECUSADO("Recusado"),
    EXPIRADO("Expirado");

    private final String label;

    BudgetStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
