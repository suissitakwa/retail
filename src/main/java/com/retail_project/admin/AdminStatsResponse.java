package com.retail_project.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AdminStatsResponse(
        BigDecimal totalRevenue,
        BigDecimal revenue30Days,
        long totalOrders,
        long totalCustomers,
        Map<String, Long> ordersByStatus,
        List<TopProductResult> topProducts
) {}
