package com.retail_project.notification;

import com.retail_project.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    public List<NotificationResponse> getMyNotifications(String email) {
        Integer customerId = resolveCustomerId(email);
        return notificationRepository
                .findByOrderCustomerIdOrderByCreatedDateDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void markAsRead(Integer notificationId, String email) {
        Integer customerId = resolveCustomerId(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getOrder().getCustomer().getId() != customerId) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public long getUnreadCount(String email) {
        Integer customerId = resolveCustomerId(email);
        return notificationRepository.countByOrderCustomerIdAndIsReadFalse(customerId);
    }

    private Integer resolveCustomerId(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"))
                .getId();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.isRead(),
                n.getOrder().getId(),
                n.getOrder().getReference(),
                n.getCreatedDate()
        );
    }
}
