package org.example.papercard.repository;

import org.example.papercard.entity.ImagesHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageHistoryRepository extends JpaRepository<ImagesHistory, Integer> {

    List<ImagesHistory> findByPaperCard_IdOrderByUploadedAtDesc(Integer cardId);
}
