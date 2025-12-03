package org.example.call.repository;

import org.example.digitalcard.entity.DigitalCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<DigitalCard, Long> {
    Optional<DigitalCard> findByPhone(String phone);
}
