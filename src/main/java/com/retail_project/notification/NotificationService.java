package com.retail_project.notification;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
}
