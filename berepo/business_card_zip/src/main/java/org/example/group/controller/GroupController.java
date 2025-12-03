package org.example.group.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.ResponseUtil;
import org.example.group.dto.GroupMemberResponse;
import org.example.group.dto.GroupMemberUpdateRequest;
import org.example.group.dto.GroupRequest;
import org.example.group.dto.GroupResponse;
import org.example.group.entity.Group;
import org.example.group.service.GroupMemberService;
import org.example.group.service.GroupService;
import org.example.oauth.entity.User;
import org.example.oauth.repository.UserRepository;
import org.example.oauth.service.oauth.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final UserRepository userRepository;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    @PostMapping
    public ResponseEntity<?> createGroup(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestBody GroupRequest request
    ) {
        try {
            Integer userId = principal.getUserId();
            Group group = groupService.createGroup(userId, request.getName());

            return ResponseUtil.ok("그룹이 생성되었습니다.", Map.of("groupId", group.getId()));

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "리소스를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyGroups(
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        try {
            Integer userId = principal.getUserId();
            List<GroupResponse> groups = groupService.getMyGroups(userId);

            return ResponseUtil.ok("그룹 목록을 조회했습니다.", Map.of("groups", groups));

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "사용자를 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable Integer groupId,
            @RequestBody GroupRequest request
    ) {
        try {
            Integer userId = principal.getUserId();
            Group updated = groupService.updateGroup(userId, groupId, request.getName());

            GroupResponse response = GroupResponse.builder()
                    .groupId(updated.getId())
                    .name(updated.getName())
                    .headcount(updated.getHeadcount())
                    .build();

            return ResponseUtil.ok("그룹명이 수정되었습니다.", Map.of("group", response));

        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseUtil.error(403, "접근 권한이 없습니다.", e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseUtil.error(404, "그룹을 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable Integer groupId
    ) {
        try {
            Integer userId = principal.getUserId();
            groupService.deleteGroup(userId, groupId);

            return ResponseUtil.ok("그룹이 삭제되었습니다.", "");

        } catch (IllegalArgumentException e) {           // 400
            return ResponseUtil.error(400, "잘못된 요청입니다.", e.getMessage());
        } catch (AccessDeniedException e) {              // 403
            return ResponseUtil.error(403, "접근 권한이 없습니다.", e.getMessage());
        } catch (NoSuchElementException e) {             // 404
            return ResponseUtil.error(404, "그룹을 찾을 수 없습니다.", e.getMessage());
        } catch (Exception e) {                          // 500
            return ResponseUtil.error(500, "server error!", e.getMessage());
        }
    }


    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable Integer groupId
    ) {
        Integer userId = principal.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        List<GroupMemberResponse> result = groupMemberService.getGroupMembers(user, groupId);

        return ResponseEntity.ok(
                Map.of(
                        "status", 200,
                        "message", "그룹원 목록 조회 성공",
                        "result", result
                )
        );
    }

    @PutMapping("/{groupId}/members")
    public ResponseEntity<?> updateGroupMembers(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @PathVariable Integer groupId,
            @RequestBody GroupMemberUpdateRequest request
    ) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        groupMemberService.updateGroupMembers(user, groupId, request.getMembers());

        return ResponseEntity.ok(
                Map.of(
                        "status", 200,
                        "message", "그룹원 목록이 수정되었습니다.",
                        "result", Map.of()
                )
        );
    }
}
