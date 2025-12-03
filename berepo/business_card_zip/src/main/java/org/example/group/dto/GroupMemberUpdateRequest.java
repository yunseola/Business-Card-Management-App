package org.example.group.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberUpdateRequest {

    private List<CardRef> members;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardRef {
        private Integer cardId;
        private boolean isDigital;
    }
}
