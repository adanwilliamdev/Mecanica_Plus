package com.mecanicaplus.api;

import com.mecanicaplus.dto.DashboardStats;
import com.mecanicaplus.security.Access;
import com.mecanicaplus.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboard;

    public DashboardApiController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/stats")
    @PreAuthorize(Access.DASHBOARD)
    public DashboardStats stats() {
        return dashboard.getStats();
    }
}
