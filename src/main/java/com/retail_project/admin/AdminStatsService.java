package com.retail_project.admin;

import com.retail_project.customer.CustomerRepository;
import com.retail_project.order.OrderRepository;
import com.retail_project.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public AdminStatsResponse getStats() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        Arrays.stream(OrderStatus.values())
                .forEach(s -> byStatus.put(s.name(), orderRepository.countByStatus(s)));

        List<TopProductResult> top = orderRepository
                .findTopProductsByUnitsSold(PageRequest.of(0, 5))
                .stream()
                .map(row -> new TopProductResult((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        return new AdminStatsResponse(
                orderRepository.sumCompletedRevenue(),
                orderRepository.sumCompletedRevenueSince(LocalDateTime.now().minusDays(30)),
                orderRepository.count(),
                customerRepository.count(),
                byStatus,
                top
        );
    }
}
