package org.example.digitalcard.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DigitalGroupUpdateRequest {

    private List<Group> groups;

    @Getter
    @Setter
    public static class Group {
        private Integer groupId;
    }
}