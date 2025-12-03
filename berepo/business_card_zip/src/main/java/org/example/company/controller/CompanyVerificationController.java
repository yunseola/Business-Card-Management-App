package org.example.company.controller;

import lombok.RequiredArgsConstructor;
import org.example.company.dto.CompanyRequest;
import org.example.company.dto.CompanyVerifyRequest;
import org.example.company.service.CompanyVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/company/{cardId}")
@RequiredArgsConstructor
public class CompanyVerificationController {

    private final CompanyVerificationService service;

    @PostMapping("/request-code")
    public ResponseEntity<?> requestCode(@PathVariable Integer cardId,
                                         @RequestBody CompanyRequest dto) {
        int match = service.requestCode(cardId, dto);
        if (match == CompanyVerificationService.MATCH_OK) {
            return ResponseEntity.status(201).body(Map.of(
                    "status", 201,
                    "message", "인증 코드를 이메일로 발송했습니다.",
                    "match", 1
            ));
        }
        // 불일치
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "회사 이메일이 아닙니다.",
                "match", 2
        ));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@PathVariable Integer cardId,
                                        @RequestBody CompanyVerifyRequest dto) {
        int result = service.verifyCode(cardId, dto);
        if (result == CompanyVerificationService.VERIFY_OK) {
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "회사 인증이 완료되었습니다.",
                    "verify", 1
            ));
        }
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "회사 인증을 실패하였습니다.",
                "verify", 2
        ));
    }
}
