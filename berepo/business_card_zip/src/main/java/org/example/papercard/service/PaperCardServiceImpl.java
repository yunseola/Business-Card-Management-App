package org.example.papercard.service;

import lombok.RequiredArgsConstructor;
import org.example.awss3.S3Service;
import org.example.group.entity.Group;
import org.example.group.entity.GroupMember;
import org.example.group.repository.GroupMemberRepository;
import org.example.group.repository.GroupRepository;
import org.example.memo.entity.Memo;
import org.example.memo.repository.MemoRepository;
import org.example.memo.service.MemoService;
import org.example.oauth.entity.User;
import org.example.oauth.repository.UserRepository;
import org.example.papercard.dto.PaperCardDetailResponse;
import org.example.papercard.dto.RegisterPaperCardRequest;
import org.example.papercard.dto.UpdatePaperCardRequest;
import org.example.papercard.entity.ImagesHistory;
import org.example.papercard.entity.PaperCard;
import org.example.papercard.entity.PaperCardField;
import org.example.papercard.repository.ImageHistoryRepository;
import org.example.papercard.repository.PaperCardFieldRepository;
import org.example.papercard.repository.PaperCardRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperCardServiceImpl implements PaperCardService {

    private static final String ERR_USER_NOT_FOUND = "존재하지 않는 사용자입니다.";
    private static final String ERR_CARD_NOT_FOUND = "명함을 찾을 수 없습니다.";
    private static final String ERR_FORBIDDEN = "해당 명함에 접근할 수 없습니다.";

    private final UserRepository userRepository;
    private final PaperCardRepository paperCardRepository;
    private final PaperCardFieldRepository paperCardFieldRepository;
    private final MemoRepository memoRepository;
    private final ImageHistoryRepository imageHistoryRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final S3Service s3Service;
    private final MemoService memoService;

    @Override
    @Transactional
    public Integer registerPaperCard(Integer userId, RegisterPaperCardRequest request, MultipartFile image1, MultipartFile image2) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        if (image1 == null || image1.isEmpty()
                || request.getName() == null || request.getName().trim().isEmpty()
                || request.getPhone() == null || request.getPhone().trim().isEmpty()
                || request.getCompany() == null || request.getCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("필수 입력값이 누락되었습니다.");
        }

        // S3 이미지 업로드
        String image1Url = s3Service.upload(image1, "paper");
        String image2Url = (image2 != null && !image2.isEmpty()) ? s3Service.upload(image2, "paper") : null;

        PaperCard paperCard = PaperCard.builder()
                .user(user)
                .name(request.getName())
                .phone(request.getPhone())
                .company(request.getCompany())
                .position(request.getPosition())
                .email(request.getEmail())
                .image1Url(image1Url)
                .image2Url(image2Url)
                .digital(false)
                .favorite(false)
                .build();

        PaperCard saved = paperCardRepository.save(paperCard);

        if (request.getFields() != null) {
            for (RegisterPaperCardRequest.Field field : request.getFields()) {
                PaperCardField fieldEntity = PaperCardField.builder()
                        .paperCard(saved)
                        .fieldName(field.getFieldName())
                        .fieldValue(field.getFieldValue())
                        .build();
                paperCardFieldRepository.save(fieldEntity);
            }
        }

        memoService.createByCard(user, saved);

        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public PaperCardDetailResponse getPaperCardDetail(Integer userId, Integer cardId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        PaperCard paperCard = paperCardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException(ERR_CARD_NOT_FOUND));

        if (!paperCard.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(ERR_FORBIDDEN);
        }

        List<PaperCardField> fieldList = paperCardFieldRepository.findByPaperCard_IdOrderByIdAsc(cardId);
        List<PaperCardDetailResponse.Field> fields = fieldList.stream()
                .map(f -> PaperCardDetailResponse.Field.builder()
                        .fieldId(f.getId())
                        .fieldName(f.getFieldName())
                        .fieldValue(f.getFieldValue())
                        .build())
                .toList();

        List<GroupMember> groupMembers = groupMemberRepository.findByPaperCardIdOrderByIdAsc(cardId);
        List<PaperCardDetailResponse.Group> groups = groupMembers.stream()
                .map(member -> PaperCardDetailResponse.Group.builder()
                        .groupId(member.getGroup().getId())
                        .groupName(member.getGroup().getName())
                        .build())
                .toList();

        Memo memo = memoRepository.findByPaperCard_Id(cardId);
        PaperCardDetailResponse.Memo memoDto = (memo != null) ? PaperCardDetailResponse.Memo.builder()
                .memoId(memo.getId())
                .relationship(memo.getRelationship())
                .personality(memo.getPersonality())
                .workStyle(memo.getWorkStyle())
                .meetingNotes(memo.getMeetingNotes())
                .etc(memo.getEtc())
                .build() : null;

        List<ImagesHistory> historyList = imageHistoryRepository.findByPaperCard_IdOrderByUploadedAtDesc(cardId);
        List<PaperCardDetailResponse.ImagesHistory> imageHistories = historyList.stream()
                .map(h -> PaperCardDetailResponse.ImagesHistory.builder()
                        .image1Url(h.getImage1Url())
                        .image2Url(h.getImage2Url())
                        .uploadedAt(h.getUploadedAt())
                        .build())
                .toList();

        return PaperCardDetailResponse.builder()
                .name(paperCard.getName())
                .phone(paperCard.getPhone())
                .company(paperCard.getCompany())
                .position(paperCard.getPosition())
                .email(paperCard.getEmail())
                .image1Url(paperCard.getImage1Url())
                .image2Url(paperCard.getImage2Url())
                .digital(paperCard.getDigital())
                .favorite(paperCard.getFavorite())
                .createdAt(paperCard.getCreatedAt())
                .fields(fields)
                .groups(groups)
                .memo(memoDto)
                .imageHistories(imageHistories)
                .build();
    }

    @Override
    @Transactional
    public void updatePaperCard(Integer userId, Integer cardId, UpdatePaperCardRequest request, MultipartFile image1, MultipartFile image2) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        PaperCard paperCard = paperCardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException(ERR_CARD_NOT_FOUND));

        if (!paperCard.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(ERR_FORBIDDEN);
        }

        // image1이 새로 들어왔으면 히스토리에 저장 후 새로 업로드
        if (image1 != null && !image1.isEmpty()) {
            ImagesHistory history = ImagesHistory.builder()
                    .paperCard(paperCard)
                    .image1Url(paperCard.getImage1Url())
                    .image2Url(paperCard.getImage2Url()) // image2도 같이 저장
                    .build();
            imageHistoryRepository.save(history);

            // S3 업로드 후 반영
            String newImage1Url = s3Service.upload(image1, "paper");
            String newImage2Url = (image2 != null && !image2.isEmpty())
                    ? s3Service.upload(image2, "paper")
                    : null;

            paperCard.updateImages(newImage1Url, newImage2Url);
        }

        // 기본 정보 수정
        paperCard.updateInfo(
                request.getName(),
                request.getPhone(),
                request.getCompany(),
                request.getPosition(),
                request.getEmail()
        );

        // 기존 필드
        List<PaperCardField> existingFields = paperCardFieldRepository.findByPaperCard_Id(cardId);
        Map<Integer, PaperCardField> existingFieldMap = existingFields.stream()
                .collect(Collectors.toMap(PaperCardField::getId, f -> f));
        Set<Integer> existingFieldIds = existingFieldMap.keySet();

        // 요청된 필드
        List<UpdatePaperCardRequest.Field> requestedFields = request.getFields() != null
                ? request.getFields()
                : List.of();
        Set<Integer> requestFieldIds = requestedFields.stream()
                .map(UpdatePaperCardRequest.Field::getFieldId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 필드 삭제
        Set<Integer> toDeleteField = new HashSet<>(existingFieldIds);
        toDeleteField.removeAll(requestFieldIds);
        if (!toDeleteField.isEmpty()) {
            paperCardFieldRepository.deleteByIdIn(toDeleteField);
        }

        // 필드 추가 및 수정
        for (UpdatePaperCardRequest.Field fieldDto : requestedFields) {
            Integer fieldId = fieldDto.getFieldId();
            String name = fieldDto.getFieldName();
            String value = fieldDto.getFieldValue();

            if (fieldId == null) {
                PaperCardField newField = PaperCardField.builder()
                        .paperCard(paperCard)
                        .fieldName(name)
                        .fieldValue(value)
                        .build();
                paperCardFieldRepository.save(newField);
            } else {
                PaperCardField existingField = existingFieldMap.get(fieldId);
                if (existingField == null) {
                    throw new NoSuchElementException("필드를 찾을 수 없습니다. ID: " + fieldId);
                }

                existingField.setFieldName(name);
                existingField.setFieldValue(value);
            }
        }

        // 기존 그룹
        List<GroupMember> existingMembers = groupMemberRepository.findByPaperCardId(cardId);
        Set<Integer> existingGroupIds = existingMembers.stream()
                .map(gm -> gm.getGroup().getId())
                .collect(Collectors.toSet());

        // 요청된 그룹
        Set<Integer> requestGroupIds = request.getGroups().stream()
                .map(UpdatePaperCardRequest.Group::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 그룹 삭제
        Set<Integer> toDeleteGroup = new HashSet<>(existingGroupIds);
        toDeleteGroup.removeAll(requestGroupIds);
        if (!toDeleteGroup.isEmpty()) {
            groupMemberRepository.deleteByPaperCardIdAndGroup_IdIn(cardId, toDeleteGroup);
        }

        // 그룹 추가
        Set<Integer> toAddGroup = new HashSet<>(requestGroupIds);
        toAddGroup.removeAll(existingGroupIds);
        for (Integer groupId : toAddGroup) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new NoSuchElementException("그룹을 찾을 수 없습니다. ID: " + groupId));

            groupMemberRepository.save(GroupMember.builder()
                    .user(user)
                    .group(group)
                    .paperCard(paperCard)
                    .build());
        }
    }

    @Override
    @Transactional
    public void deletePaperCard(Integer userId, Integer cardId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        PaperCard paperCard = paperCardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException(ERR_CARD_NOT_FOUND));

        if (!paperCard.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        paperCardRepository.delete(paperCard);
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Integer userId, Integer cardId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        PaperCard paperCard = paperCardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException(ERR_CARD_NOT_FOUND));

        if (!paperCard.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(ERR_FORBIDDEN);
        }

        paperCard.toggleFavorite();
        return paperCard.getFavorite();
    }
}
