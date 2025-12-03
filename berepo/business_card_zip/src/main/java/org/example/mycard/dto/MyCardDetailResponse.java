package org.example.mycard.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyCardDetailResponse {
    private String name;
    private String phone;
    private String company;
    private String position;
    private String email;
    private String customImageUrl;
    private String imageUrlHorizontal;
    private String imageUrlVertical;
    private Boolean confirmed;
    private Integer backgroundImageNum;
    private Boolean fontColor;

    private String shareUrl;
    private String qrCodeUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    private List<FieldDto> fields;
    private List<CompanyHistoryDto> companyHistories;

    @Getter
    @Builder
    public static class FieldDto {
        private Integer fieldId;
        private String fieldName;
        private String fieldValue;
        private Integer fieldOrder;
    }

    @Getter
    @Builder
    public static class CompanyHistoryDto {
        private String company;
        private Boolean confirmed;
        private LocalDateTime changedAt;
    }
}

