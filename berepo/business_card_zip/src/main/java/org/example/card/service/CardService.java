package org.example.card.service;

import org.example.card.dto.CardDuplicateCheckRequest;
import org.example.card.dto.CardDuplicateCheckResponse;
import org.example.card.dto.CardListResponse;

import java.util.List;

public interface CardService {

    List<CardListResponse> getCardList(Integer userId);

    CardDuplicateCheckResponse checkDuplicateCard(Integer userId, CardDuplicateCheckRequest request);
}
