package org.example.mycard.repository;

import org.example.digitalcard.entity.DigitalCard;
import org.example.mycard.entity.CompanyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CompanyHistoryRepository extends JpaRepository<CompanyHistory, Integer> {

    List<CompanyHistory> findByDigitalCard_Id(Integer cardId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update CompanyHistory ch
              set ch.confirmed = true,
                  ch.changedAt = :now
            where ch.digitalCard.id = :cardId
              and (ch.confirmed = false or ch.confirmed is null)
           """)
    int markConfirmedByCardId(@Param("cardId") Integer cardId,
                              @Param("now") LocalDateTime now);
}