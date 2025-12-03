package org.example.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class CallResponse {
    private String name;
    private String phone;
    private String company;
    private String position;
    private String imageUrlHorizontal;
    private String summary;
}
