package com.retail_project.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Integer id,
        String type,
        String message,
        boolean isRead,
        Integer orderId,
        String orderReference,
        LocalDateTime createdDate
) {}
