package org.example.oauth.controller;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.example.common.ResponseUtil;
import org.example.oauth.entity.User;
import org.example.oauth.repository.UserRepository;
import org.example.oauth.service.google.GoogleVerifierService;
import org.example.oauth.service.jwt.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleVerifierService googleVerifierService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginWithGoogle(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Authorization 헤더 형식이 잘못되었습니다."
            ));
        }

        String idToken = authorizationHeader.substring(7);
        GoogleIdToken.Payload payload = googleVerifierService.verify(idToken);

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // 유저가 없으면 db에 저장
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .build()));

        String jwt = jwtProvider.createToken(user.getId(), user.getEmail());

        return ResponseUtil.ok(
                "로그인 성공",
                Map.of(
                        "jwtToken", jwt,
                        "userId", user.getId()
                )
        );
    }
}