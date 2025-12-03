package org.example.papercard.repository;

import org.example.papercard.entity.PaperCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaperCardRepository extends JpaRepository<PaperCard, Integer> {

    List<PaperCard> findAllByUser_Id(Integer userId);

    Optional<PaperCard> findByUser_IdAndNameAndPhoneAndCompany(
            Integer userId,
            String name,
            String phone,
            String company
    );

    @Query("""
            SELECT p
            FROM PaperCard p
            WHERE p.user.id = :userId AND (
                (p.name = :name AND p.phone = :phone) OR
                (p.name = :name AND p.company = :company) OR
                (p.phone = :phone AND p.company = :company)
            )
            """)
    List<PaperCard> findPartialMatchPaperCards(Integer userId, String name, String phone, String company);

    @Query("""
            SELECT p
            FROM PaperCard p
            WHERE p.name = :name
                AND p.phone = :phone
                AND p.company = :company
                AND p.user.id <> :userId
            """)
    List<PaperCard> findByNameAndPhoneAndCompanyExceptUser(
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("company") String company,
            @Param("userId") Integer userId
    );

    Optional<PaperCard> findByUser_IdAndPhone(Integer userId, String phone);

    Optional<PaperCard> findFirstByPhoneOrderByIdAsc(String phone);

    List<PaperCard> findAllByUser_IdAndPhone(Integer userId, String phone);
}
