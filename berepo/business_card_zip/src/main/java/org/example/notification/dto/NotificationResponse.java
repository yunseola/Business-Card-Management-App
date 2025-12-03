package org.example.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private Integer cardId;
    private String message;
    private boolean isRead;
    private String createdAt;
}
