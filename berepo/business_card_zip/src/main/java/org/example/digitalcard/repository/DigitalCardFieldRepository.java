package org.example.digitalcard.repository;

import org.example.digitalcard.entity.DigitalCard;
import org.example.digitalcard.entity.DigitalCardField;
import org.example.papercard.entity.PaperCard;
import org.example.papercard.entity.PaperCardField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DigitalCardFieldRepository extends JpaRepository<DigitalCardField, Integer> {
}
