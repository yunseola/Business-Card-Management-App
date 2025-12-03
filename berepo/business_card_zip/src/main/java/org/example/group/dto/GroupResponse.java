package org.example.group.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupResponse {

    private Integer groupId;
    private String name;
    private Integer headcount;
}
