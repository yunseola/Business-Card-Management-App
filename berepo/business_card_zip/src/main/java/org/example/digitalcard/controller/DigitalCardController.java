package org.example.digitalcard.controller;

import lombok.RequiredArgsConstructor;
import org.example.awss3.S3ServiceImpl;
import org.example.digitalcard.dto.DigitalCardDetailResponse;
import org.example.digitalcard.dto.DigitalGroupUpdateRequest;
import org.example.digitalcard.repository.DigitalCardRepository;
import org.example.digitalcard.service.DigitalService;
import org.example.group.service.GroupService;
import org.example.memo.service.MemoService;
import org.example.mycard.repository.MyCardRepository;
import org.example.mycard.service.MyCardServiceImpl;
import org.example.oauth.entity.User;
import org.example.oauth.repository.UserRepository;
import org.example.oauth.service.oauth.CustomOAuth2User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards/digital")
public class DigitalCardController {

    private static final Logger logger = LoggerFactory.getLogger(DigitalCardController.class);
    private final UserRepository userRepository;
    private final DigitalService digitalService;
    private final MyCardRepository myCardRepository;
    private final MemoService memoService;
    private final GroupService groupService;
    private final MyCardServiceImpl myCardService;

    // 등록
    @PostMapping("/{cardId}")
    public ResponseEntity<?> shareCard(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal) {

        Integer currentUserId = principal.getUserId();

        digitalService.createRelation(currentUserId, cardId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", 201,
                "message", "명함이 성공적으로 공유되었습니다."
        ));
    }

    // 상세조회
    @GetMapping("/{cardId}")
    public ResponseEntity<Map<String, Object>> getDigitalCardDetail(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Integer userId = principal.getUserId();
        DigitalCardDetailResponse response = digitalService.getDigitalCardDetail(cardId, userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "디지털 명함 상세 조회 성공",
                "result", response
        ));
    }


    // 삭제
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Map<String, Object>> deleteRelation(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Integer userId = principal.getUserId();
        digitalService.deleteRelation(userId, cardId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "디지털 명함의 관계가 해제되었습니다.",
                "result", "null"
        ));
    }


    // 즐겨 찾기
    @PutMapping("/{cardId}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Integer cardId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Integer userId = principal.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        boolean isFavorite = digitalService.toggleFavorite(userId, cardId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "즐겨찾기 상태가 변경되었습니다.",
                "result", Map.of("isFavorite", isFavorite)
        ));
    }

    // 그룹 수정
    @PutMapping("/{cardId}/group")
    public ResponseEntity<?> updateCardGroups(
            @PathVariable Integer cardId,
            @RequestBody DigitalGroupUpdateRequest request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        logger.info("updateCardGroups 호출됨 - cardId: {}, groups: {}", cardId, request.getGroups());
        Integer userId = customOAuth2User.getUserId(); // 사용자 ID 가져오기
        digitalService.updateCardGroups(cardId, request, userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "그룹 수정이 완료 되었습니다.",
                "result", "null"
        ));
    }

    // 공유
    @GetMapping("/share/{token}")
    public ResponseEntity<Map<String, Integer>> getSharedCardId(@PathVariable String token) {
        return myCardRepository.findByShareToken(token)
                .map(card -> ResponseEntity.ok(Map.of("cardId", card.getId())))
                .orElse(ResponseEntity.notFound().build()); // 문자열 바디를 섞지 않음 -> 제네릭 충돌 없음
    }
}