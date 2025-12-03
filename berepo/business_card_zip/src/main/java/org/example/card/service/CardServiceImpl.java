package org.example.card.service;

import lombok.RequiredArgsConstructor;
import org.example.card.dto.CardDuplicateCheckRequest;
import org.example.card.dto.CardDuplicateCheckResponse;
import org.example.card.dto.CardListResponse;
import org.example.digitalcard.entity.DigitalCard;
import org.example.digitalcard.entity.Relation;
import org.example.digitalcard.repository.DigitalCardRepository;
import org.example.digitalcard.repository.RelationRepository;
import org.example.oauth.repository.UserRepository;
import org.example.papercard.entity.PaperCard;
import org.example.papercard.repository.PaperCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final String ERR_USER_NOT_FOUND = "존재하지 않는 사용자입니다.";

    private final UserRepository userRepository;
    private final PaperCardRepository paperCardRepository;
    private final DigitalCardRepository digitalCardRepository;
    private final RelationRepository relationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CardListResponse> getCardList(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        List<PaperCard> paperCards = paperCardRepository.findAllByUser_Id(userId);

        List<CardListResponse> paperCardResponses = paperCards.stream()
                .map(card -> CardListResponse.builder()
                        .cardId(card.getId())
                        .name(card.getName())
                        .phone(card.getPhone())
                        .company(card.getCompany())
                        .position(card.getPosition())
                        .email(card.getEmail())
                        .imageUrl(card.getImage1Url())
                        .digital(false)
                        .confirmed(false)
                        .favorite(card.getFavorite())
                        .createdAt(card.getCreatedAt())
                        .updatedAt(card.getUpdatedAt())
                        .build())
                .toList();

        List<Relation> relations = relationRepository.findAllWithCardByUserId(userId);

        List<CardListResponse> digitalCardResponses = relations.stream()
                .map(relation -> {
                    var card = relation.getCard(); // fetch join으로 이미 함께 로딩

                    return CardListResponse.builder()
                            .cardId(card.getId())
                            .name(card.getName())
                            .phone(card.getPhone())
                            .company(card.getCompany())
                            .position(card.getPosition())
                            .email(card.getEmail())
                            .imageUrl(card.getImageUrlHorizontal())
                            .digital(true)
                            .confirmed(card.getConfirmed())
                            .favorite(relation.getIsFavorite())
                            .createdAt(relation.getCreatedAt())
                            .updatedAt(card.getUpdatedAt())
                            .build();
                })
                .toList();

        return Stream.concat(paperCardResponses.stream(), digitalCardResponses.stream())
                .sorted(Comparator.comparing(CardListResponse::getName))
                .collect(Collectors.toList());
    }

    @Override
    public CardDuplicateCheckResponse checkDuplicateCard(Integer userId, CardDuplicateCheckRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ERR_USER_NOT_FOUND));

        if (request.getName() == null || request.getPhone() == null || request.getCompany() == null) {
            throw new IllegalArgumentException("name, phone, company은(는) 필수 입력값입니다.");
        }

        // 1. 디지털 명함 완전 일치
        DigitalCard exactDigital = digitalCardRepository
                .findByNameAndPhoneAndCompany(request.getName(), request.getPhone(), request.getCompany())
                .orElse(null);

        if (exactDigital != null) {
            return CardDuplicateCheckResponse.builder()
                    .type(1)
                    .cardId(exactDigital.getId())
                    .build();
        }

        // 2. 종이 명함 완전 일치
        PaperCard exactPaper = paperCardRepository
                .findByUser_IdAndNameAndPhoneAndCompany(userId, request.getName(), request.getPhone(), request.getCompany())
                .orElse(null);

        if (exactPaper != null) {
            return CardDuplicateCheckResponse.builder()
                    .type(2)
                    .cardId(exactPaper.getId())
                    .build();
        }

        // 3. 종이 명함 2개 항목 일치
        List<PaperCard> partialMatches = paperCardRepository
                .findPartialMatchPaperCards(userId, request.getName(), request.getPhone(), request.getCompany());

        if (!partialMatches.isEmpty()) {
            return CardDuplicateCheckResponse.builder()
                    .type(3)
                    .matches(
                            partialMatches.stream()
                                    .map(card -> CardDuplicateCheckResponse.MatchInfo.builder()
                                            .cardId(card.getId())
                                            .name(card.getName())
                                            .position(card.getPosition())
                                            .phone(card.getPhone())
                                            .company(card.getCompany())
                                            .imageUrl(card.getImage1Url())
                                            .build()
                                    )
                                    .toList()
                    )
                    .build();
        }

        // 4. 중복 없음
        return CardDuplicateCheckResponse.builder()
                .type(4)
                .build();
    }
}
