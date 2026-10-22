package com.mecanicaplus.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStats(
        long totalClients,
        long totalVehicles,
        long totalServiceOrders,
        long openServiceOrders,
        long inProgressServiceOrders,
        long waitingApprovalServiceOrders,
        BigDecimal monthlyRevenue,
        long lowStockItems,
        List<MonthlyRevenue> monthlyRevenueChart,
        List<TopMechanic> topMechanics,
        List<MostUsedPart> mostUsedParts) {

    public record MonthlyRevenue(String month, BigDecimal amount) {
    }

    public record TopMechanic(String mechanicId, String mechanicName, long completedOrders) {
    }

    public record MostUsedPart(String partId, String partName, long usageCount) {
    }
}
