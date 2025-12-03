package org.example.papercard.service;

import org.example.papercard.dto.PaperCardDetailResponse;
import org.example.papercard.dto.RegisterPaperCardRequest;
import org.example.papercard.dto.UpdatePaperCardRequest;
import org.springframework.web.multipart.MultipartFile;

public interface PaperCardService {

    Integer registerPaperCard(Integer userId, RegisterPaperCardRequest request, MultipartFile image1, MultipartFile image2);

    PaperCardDetailResponse getPaperCardDetail(Integer userId, Integer cardId);

    void updatePaperCard(Integer userId, Integer cardId, UpdatePaperCardRequest request, MultipartFile image1, MultipartFile image2);

    void deletePaperCard(Integer userId, Integer cardId);

    boolean toggleFavorite(Integer userId, Integer cardId);
}
