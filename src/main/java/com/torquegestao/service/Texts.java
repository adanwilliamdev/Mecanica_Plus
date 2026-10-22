package com.torquegestao.service;

/** Utilidades de texto compartilhadas pelos serviços. */
final class Texts {

    private Texts() {
    }

    /** Converte texto vazio/em branco em {@code null} (evita violar colunas únicas com ""). */
    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
