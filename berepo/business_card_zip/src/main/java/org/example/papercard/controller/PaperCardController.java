package org.example.papercard.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.ResponseUtil;
import org.example.oauth.service.oauth.CustomOAuth2User;
import org.example.papercard.dto.PaperCardDetailResponse;
import org.example.papercard.dto.RegisterPaperCardRequest;
import org.example.papercard.dto.UpdatePaperCardRequest;
import org.example.papercard.service.PaperCardService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/cards/paper")
public class PaperCardController {

    private final PaperCardService paperCardService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerPaperCard(
            @RequestPart("request") RegisterPaperCardRequest request,
            @RequestPart("image1") MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            Integer cardId = paperCardService.registerPaperCard(userId, request, image1, image2);

            return ResponseUtil.created("종이 명함이 등록되었습니다.", Map.of("cardId", cardId));

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "필수 입력값이 누락되었습니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<?> getPaperCardDetail(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            PaperCardDetailResponse response = paperCardService.getPaperCardDetail(userId, cardId);

            return ResponseUtil.ok("종이 명함 상세 정보입니다.", response);

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseUtil.error(403, "접근 권한이 없습니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<?> updatePaperCard(
            @PathVariable Integer cardId,
            @RequestPart("request") UpdatePaperCardRequest request,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            paperCardService.updatePaperCard(userId, cardId, request, image1, image2);

            return ResponseUtil.ok("종이 명함이 수정되었습니다.", Map.of("cardId", cardId));

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseUtil.error(403, "접근 권한이 없습니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deletePaperCard(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            paperCardService.deletePaperCard(userId, cardId);

            return ResponseUtil.ok("종이 명함이 삭제되었습니다.", "");

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseUtil.error(403, "접근 권한이 없습니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    @PutMapping("/{cardId}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            boolean favorite = paperCardService.toggleFavorite(userId, cardId);

            return ResponseUtil.ok("즐겨찾기 상태가 변경되었습니다.", Map.of("cardId", cardId, "favorite", favorite));

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseUtil.error(403, "접근 권한이 없습니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }
}
