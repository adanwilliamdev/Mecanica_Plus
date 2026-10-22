package com.torquegestao.api;

import com.torquegestao.dto.DashboardStats;
import com.torquegestao.security.Access;
import com.torquegestao.service.DashboardService;
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
