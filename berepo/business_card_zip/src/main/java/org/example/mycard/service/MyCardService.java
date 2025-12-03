package org.example.mycard.service;

import org.example.mycard.dto.MyCardDetailResponse;
import org.example.mycard.dto.MyCardListItemResponse;
import org.example.mycard.dto.MyCardRegisterRequest;
import org.example.mycard.dto.MyCardUpdateRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MyCardService {

    // 명함 생성
    Integer registerMyCard(Integer userId, MyCardRegisterRequest request, MultipartFile customImage, MultipartFile imageUrlHorizontal, MultipartFile imageUrlVertical);

    // 명함 목록
    List<MyCardListItemResponse> getMyCardList(Integer userId);

    // 명함 상세 조회
    MyCardDetailResponse getMyCardDetail(Integer userId, Integer cardId);

    // 명함 수정
    void updateMyCard(Integer userId, Integer cardId, MyCardUpdateRequest request, MultipartFile customImage, MultipartFile imageUrlHorizontal, MultipartFile imageUrlVertical);

    // 명함 삭제
    void deleteMyCard(Integer userId, Integer cardId);
}
