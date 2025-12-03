package org.example.group.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberResponse {
    
    private boolean isDigital;
    private Integer id;
    private String name;
    private String phone;
    private String company;
    private String position;
    private String imageUrl;
    private boolean isConfirm;
    private boolean isFavorite;
}
