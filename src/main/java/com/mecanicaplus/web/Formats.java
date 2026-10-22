package com.mecanicaplus.web;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatação de valores para exibição (pt-BR). Funções puras, sem dependência de Spring. */
public final class Formats {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PT_BR);

    private Formats() {
    }

    public static String currency(BigDecimal value) {
        // NumberFormat não é thread-safe: uma instância por chamada.
        return NumberFormat.getCurrencyInstance(PT_BR).format(value == null ? BigDecimal.ZERO : value);
    }

    public static String date(Instant instant, ZoneId zone) {
        return instant == null ? "—" : DATE.format(instant.atZone(zone));
    }

    public static String dateTime(Instant instant, ZoneId zone) {
        return instant == null ? "—" : DATE_TIME.format(instant.atZone(zone));
    }

    /** Formata só quando o valor tem exatamente os dígitos esperados; senão devolve como veio. */
    public static String cpf(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value.replaceFirst("^(\\d{3})(\\d{3})(\\d{3})(\\d{2})$", "$1.$2.$3-$4");
    }

    public static String phone(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value.replaceFirst("^(\\d{2})(\\d{5})(\\d{4})$", "($1) $2-$3");
    }

    public static String kilometers(Integer value) {
        return value == null ? "—" : NumberFormat.getIntegerInstance(PT_BR).format(value) + " km";
    }

    public static String orDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
