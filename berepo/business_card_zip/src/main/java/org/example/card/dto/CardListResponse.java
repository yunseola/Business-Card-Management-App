package org.example.card.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CardListResponse {

    private Integer cardId;
    private String name;
    private String phone;
    private String company;
    private String position;
    private String email;
    private String imageUrl;
    private boolean digital;
    private boolean confirmed;
    private boolean favorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
