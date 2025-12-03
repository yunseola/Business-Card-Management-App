package org.example.mycard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyCardListItemResponse {
    private Integer cardId;
    private String imageUrlHorizontal;
    private Boolean confirmed;
}