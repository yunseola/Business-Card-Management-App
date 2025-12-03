package org.example.memo.service;

import lombok.RequiredArgsConstructor;
import org.example.digitalcard.entity.DigitalCard;
import org.example.digitalcard.repository.DigitalCardRepository;
import org.example.memo.dto.MemoUpdateRequest;
import org.example.memo.entity.Memo;
import org.example.memo.repository.MemoRepository;
import org.example.oauth.entity.User;
import org.example.papercard.entity.PaperCard;
import org.example.papercard.repository.PaperCardRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MemoServiceImpl implements MemoService {

    private final MemoRepository memoRepository;
    private final PaperCardRepository paperCardRepository;
    private final DigitalCardRepository digitalCardRepository;

    @Override
    @Transactional
    public void createByCard(User user, Object card) {
        Memo.MemoBuilder builder = Memo.builder()
                .user(user)
                .relationship("")
                .personality("")
                .workStyle("")
                .meetingNotes("")
                .etc("")
                .summary("");

        if (card instanceof PaperCard paperCard) {
            builder.paperCard(paperCard);
        } else if (card instanceof DigitalCard digitalCard) {
            builder.digitalCard(digitalCard);
        } else {
            throw new IllegalArgumentException("지원하지 않는 명함 타입입니다: " + card.getClass().getSimpleName());
        }

        memoRepository.save(builder.build());
    }

    @Override
    @Transactional
    public void updatePaperMemo(Integer cardId, Integer userId, MemoUpdateRequest request) {
        PaperCard paperCard = paperCardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("종이 명함을 찾을 수 없습니다."));

        if (!paperCard.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        Memo memo = memoRepository.findByUser_IdAndPaperCard_Id(userId, cardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 종이 명함에 대한 메모가 존재하지 않습니다."));

        memo.updateMemo(request);
    }

    @Override
    @Transactional
    public void updateDigitalMemo(Integer cardId, Integer userId, MemoUpdateRequest request) {
        digitalCardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("디지털 명함을 찾을 수 없습니다."));

        Memo memo = memoRepository.findByUser_IdAndDigitalCard_Id(userId, cardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 디지털 명함에 대한 메모가 존재하지 않습니다."));

        memo.updateMemo(request);
    }
}
