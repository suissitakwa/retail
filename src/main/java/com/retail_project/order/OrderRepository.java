package com.retail_project.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Integer> {
    Order findByStripeSessionId(String sessionId);
    Page<Order> findByCustomerId(Integer customerId, Pageable pageable);
    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal sumCompletedRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdDate >= :since")
    BigDecimal sumCompletedRevenueSince(@Param("since") LocalDateTime since);

    @Query("""
           SELECT oi.product.name, SUM(oi.quantity)
           FROM OrderItem oi
           JOIN oi.order o
           WHERE o.status = 'COMPLETED'
           GROUP BY oi.product.name
           ORDER BY SUM(oi.quantity) DESC
           """)
    List<Object[]> findTopProductsByUnitsSold(Pageable pageable);
}
