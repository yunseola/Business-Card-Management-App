package org.example.digitalcard.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
public class DigitalCardRegisterRequest {
    private String name;
    private String phone;
    private String company;
    private String position;
    private Integer backgroundImageNum;
    private Boolean fontColor;
    private List<FieldDto> fields;

    @Getter
    @Setter
    @Builder
    public static class FieldDto {
        private String fieldName;
        private String fieldValue;
        private Integer fieldOrder;
    }
}
