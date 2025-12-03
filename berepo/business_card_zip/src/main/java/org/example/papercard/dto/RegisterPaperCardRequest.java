package org.example.papercard.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegisterPaperCardRequest {

    private String name;
    private String phone;
    private String company;
    private String position;
    private String email;
    private List<Field> fields;

    @Getter
    @Setter
    public static class Field {
        private String fieldName;
        private String fieldValue;
    }
}
