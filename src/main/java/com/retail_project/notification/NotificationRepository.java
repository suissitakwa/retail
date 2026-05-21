package com.retail_project.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByOrderCustomerIdOrderByCreatedDateDesc(Integer customerId);

    long countByOrderCustomerIdAndIsReadFalse(Integer customerId);
}
