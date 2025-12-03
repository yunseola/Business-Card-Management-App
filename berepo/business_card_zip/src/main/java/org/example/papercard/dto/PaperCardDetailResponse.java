package org.example.papercard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PaperCardDetailResponse {

    private String name;
    private String phone;
    private String company;
    private String position;
    private String email;
    private String image1Url;
    private String image2Url;
    private boolean digital;
    private boolean favorite;
    private LocalDateTime createdAt;

    private List<Field> fields;
    private List<Group> groups;
    private Memo memo;
    private List<ImagesHistory> imageHistories;

    @Getter
    @Builder
    public static class Field {
        private Integer fieldId;
        private String fieldName;
        private String fieldValue;
    }

    @Getter
    @Builder
    public static class Group {
        private Integer groupId;
        private String groupName;
    }

    @Getter
    @Builder
    public static class Memo {
        private Integer memoId;
        private String relationship;
        private String personality;
        private String workStyle;
        private String meetingNotes;
        private String etc;
    }

    @Getter
    @Builder
    public static class ImagesHistory {
        private String image1Url;
        private String image2Url;
        private LocalDateTime uploadedAt;
    }
}
