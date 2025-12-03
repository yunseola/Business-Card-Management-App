package org.example.papercard.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdatePaperCardRequest {

    private String name;
    private String phone;
    private String company;
    private String position;
    private String email;
    private List<Field> fields;
    private List<Group> groups;

    @Getter
    @Setter
    public static class Field {
        private Integer fieldId;
        private String fieldName;
        private String fieldValue;
    }

    @Getter
    @Setter
    public static class Group {
        private Integer groupId;
    }
}
