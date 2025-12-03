package org.example.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.notification.dto.NotificationResponse;
import org.example.notification.entity.Notification;
import org.example.notification.repository.NotificationRepository;
import org.example.oauth.entity.User;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<NotificationResponse> getNotificationsByUser(Integer userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(noti -> NotificationResponse.builder()
                        .cardId(noti.getCard().getId())
                        .message(noti.getMessage())
                        .isRead(noti.isRead())
                        .createdAt(noti.getCreatedAt().format(formatter))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Integer userId, Integer notiId) {
        Notification notification = notificationRepository.findById(notiId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("접근 권한이 없습니다.");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }


}
