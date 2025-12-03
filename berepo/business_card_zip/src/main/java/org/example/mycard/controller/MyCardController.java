package org.example.mycard.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.ResponseUtil;
import org.example.mycard.dto.MyCardDetailResponse;
import org.example.mycard.dto.MyCardListItemResponse;
import org.example.mycard.dto.MyCardRegisterRequest;
import org.example.mycard.dto.MyCardUpdateRequest;
import org.example.mycard.service.MyCardServiceImpl;
import org.example.oauth.service.oauth.CustomOAuth2User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/cards/mine")
public class MyCardController {

    private final MyCardServiceImpl myCardService;

    // 내 명함 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerMyCard(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestPart("request") MyCardRegisterRequest request,
            @RequestPart(value = "custom_image", required = false) MultipartFile customImage,
            @RequestPart(value = "imageUrlHorizontal", required = false) MultipartFile imageUrlHorizontal,
            @RequestPart(value = "imageUrlVertical", required = false) MultipartFile imageUrlVertical
    ) {
        try {
            Integer userId = principal.getUserId();

            Integer cardId = myCardService.registerMyCard(userId, request, customImage, imageUrlHorizontal, imageUrlVertical);

            return ResponseUtil.created("내 명함이 생성되었습니다.", Map.of("cardId", cardId));

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "필수 입력값이 누락되었습니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    // 내 명함 목록 조회
    @GetMapping
    public ResponseEntity<?> getMyCardList(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            List<MyCardListItemResponse> cardList = myCardService.getMyCardList(userId);

            return ResponseUtil.ok("내 명함 목록 조회 성공", cardList);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    // 내 명함 상세 조회
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getMyCardDetail(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            MyCardDetailResponse response = myCardService.getMyCardDetail(userId, cardId);

            return ResponseUtil.ok("내 명함 상세 조회 성공", response);

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    // 내 명함 수정
    @PutMapping(value = "/{cardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMyCard(
            @PathVariable Integer cardId,
            @RequestPart("request") MyCardUpdateRequest request,
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestPart(value = "custom_image", required = false) MultipartFile customImage,
            @RequestPart(value = "imageUrlHorizontal", required = false) MultipartFile imageUrlHorizontal,
            @RequestPart(value = "imageUrlVertical", required = false) MultipartFile imageUrlVertical
    ) {
        try {
            Integer userId = principal.getUserId();
            myCardService.updateMyCard(userId, cardId, request, customImage, imageUrlHorizontal, imageUrlVertical);

            return ResponseUtil.ok("명함이 성공적으로 수정되었습니다.", Map.of("cardId", cardId));

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    // 내 명함 삭제
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteMyCard(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            myCardService.deleteMyCard(userId, cardId);

            return ResponseUtil.ok("명함이 성공적으로 삭제되었습니다.", "");

        } catch (IllegalArgumentException e) {          // 400
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (NoSuchElementException e) {            // 404
            return ResponseUtil.error(404, "명함을 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {                         // 500
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }
}
