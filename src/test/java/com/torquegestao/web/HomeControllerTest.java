package com.torquegestao.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.torquegestao.dto.DashboardStats.MonthlyRevenue;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class HomeControllerTest {

    @Test
    void barrasSaoProporcionaisAoMaiorValor() {
        var bars = HomeController.bars(List.of(
                new MonthlyRevenue("Mai", new BigDecimal("100.00")),
                new MonthlyRevenue("Jun", new BigDecimal("50.00")),
                new MonthlyRevenue("Jul", BigDecimal.ZERO)));
        assertEquals(100, bars.get(0).percent());
        assertEquals(50, bars.get(1).percent());
        assertEquals(0, bars.get(2).percent());
    }

    @Test
    void graficoSemFaturamentoNaoQuebra() {
        var bars = HomeController.bars(List.of(new MonthlyRevenue("Mai", BigDecimal.ZERO)));
        assertEquals(0, bars.get(0).percent());
    }

    @Test
    void valorPequenoAindaApareceNoGrafico() {
        var bars = HomeController.bars(List.of(
                new MonthlyRevenue("Mai", new BigDecimal("100000.00")),
                new MonthlyRevenue("Jun", new BigDecimal("1.00"))));
        assertTrue(bars.get(1).percent() >= 2);
    }
}
