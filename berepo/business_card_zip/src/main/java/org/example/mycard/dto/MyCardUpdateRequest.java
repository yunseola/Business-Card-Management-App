package org.example.mycard.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MyCardUpdateRequest {
    private String name;
    private String phone;
    private String company;
    private String position;
    private String email;
    private Integer backgroundImageNum;
    private Boolean fontColor;
    private List<FieldDto> fields;

    @Getter
    @Setter
    public static class FieldDto {
        private Integer fieldId;
        private String fieldName;
        private String fieldValue;
        private Integer fieldOrder;
    }
}

