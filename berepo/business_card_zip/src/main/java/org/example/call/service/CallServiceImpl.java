package org.example.call.service;

import lombok.RequiredArgsConstructor;
import org.example.call.dto.CallResponse;
import org.example.digitalcard.entity.DigitalCard;
import org.example.digitalcard.entity.Relation;
import org.example.digitalcard.repository.RelationRepository;
import org.example.memo.entity.Memo;
import org.example.memo.repository.MemoRepository;
import org.example.papercard.entity.PaperCard;
import org.example.papercard.repository.PaperCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CallServiceImpl implements CallService {

    private final MemoRepository memoRepository;
    private final RelationRepository relationRepository;
    private final PaperCardRepository paperCardRepository;

    @Transactional(readOnly = true)
    @Override
    public CallResponse getCardInfoByPhone(String phone, Integer userId) {
        // 1. 디지털 명함(관계) 조회 시도
        List<Relation> relations = relationRepository.findAllByUserIdAndCardPhoneOrderByIdDesc(userId, phone);

        if (!relations.isEmpty()) {
            // 여러 개면 PK(id)가 가장 작은 명함 선택
            Relation oldestRelation = relations.stream()
                    .min(Comparator.comparing(Relation::getId))
                    .orElse(relations.get(0));

            DigitalCard digitalCard = oldestRelation.getCard();

            String summary = memoRepository.findByUser_IdAndDigitalCard_Id(userId, digitalCard.getId())
                    .map(Memo::getSummary)
                    .orElse(null);

            return CallResponse.builder()
                    .name(digitalCard.getName())
                    .phone(digitalCard.getPhone())
                    .company(digitalCard.getCompany())
                    .imageUrlHorizontal(digitalCard.getImageUrlHorizontal())
                    .position(digitalCard.getPosition())
                    .summary(summary)
                    .build();
        }

        // 2. 종이 명함 조회 시도 (본인 소유의 PaperCard)
        List<PaperCard> paperCards = paperCardRepository.findAllByUser_IdAndPhone(userId, phone);
        if (!paperCards.isEmpty()) {
            // 여러 개면 PK(id)가 가장 작은 명함 선택
            PaperCard oldestPaperCard = paperCards.stream()
                    .min(Comparator.comparing(PaperCard::getId))
                    .orElse(paperCards.get(0));

            String summary = memoRepository.findSummaryByPaperCard_IdAndUser_Id(oldestPaperCard.getId(), userId)
                    .orElse(null);

            return CallResponse.builder()
                    .name(oldestPaperCard.getName())
                    .phone(oldestPaperCard.getPhone())
                    .company(oldestPaperCard.getCompany())
                    .imageUrlHorizontal(oldestPaperCard.getImage1Url())
                    .position(oldestPaperCard.getPosition())
                    .summary(summary)
                    .build();
        }

        // 3. 둘 다 없으면 예외 발생
        throw new IllegalArgumentException("해당 전화번호에 대한 접근 권한이 없거나 명함이 존재하지 않습니다: " + phone);
    }
}
