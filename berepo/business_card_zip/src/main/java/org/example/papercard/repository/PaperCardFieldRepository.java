package org.example.papercard.repository;

import org.example.papercard.entity.PaperCardField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PaperCardFieldRepository extends JpaRepository<PaperCardField, Integer> {

    List<PaperCardField> findByPaperCard_IdOrderByIdAsc(Integer cardId);

    List<PaperCardField> findByPaperCard_Id(Integer cardId);

    void deleteByIdIn(Set<Integer> toDelete);
}
