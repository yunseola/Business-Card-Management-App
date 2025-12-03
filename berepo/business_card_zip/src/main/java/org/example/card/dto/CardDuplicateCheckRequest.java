package org.example.card.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardDuplicateCheckRequest {
    private String name;
    private String phone;
    private String company;
}
