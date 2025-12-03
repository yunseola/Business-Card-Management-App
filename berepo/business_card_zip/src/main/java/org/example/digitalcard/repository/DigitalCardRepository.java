package org.example.digitalcard.repository;

import org.example.digitalcard.entity.DigitalCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DigitalCardRepository extends JpaRepository<DigitalCard, Integer> {
//    Optional<DigitalCard> findByIdAndUserId(Integer id, Integer userId);
//
//    List<DigitalCard> findAllByUserId(Integer userId);
//
//    Optional<DigitalCard> findByIdAndIsDigital(Integer id, Boolean isDigital);

    Optional<DigitalCard> findByNameAndPhoneAndCompany(String name, String phone, String company);

    Optional<DigitalCard> findFirstByPhoneOrderByIdAsc(String phone);

}
