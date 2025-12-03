package org.example.group.service;

import lombok.RequiredArgsConstructor;
import org.example.digitalcard.entity.Relation;
import org.example.digitalcard.repository.DigitalCardRepository;
import org.example.digitalcard.repository.RelationRepository;
import org.example.group.dto.GroupMemberResponse;
import org.example.group.dto.GroupMemberUpdateRequest;
import org.example.group.entity.Group;
import org.example.group.entity.GroupMember;
import org.example.group.repository.GroupMemberRepository;
import org.example.group.repository.GroupRepository;
import org.example.oauth.entity.User;
import org.example.papercard.repository.PaperCardRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMemberServiceImpl implements GroupMemberService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PaperCardRepository paperCardRepository;
    private final DigitalCardRepository digitalCardRepository;
    private final RelationRepository relationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(User user, Integer groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."));

        if (!group.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 그룹에 접근할 권한이 없습니다.");
        }

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);

        return members.stream()
                .map(member -> {
                    if (member.getPaperCard() != null) {
                        var paperCard = member.getPaperCard();
                        return GroupMemberResponse.builder()
                                .isDigital(false)
                                .id(paperCard.getId())
                                .name(paperCard.getName())
                                .phone(paperCard.getPhone())
                                .company(paperCard.getCompany())
                                .position(paperCard.getPosition())
                                .imageUrl(paperCard.getImage1Url())
                                .isConfirm(false)
                                .isFavorite(paperCard.getFavorite())
                                .build();
                    } else if (member.getDigitalCard() != null) {
                        var digitalCard = member.getDigitalCard();

                        // Relation에서 즐겨찾기 조회
                        boolean favorite = relationRepository
                                .findByUser_IdAndCard_Id(user.getId(), digitalCard.getId())
                                .map(Relation::getIsFavorite)
                                .orElse(false);

                        return GroupMemberResponse.builder()
                                .isDigital(true)
                                .id(digitalCard.getId())
                                .name(digitalCard.getName())
                                .phone(digitalCard.getPhone())
                                .company(digitalCard.getCompany())
                                .position(digitalCard.getPosition())
                                .imageUrl(digitalCard.getImageUrlHorizontal())
                                .isConfirm(digitalCard.getConfirmed())
                                .isFavorite(favorite)
                                .build();
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull) // 명함이 없는 멤버 제거
                .toList();
    }

    @Override
    @Transactional
    public void updateGroupMembers(User user, Integer groupId, List<GroupMemberUpdateRequest.CardRef> newCards ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."));

        if (!group.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 그룹에 접근할 권한이 없습니다.");
        }

        List<GroupMember> existingMembers = groupMemberRepository.findByGroupId(groupId);

        // 요청된 새 명함 목록 Set
        Set<String> newKeySet = newCards.stream()
                .map(card -> key(card.getCardId(), card.isDigital()))
                .collect(Collectors.toSet());

        // 기존 명함 중 요청에 없는 것 삭제
        for (GroupMember member : existingMembers) {
            String existingKey = key(
                    member.getPaperCard() != null ? member.getPaperCard().getId() : member.getDigitalCard().getId(),
                    member.getPaperCard() == null // 종이 없으면 디지털임
            );

            if (!newKeySet.contains(existingKey)) {
                groupMemberRepository.delete(member);
            }
        }

        // 기존 명함 목록 Set
        Set<String> existingKeySet = existingMembers.stream()
                .map(member -> key(
                        member.getPaperCard() != null ? member.getPaperCard().getId() : member.getDigitalCard().getId(),
                        member.getPaperCard() == null
                ))
                .collect(Collectors.toSet());

        // 새 명함 중 추가할 것만 추가
        for (GroupMemberUpdateRequest.CardRef cardRef : newCards) {
            String cardKey = key(cardRef.getCardId(), cardRef.isDigital());

            if (!existingKeySet.contains(cardKey)) {
                GroupMember newMember = GroupMember.builder()
                        .user(user)
                        .group(group)
                        .paperCard(!cardRef.isDigital() ? paperCardRepository.findById(cardRef.getCardId())
                                .orElseThrow(() -> new IllegalArgumentException("종이 명함이 존재하지 않습니다.")) : null)
                        .digitalCard(cardRef.isDigital() ? digitalCardRepository.findById(cardRef.getCardId())
                                .orElseThrow(() -> new IllegalArgumentException("디지털 명함이 존재하지 않습니다.")) : null)
                        .build();
                groupMemberRepository.save(newMember);
            }
        }

        // === headcount를 요청받은 카드 개수로 갱신 ===
        group.updateHeadcount(newCards.size());
        groupRepository.save(group);
    }

    // 키 생성
    private String key(Integer id, boolean isDigital) {
        return (isDigital ? "D" : "P") + id;
    }
}
