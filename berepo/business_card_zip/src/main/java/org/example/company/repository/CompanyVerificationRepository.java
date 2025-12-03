package org.example.company.repository;

import org.example.company.entity.CompanyVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyVerificationRepository
        extends JpaRepository<CompanyVerification, Integer> {

    Optional<CompanyVerification> findByCardId(Integer cardId);
}
