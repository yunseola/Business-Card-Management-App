package org.example.notification.service;

import org.example.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getNotificationsByUser(Integer userId);
    void markAsRead(Integer userId, Integer notiId);
}
