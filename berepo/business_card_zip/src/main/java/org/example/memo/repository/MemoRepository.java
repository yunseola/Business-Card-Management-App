package org.example.memo.repository;

import org.example.memo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemoRepository extends JpaRepository<Memo, Integer> {

    Optional<Memo> findByUser_IdAndPaperCard_Id(Integer userId, Integer paperCardId);

    Optional<Memo> findByUser_IdAndDigitalCard_Id(Integer userId, Integer cardId);

    Memo findByPaperCard_Id(Integer cardId);

    @Query("SELECT m.summary FROM Memo m WHERE m.paperCard.id = :paperCardId AND m.user.id = :userId")
    Optional<String> findSummaryByPaperCard_IdAndUser_Id(@Param("paperCardId") Integer paperCardId,
                                                       @Param("userId") Integer userId);

    void deleteByUser_IdAndDigitalCard_Id(Integer userId, Integer cardId);
}
