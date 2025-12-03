package org.example.company.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyVerifyRequest {
    private String email;
    private String code;
}
