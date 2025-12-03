package org.example.notification.controller;


import lombok.RequiredArgsConstructor;
import org.example.notification.dto.NotificationResponse;
import org.example.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.example.oauth.service.oauth.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/noti")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @AuthenticationPrincipal CustomOAuth2User principal) {

        Integer userId = principal.getUserId();
        List<NotificationResponse> notifications = notificationService.getNotificationsByUser(userId);

        return ResponseEntity.ok(
                Map.of(
                        "status", 200,
                        "message", "정상적으로 반환하였습니다.",
                        "result", notifications
                )
        );
    }

    // 읽음 처리
    @PutMapping("/{notiId}")
    public ResponseEntity<?> markAsRead(
            @PathVariable Integer notiId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            notificationService.markAsRead(userId, notiId);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "알림이 읽음 처리되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", 404,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", 500,
                    "message", "서버 오류 발생"
            ));
        }
    }

}
