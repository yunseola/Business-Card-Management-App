package org.example.memo.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.ResponseUtil;
import org.example.memo.dto.MemoUpdateRequest;
import org.example.memo.service.MemoService;
import org.example.oauth.service.oauth.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/memos")
public class MemoController {

    private final MemoService memoService;

    @PutMapping("/{type}/{cardId}")
    public ResponseEntity<?> updateMemo(
            @PathVariable String type,
            @PathVariable Integer cardId,
            @RequestBody MemoUpdateRequest request,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();

            switch (type.toLowerCase()) {
                case "paper" -> memoService.updatePaperMemo(cardId, userId, request);
                case "digital" -> memoService.updateDigitalMemo(cardId, userId, request);
                default -> throw new IllegalArgumentException("지원하지 않는 명함 타입입니다: " + type);
            }

            return ResponseUtil.ok("메모가 수정되었습니다.", "");

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }
}
