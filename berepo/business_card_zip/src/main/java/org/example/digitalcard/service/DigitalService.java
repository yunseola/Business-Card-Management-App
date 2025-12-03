package org.example.digitalcard.service;

import lombok.RequiredArgsConstructor;
import org.example.digitalcard.dto.DigitalCardDetailResponse;
import org.example.digitalcard.dto.DigitalGroupUpdateRequest;
import org.example.digitalcard.entity.DigitalCard;
import org.example.digitalcard.entity.Relation;
import org.example.digitalcard.repository.DigitalCardRepository;
import org.example.digitalcard.repository.RelationRepository;
import org.example.group.entity.Group;
import org.example.group.entity.GroupMember;
import org.example.group.repository.GroupMemberRepository;
import org.example.group.repository.GroupRepository;
import org.example.memo.entity.Memo;
import org.example.memo.repository.MemoRepository;
import org.example.memo.service.MemoService;
import org.example.mycard.entity.CompanyHistory;
import org.example.mycard.repository.CompanyHistoryRepository;
import org.example.oauth.entity.User;
import org.example.oauth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DigitalService {

    private final UserRepository userRepository;
    private final RelationRepository relationRepository;
    private final DigitalCardRepository digitalCardRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CompanyHistoryRepository companyHistoryRepository;
    private final MemoRepository memoRepository;
    private final MemoService memoService;

    // 관계 등록
    @Transactional
    public void createRelation(Integer receiverId, Integer cardId) {
        DigitalCard card = digitalCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 디지털 명함이 존재하지 않습니다."));

        Integer giverId = card.getUser().getId();

        if (giverId.equals(receiverId)) {
            throw new IllegalArgumentException("자신의 명함은 공유할 수 없습니다.");
        }

        // 중복 방지 (선택사항)
        boolean exists = relationRepository.existsByUserIdAndCardId(receiverId, cardId);
        if (exists) {
            throw new IllegalStateException("이미 공유된 명함입니다.");
        }

        User user = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("받는 사용자가 존재하지 않습니다."));
        User giver = userRepository.findById(giverId)
                .orElseThrow(() -> new IllegalArgumentException("주는 사용자가 존재하지 않습니다."));

        Relation relation = Relation.builder()
                .user(user)
                .giver(giver)
                .card(card)
                .isFavorite(false)
                .build();

        DigitalCard saved = digitalCardRepository.save(card);
        memoService.createByCard(user, saved);

        relationRepository.save(relation);
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public DigitalCardDetailResponse getDigitalCardDetail(Integer cardId, Integer userId) {
        DigitalCard digitalCard = digitalCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 명함이 존재하지 않습니다."));

        // fields 변환
        List<DigitalCardDetailResponse.FieldDto> fields = digitalCard.getFields()
                .stream()
                .map(field -> DigitalCardDetailResponse.FieldDto.builder()
                        .fieldName(field.getFieldName())
                        .fieldValue(field.getFieldValue())
                        .fieldOrder(field.getFieldOrder())
                        .build())
                .collect(Collectors.toList());

        // relation 조회 (즐겨찾기, 메모 등)
        Optional<Relation> relationOpt = relationRepository.findByUserIdAndCardId(userId, cardId);

        // groups 조회
        List<GroupMember> groupMembers = groupMemberRepository.findByDigitalCardIdAndUserId(cardId, userId);
        List<DigitalCardDetailResponse.GroupDto> groups = groupMembers.stream()
                .map(gm -> DigitalCardDetailResponse.GroupDto.builder()
                        .groupId(gm.getGroup().getId())
                        .groupName(gm.getGroup().getName())
                        .build())
                .collect(Collectors.toList());

        // memo 변환
        Optional<Memo> memoOpt = memoRepository.findByUser_IdAndDigitalCard_Id(userId, cardId);

        DigitalCardDetailResponse.MemoDto memo = memoOpt.map(m ->
                DigitalCardDetailResponse.MemoDto.builder()
                        .relationship(m.getRelationship())
                        .personality(m.getPersonality())
                        .workStyle(m.getWorkStyle())
                        .meetingNotes(m.getMeetingNotes())
                        .etc(m.getEtc())
                        .build()
        ).orElse(null);

        // companyHistories 조회 및 변환
        List<CompanyHistory> histories = companyHistoryRepository.findByDigitalCard_Id(cardId);
        List<DigitalCardDetailResponse.CompanyHistoryDto> companyHistories = histories.stream()
                .map(h -> DigitalCardDetailResponse.CompanyHistoryDto.builder()
                        .company(h.getCompany())
                        .isConfirm(h.getConfirmed())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());

        return DigitalCardDetailResponse.builder()
                .name(digitalCard.getName())
                .phone(digitalCard.getPhone())
                .company(digitalCard.getCompany())
                .position(digitalCard.getPosition())
                .email(digitalCard.getEmail())
                .imageUrlHorizontal(digitalCard.getImageUrlHorizontal())
                .imageUrlVertical(digitalCard.getImageUrlVertical())
                .isDigital(digitalCard.getDigital())
                .isConfirm(digitalCard.getConfirmed())
                .isFavorite(relationOpt.map(Relation::getIsFavorite).orElse(false))
                .createdAt(digitalCard.getCreatedAt())
                .fields(fields)
                .groups(groups)
                .memo(memo)
                .companyHistories(companyHistories)
                .build();
    }

    // 관계 삭제
    @Transactional
    public void deleteRelation(Integer userId, Integer cardId) {
        memoRepository.deleteByUser_IdAndDigitalCard_Id(userId, cardId);
        relationRepository.deleteByUserIdAndCardId(userId, cardId);
    }

    // 즐찾
    @Transactional
    public boolean toggleFavorite(Integer userId, Integer cardId) {
        Relation relation = relationRepository.findByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new IllegalArgumentException("공유받은 명함이 아닙니다."));

        boolean newState = !relation.getIsFavorite();
        relation.setIsFavorite(newState);
        return newState;
    }


    private static final Logger logger = LoggerFactory.getLogger(DigitalService.class);

    // 그룹 수정
    @Transactional
    public void updateCardGroups(Integer cardId, DigitalGroupUpdateRequest newGroupIds, Integer userId) {
        logger.info("updateCardGroups 서비스 진입 - cardId: {}, userId: {}", cardId, userId);

        // 1. 관계 확인
        if (!relationRepository.existsByUserIdAndCardId(userId, cardId)) {
            logger.warn("접근 권한 없음 - userId: {}, cardId: {}", userId, cardId);
            throw new AccessDeniedException("접근 권한 없음");
        }

        // 2. 기존 그룹 조회
        List<GroupMember> currentGroupMember = groupMemberRepository.findByDigitalCardIdAndUserId(cardId, userId);


        Set<Integer> currentGroupIds = currentGroupMember.stream()
                .map(gm -> gm.getGroup().getId())
                .collect(Collectors.toSet());

        Set<Integer> newGroupIdSet = newGroupIds.getGroups().stream()
                .map(DigitalGroupUpdateRequest.Group::getGroupId)
                .collect(Collectors.toSet());


        // ➕ 추가할 그룹 ID = newGroupIds - currentGroupIds
        Set<Integer> toAdd = new HashSet<>(newGroupIdSet);
        toAdd.removeAll(currentGroupIds);

        // ➖ 삭제할 그룹 ID = currentGroupIds - newGroupIds
        Set<Integer> toDelete = new HashSet<>(currentGroupIds);
        toDelete.removeAll(newGroupIdSet);

        // 삭제 수행
        if (!toDelete.isEmpty()) {
            groupMemberRepository.deleteByDigitalCardIdAndGroupIdIn(cardId, new ArrayList<>(toDelete));
        }

        // 추가 수행
        for (Integer groupId : toAdd) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            DigitalCard digitalCard = digitalCardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException("Card not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            GroupMember newMember = GroupMember.builder()
                    .group(group)
                    .digitalCard(digitalCard)
                    .user(user)
                    .build();

            groupMemberRepository.save(newMember);
        }
    }

}
