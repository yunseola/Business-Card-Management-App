package org.example.digitalcard.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class DigitalCardDetailResponse {
    private String name;
    private String phone;
    private String company;
    private String position;
    private String email;

    private String imageUrlHorizontal;
    private String imageUrlVertical;
    private Boolean isDigital;
    private Boolean isConfirm;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private List<FieldDto> fields;
    private List<GroupDto> groups;
    private MemoDto memo;
    private List<CompanyHistoryDto> companyHistories;

    @Getter
    @Builder
    public static class FieldDto {
        private String fieldName;
        private String fieldValue;
        private Integer fieldOrder;
    }

    @Getter
    @Builder
    public static class GroupDto {
        private Integer groupId;
        private String groupName;
    }

    @Getter
    @Builder
    public static class MemoDto {
        private String relationship;
        private String personality;
        private String workStyle;
        private String meetingNotes;
        private String etc;
    }

    @Getter
    @Builder
    public static class CompanyHistoryDto {
        private String company;
        private Boolean isConfirm;
        private LocalDateTime changedAt;
    }
}
