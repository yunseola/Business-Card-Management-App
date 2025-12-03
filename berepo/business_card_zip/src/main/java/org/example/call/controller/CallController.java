package org.example.call.controller;

import lombok.RequiredArgsConstructor;
import org.example.call.dto.CallResponse;
import org.example.call.repository.CardRepository;
import org.example.call.service.CallService;
import org.example.digitalcard.entity.DigitalCard;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
public class CallController{

    private final CallService callService;

    @GetMapping("/{phone}")
    public ResponseEntity<Map<String, Object>> getCallCardInfo(
            @AuthenticationPrincipal(expression = "userId") Integer userId,
            @PathVariable String phone
    ) {
        CallResponse response = callService.getCardInfoByPhone(phone, userId);

        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("status", 200);
        resultMap.put("message", "정상적으로 반환하였습니다.");
        resultMap.put("result", response);

        return ResponseEntity.ok(resultMap);
    }

}

