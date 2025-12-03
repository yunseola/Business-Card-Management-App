package org.example.card.controller;

import lombok.RequiredArgsConstructor;
import org.example.card.dto.CardDuplicateCheckRequest;
import org.example.card.dto.CardDuplicateCheckResponse;
import org.example.card.dto.CardListResponse;
import org.example.card.service.CardService;
import org.example.common.ResponseUtil;
import org.example.oauth.service.oauth.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<?> getMyCardList(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            List<CardListResponse> result = cardService.getCardList(principal.getUserId());

            return ResponseUtil.ok("명함 목록 조회 성공", result);

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "필수 입력값이 누락되었습니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkDuplicateCard(
            @RequestBody CardDuplicateCheckRequest request,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            CardDuplicateCheckResponse result = cardService.checkDuplicateCard(principal.getUserId(), request);

            return ResponseEntity.ok(
                    Map.of(
                            "status", 200,
                            "message", switch (result.getType()) {
                                case 1 -> "중복된 디지털 명함이 있습니다.";
                                case 2 -> "중복된 종이 명함이 있습니다.";
                                case 3 -> "2개 항목이 일치하는 종이 명함이 있습니다.";
                                case 4 -> "중복된 명함이 없습니다.";
                                default -> "알 수 없는 중복 상태입니다.";
                            },
                            "result", result
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }
}
