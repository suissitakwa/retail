package com.retail_project.payment;

import com.retail_project.order.Order;
import com.retail_project.order.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Payment {
    @Id
    @GeneratedValue
    private Integer id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;



    private String stripeSessionId;
    private String stripePaymentIntentId;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private BigDecimal amount;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;
    private LocalDateTime paidAt;



    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

}
