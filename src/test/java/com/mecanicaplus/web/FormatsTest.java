package com.mecanicaplus.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class FormatsTest {

    @Test
    void formataCpfApenasQuandoTemOnzeDigitos() {
        assertEquals("123.456.789-00", Formats.cpf("12345678900"));
        assertEquals("123", Formats.cpf("123"));
        assertEquals("—", Formats.cpf(null));
    }

    @Test
    void formataTelefone() {
        assertEquals("(11) 99999-9999", Formats.phone("11999999999"));
        assertEquals("—", Formats.phone(" "));
    }

    @Test
    void formataMoedaEmReais() {
        String text = Formats.currency(new BigDecimal("1234.5")).replace('\u00a0', ' ');
        assertEquals("R$ 1.234,50", text);
        assertEquals("R$ 0,00", Formats.currency(null).replace('\u00a0', ' '));
    }

    @Test
    void formataDataNoFusoDaOficina() {
        Instant instant = Instant.parse("2026-10-22T02:30:00Z");
        assertEquals("21/10/2026", Formats.date(instant, ZoneId.of("America/Sao_Paulo")));
        assertEquals("21/10/2026 23:30", Formats.dateTime(instant, ZoneId.of("America/Sao_Paulo")));
        assertEquals("—", Formats.date(null, ZoneId.of("UTC")));
    }

    @Test
    void formataQuilometragem() {
        assertEquals("12.345 km", Formats.kilometers(12345));
        assertEquals("—", Formats.kilometers(null));
    }
}
