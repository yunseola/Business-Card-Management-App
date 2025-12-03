package org.example.card.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CardDuplicateCheckResponse {

    private int type;
    private Integer cardId;

    private List<MatchInfo> matches;

    @Getter
    @Builder
    public static class MatchInfo {
        private Integer cardId;
        private String name;
        private String phone;
        private String company;
        private String position;
        private String imageUrl;
    }
}
