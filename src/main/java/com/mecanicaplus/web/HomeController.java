package com.mecanicaplus.web;

import com.mecanicaplus.domain.Role;
import com.mecanicaplus.dto.DashboardStats;
import com.mecanicaplus.dto.DashboardStats.MonthlyRevenue;
import com.mecanicaplus.security.AppUserDetails;
import com.mecanicaplus.service.DashboardService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** "/" é o Dashboard (ADMIN/MANAGER); os demais papéis vão para a sua página inicial. */
@Controller
public class HomeController {

    public record Bar(String label, BigDecimal amount, int percent) {
    }

    private final DashboardService dashboard;

    public HomeController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal AppUserDetails user, Model model) {
        Role role = user.getRole();
        if (role == Role.RECEPTIONIST) {
            return "redirect:/clients";
        }
        if (role == Role.MECHANIC) {
            return "redirect:/service-orders";
        }
        DashboardStats stats = dashboard.getStats();
        model.addAttribute("stats", stats);
        model.addAttribute("bars", bars(stats.monthlyRevenueChart()));
        return "dashboard";
    }

    /** Altura de cada barra do gráfico, proporcional ao maior faturamento do período. */
    static List<Bar> bars(List<MonthlyRevenue> chart) {
        BigDecimal max = chart.stream()
                .map(MonthlyRevenue::amount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return chart.stream()
                .map(m -> new Bar(m.month(), m.amount(), percent(m.amount(), max)))
                .toList();
    }

    private static int percent(BigDecimal value, BigDecimal max) {
        if (max.signum() <= 0 || value.signum() <= 0) {
            return 0;
        }
        int pct = value.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
        return Math.max(pct, 2);
    }
}
