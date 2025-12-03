package org.example.mycard.service;

import lombok.RequiredArgsConstructor;
import org.example.awss3.S3Service;
import org.example.common.QrCodeGenerator;
import org.example.digitalcard.entity.DigitalCard;
import org.example.digitalcard.entity.DigitalCardField;
import org.example.digitalcard.entity.Relation;
import org.example.digitalcard.repository.DigitalCardFieldRepository;
import org.example.digitalcard.repository.DigitalCardRepository;
import org.example.digitalcard.repository.RelationRepository;
import org.example.group.entity.GroupMember;
import org.example.group.repository.GroupMemberRepository;
import org.example.memo.entity.Memo;
import org.example.memo.repository.MemoRepository;
import org.example.mycard.dto.*;
import org.example.mycard.entity.CompanyHistory;
import org.example.mycard.repository.CompanyHistoryRepository;
import org.example.mycard.repository.MyCardRepository;
import org.example.notification.entity.Notification;
import org.example.notification.repository.NotificationRepository;
import org.example.oauth.entity.User;
import org.example.oauth.repository.UserRepository;
import org.example.papercard.entity.PaperCard;
import org.example.papercard.repository.PaperCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyCardServiceImpl implements MyCardService {

    private static final String ERR_USER_NOT_FOUND = "존재하지 않는 사용자입니다.";

    private final UserRepository userRepository;
    private final PaperCardRepository paperCardRepository;
    private final DigitalCardFieldRepository digitalCardFieldRepository;
    private final MyCardRepository myCardRepository;
    private final CompanyHistoryRepository companyHistoryRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final RelationRepository relationRepository;
    private final MemoRepository memoRepository;
    private final S3Service s3Service;
    private final QrCodeGenerator qrCodeGenerator;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Integer registerMyCard(Integer userId, MyCardRegisterRequest request, MultipartFile customImage, MultipartFile imageUrlHorizontal, MultipartFile imageUrlVertical) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        if (request.getName() == null || request.getName().trim().isEmpty()
                || request.getPhone() == null || request.getPhone().trim().isEmpty()
                || request.getCompany() == null || request.getCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("필수 입력값이 누락되었습니다.");
        }

        // 커스텀 사진
        String imageUrl = (customImage != null && !customImage.isEmpty()) ? s3Service.upload(customImage, "digital") : null;
        String imageUrlHorizontalUrl = (imageUrlHorizontal != null && !imageUrlHorizontal.isEmpty())
                ? s3Service.upload(imageUrlHorizontal, "digital") : null;
        String imageUrlVerticalUrl = (imageUrlVertical != null && !imageUrlVertical.isEmpty())
                ? s3Service.upload(imageUrlVertical, "digital") : null;

        // 공유 토큰
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (myCardRepository.existsByShareToken(token));

        // share_url 생성
        String shareUrl = "https://i13e201.p.ssafy.io/api/cards/share/" + token;
//      String shareUrl = "https://localhost:8443/api/cards/digital/share/" + token;

        // QR 코드 생성 후 S3 업로드
        String qrCodeUrl;
        try {
            // QR 코드 생성
            byte[] qrBytes = qrCodeGenerator.generateQRCodeImage(shareUrl, 250, 250);

            // S3에 업로드
            qrCodeUrl = s3Service.upload(qrBytes, "qrcodes", "image/png");

        } catch (Exception e) {
            throw new RuntimeException("QR 코드 생성 실패", e);
        }

        DigitalCard digitalCard = DigitalCard.builder()
                .user(user)
                .name(request.getName())
                .phone(request.getPhone())
                .company(request.getCompany())
                .position(request.getPosition())
                .email(request.getEmail())
                .customImageUrl(imageUrl)
                .imageUrlHorizontal(imageUrlHorizontalUrl)
                .imageUrlVertical(imageUrlVerticalUrl)
                .backgroundImageNum(request.getBackgroundImageNum())
                .fontColor(true)
                .digital(true)
                .confirmed(false)
                .shareUrl(shareUrl)
                .qrCodeUrl(qrCodeUrl)
                .shareToken(token)
                .build();

        DigitalCard saved = myCardRepository.save(digitalCard);

        if (request.getFields() != null) {
            for (MyCardRegisterRequest.FieldDto field : request.getFields()) {
                DigitalCardField fieldEntity = DigitalCardField.builder()
                        .digitalCard(saved)
                        .fieldName(field.getFieldName())
                        .fieldValue(field.getFieldValue())
                        .fieldOrder(field.getOrder())
                        .build();
                digitalCardFieldRepository.save(fieldEntity);
            }
        }

        CompanyHistory history = CompanyHistory.builder()
                .digitalCard(saved)
                .company(saved.getCompany())
                .confirmed(false)
                .build();

        companyHistoryRepository.save(history);

        migratePaperCardRelationsToDigitalCard(user, saved);

        return saved.getId();
    }

    private void migratePaperCardRelationsToDigitalCard(User user, DigitalCard digitalCard) {
        List<PaperCard> paperCards = paperCardRepository.findByNameAndPhoneAndCompanyExceptUser(
                digitalCard.getName(), digitalCard.getPhone(), digitalCard.getCompany(), user.getId()
        );

        for (PaperCard paperCard : paperCards) {
            // 관계 생성
            Relation relation = Relation.builder()
                    .user(paperCard.getUser())
                    .giver(user)
                    .card(digitalCard)
                    .isFavorite(paperCard.getFavorite())
                    .createdAt(paperCard.getCreatedAt())
                    .build();
            relationRepository.save(relation);

            // 메모 계승
            Memo memo = memoRepository.findByPaperCard_Id(paperCard.getId());
            if (memo != null) {
                memo.linkToDigitalCard(digitalCard);
                memoRepository.save(memo);
            }

            // 그룹 계승
            List<GroupMember> groupMembers = groupMemberRepository.findByPaperCardId(paperCard.getId());
            for (GroupMember groupMember : groupMembers) {
                groupMember.convertPaperToDigital(digitalCard);
            }

            // 알림 저장
            Notification notification = Notification.builder()
                    .user(paperCard.getUser())
                    .card(digitalCard)
                    .message(digitalCard.getName() + "님의 디지털 명함으로 자동 연결되었습니다.")
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

            // 종이 명함 삭제
            paperCardRepository.delete(paperCard);
        }
    }

    // 명함 목록
    @Override
    @Transactional(readOnly = true)
    public List<MyCardListItemResponse> getMyCardList(Integer userId) {
        List<DigitalCard> cards = myCardRepository.findAllByUser_Id(userId);

        return cards.stream()
                .map(card -> MyCardListItemResponse.builder()
                        .cardId(card.getId())
                        .imageUrlHorizontal(card.getImageUrlHorizontal())
                        .confirmed(card.getConfirmed())
                        .build()
                )
                .collect(Collectors.toList());
    }

    // 상세 조회
    @Override
    @Transactional(readOnly = true)
    public MyCardDetailResponse getMyCardDetail(Integer userId, Integer cardId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        DigitalCard digitalCard = myCardRepository.findByIdAndUser_Id(cardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 명함이 없거나 접근 권한이 없습니다."));

        List<MyCardDetailResponse.FieldDto> fields = digitalCard.getFields()
                .stream()
                .map(field -> MyCardDetailResponse.FieldDto.builder()
                        .fieldId(field.getId())
                        .fieldName(field.getFieldName())
                        .fieldValue(field.getFieldValue())
                        .fieldOrder(field.getFieldOrder())
                        .build())
                .collect(Collectors.toList());

        List<MyCardDetailResponse.CompanyHistoryDto> companyHistories = digitalCard.getCompanyHistories()
                .stream()
                .sorted(Comparator.comparing(CompanyHistory::getChangedAt).reversed())
                .map(history -> MyCardDetailResponse.CompanyHistoryDto.builder()
                        .company(history.getCompany())
                        .confirmed(history.getConfirmed())
                        .changedAt(history.getChangedAt())
                        .build())
                .collect(Collectors.toList());

        return MyCardDetailResponse.builder()
                .name(digitalCard.getName())
                .phone(digitalCard.getPhone())
                .company(digitalCard.getCompany())
                .position(digitalCard.getPosition())
                .email(digitalCard.getEmail())
                .imageUrlHorizontal(digitalCard.getImageUrlHorizontal())
                .imageUrlVertical(digitalCard.getImageUrlVertical())
                .backgroundImageNum(digitalCard.getBackgroundImageNum())
                .fontColor(digitalCard.getFontColor())
                .confirmed(digitalCard.getConfirmed())
                .shareUrl(digitalCard.getShareUrl())
                .qrCodeUrl(digitalCard.getQrCodeUrl())
                .createdAt(digitalCard.getCreatedAt())
                .updateAt(digitalCard.getUpdatedAt())
                .fields(fields)
                .companyHistories(companyHistories)
                .build();
    }

    // 명함 수정
    @Override
    @Transactional
    public void updateMyCard(Integer userId, Integer cardId, MyCardUpdateRequest request, MultipartFile customImage, MultipartFile imageHorizontal, MultipartFile imageVertical) {
        DigitalCard digitalCard = myCardRepository.findByIdAndUser_Id(cardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 명함이 없거나 접근 권한이 없습니다."));

        String customImageUrl = (customImage != null && !customImage.isEmpty()) ?
                s3Service.upload(customImage, "digital") : null;

        String imageUrlHorizontal = (imageHorizontal != null && !imageHorizontal.isEmpty()) ?
                s3Service.upload(imageHorizontal, "digital") : null;

        String imageUrlVertical = (imageVertical != null && !imageVertical.isEmpty()) ?
                s3Service.upload(imageVertical, "digital") : null;

        String oldCompany = digitalCard.getCompany();
        String oldPhone = digitalCard.getPhone();
        String oldName = digitalCard.getName();

        digitalCard.updateInfo(
                request.getName(),
                request.getPhone(),
                request.getCompany(),
                request.getPosition(),
                request.getEmail(),
                request.getBackgroundImageNum(),
                request.getFontColor(),
                customImageUrl,
                imageUrlHorizontal,
                imageUrlVertical
        );

        // 필드 업데이트
        // 기존 필드
        List<DigitalCardField> existingFields = new ArrayList<>(digitalCard.getFields());
        Map<Integer, DigitalCardField> existingFieldMap = existingFields.stream()
                .filter(f -> f.getId() != null)
                .collect(Collectors.toMap(DigitalCardField::getId, f -> f));

        List<DigitalCardField> updatedFields = new ArrayList<>();

        if (request.getFields() != null) {
            for (MyCardUpdateRequest.FieldDto f : request.getFields()) {
                if (f.getFieldId() != null && existingFieldMap.containsKey(f.getFieldId())) {
                    // 기존 필드 → 수정
                    DigitalCardField existing = existingFieldMap.get(f.getFieldId());
                    existing.setFieldName(f.getFieldName());
                    existing.setFieldValue(f.getFieldValue());
                    existing.setFieldOrder(f.getFieldOrder() == null ? 0 : f.getFieldOrder());
                    updatedFields.add(existing);

                    existingFieldMap.remove(f.getFieldId()); // 남은 건 삭제 대상
                } else {
                    // 새로운 필드 → 추가
                    DigitalCardField newField = DigitalCardField.builder()
                            .fieldName(f.getFieldName())
                            .fieldValue(f.getFieldValue())
                            .fieldOrder(f.getFieldOrder() == null ? 0 : f.getFieldOrder())
                            .digitalCard(digitalCard)
                            .build();
                    updatedFields.add(newField);
                }
            }
        }

        // 요청에 없는 기존 필드 → 삭제
        for (DigitalCardField toDelete : existingFieldMap.values()) {
            digitalCard.getFields().remove(toDelete); // 연관관계 끊기
        }

        // 연관관계 재설정
        digitalCard.getFields().clear();
        digitalCard.getFields().addAll(updatedFields);

        // 변경된 정보를 기반으로 알림 생성
        User owner = digitalCard.getUser();  // 명함 주인
        String userName = owner.getName();

        // 명함을 공유 받은 유저들 (relation.user)
        List<User> relatedUsers = relationRepository.findUsersByCardId(cardId);

        if (!Objects.equals(oldCompany, request.getCompany())) {
            companyHistoryRepository.save(
                    CompanyHistory.builder()
                            .digitalCard(digitalCard)
                            .company(request.getCompany())
                            .confirmed(false)
                            .build()
            );

            sendNotificationToUsers(relatedUsers, digitalCard, userName + "님의 회사 정보가 '" + oldCompany + "'에서 '" + request.getCompany() + "'(으)로 변경되었습니다.");
        }

        if (!Objects.equals(oldPhone, request.getPhone())) {
            sendNotificationToUsers(relatedUsers, digitalCard, userName + "님의 연락처 정보가 '" + oldPhone + "'에서 '" + request.getPhone() + "'(으)로 변경되었습니다.");
        }

        if (!Objects.equals(oldName, request.getName())) {
            sendNotificationToUsers(relatedUsers, digitalCard, "명함 주인의 이름이 '" + oldName + "'에서 '" + request.getName() + "'(으)로 변경되었습니다.");
        }

        myCardRepository.save(digitalCard);
    }

    // 명함 삭제
    @Override
    @Transactional
    public void deleteMyCard(Integer userId, Integer cardId) {
        DigitalCard digitalCard = myCardRepository.findByIdAndUser_Id(cardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 명함이 없거나 권한이 없습니다."));

        myCardRepository.delete(digitalCard);
    }

    private void sendNotificationToUsers(List<User> users, DigitalCard card, String message) {
        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .card(card)
                        .message(message)
                        .isRead(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }
}
