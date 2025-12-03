package org.example.digitalcard.repository;

import io.micrometer.observation.ObservationFilter;
import jakarta.transaction.Transactional;
import org.example.digitalcard.entity.Relation;
import org.example.oauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RelationRepository extends JpaRepository<Relation, Integer> {
    boolean existsByUserIdAndCardId(Integer userId, Integer cardId);

    Optional<Relation> findByUserIdAndCardId(Integer userId, Integer cardId);

    @Modifying
    @Transactional
    @Query("delete from Relation r where r.user.id = :userId and r.card.id = :cardId")
    void deleteByUserIdAndCardId(@Param("userId") Integer userId,
                                 @Param("cardId") Integer cardId);

    @Query("""
        select r
        from Relation r
        join fetch r.card c
        where r.user.id = :userId
    """)
    List<Relation> findAllWithCardByUserId(@Param("userId") Integer userId);

    List<Relation> findAllByCardId(Integer cardId);

    @Query("SELECT r.user FROM Relation r WHERE r.card.id = :cardId")
    List<User> findUsersByCardId(@Param("cardId") Integer cardId);

    Optional<Relation> findByUserIdAndCardPhone(Integer userId, String phone);

    Optional<Relation>  findByUser_IdAndCard_Id(Integer userId, Integer cardId);

    List<Relation> findAllByUserIdAndCardPhoneOrderByIdDesc(Integer userId, String phone);

}

