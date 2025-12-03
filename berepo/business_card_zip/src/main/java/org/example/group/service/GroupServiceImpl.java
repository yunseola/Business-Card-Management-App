package org.example.group.service;

import lombok.RequiredArgsConstructor;
import org.example.group.dto.GroupResponse;
import org.example.group.entity.Group;
import org.example.group.repository.GroupRepository;
import org.example.oauth.entity.User;
import org.example.oauth.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private static final String ERR_USER_NOT_FOUND = "존재하지 않는 사용자입니다.";
    private static final String ERR_GROUP_NOT_FOUND = "해당 그룹이 존재하지 않습니다.";
    private static final String ERR_NAME_REQUIRED = "그룹명은 필수입니다.";
    private static final String ERR_NAME_DUPLICATE = "이미 존재하는 그룹명입니다.";
    private static final String ERR_FORBIDDEN = "해당 그룹에 대한 권한이 없습니다.";

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public Group createGroup(Integer userId, String groupName) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException(ERR_NAME_REQUIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        if (groupRepository.existsByUserIdAndName(userId, groupName)) {
            throw new IllegalArgumentException(ERR_NAME_DUPLICATE);
        }

        Group group = Group.builder()
                .user(user)
                .name(groupName)
                .headcount(0)
                .build();

        return groupRepository.save(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        return groupRepository.findAllByUserIdOrderByNameAsc(userId).stream()
                .map(group -> GroupResponse.builder()
                        .groupId(group.getId())
                        .name(group.getName())
                        .headcount(group.getHeadcount())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public Group updateGroup(Integer userId, Integer groupId, String groupName) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException(ERR_NAME_REQUIRED);
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException(ERR_GROUP_NOT_FOUND));

        if (!group.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(ERR_FORBIDDEN);
        }

        if (groupRepository.existsByUserIdAndName(userId, groupName)) {
            throw new IllegalArgumentException(ERR_NAME_DUPLICATE);
        }

        group.updateName(groupName);
        return group;
    }

    @Override
    @Transactional
    public void deleteGroup(Integer userId, Integer groupId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException(ERR_GROUP_NOT_FOUND));

        if (!group.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(ERR_FORBIDDEN);
        }

        groupRepository.delete(group);
    }
}
