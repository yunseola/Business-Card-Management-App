package org.example.mycard.repository;

import org.example.digitalcard.entity.DigitalCard;
import org.example.mycard.entity.CompanyHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyCardRepository extends JpaRepository<DigitalCard, Integer> {

    @EntityGraph(attributePaths = {"fields", "companyHistories"})
    Optional<DigitalCard> findByIdAndUser_Id(Integer cardId, Integer userId);

    List<DigitalCard> findAllByUser_Id(Integer userId);

    Optional<DigitalCard> findByShareToken(String token);

    boolean existsByShareToken(String token);
}
