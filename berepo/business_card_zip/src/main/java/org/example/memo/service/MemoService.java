package org.example.memo.service;

import org.example.memo.dto.MemoUpdateRequest;
import org.example.oauth.entity.User;

public interface MemoService {

    void createByCard(User user, Object card);

    void updatePaperMemo(Integer cardId, Integer userId, MemoUpdateRequest request);

    void updateDigitalMemo(Integer cardId, Integer userId, MemoUpdateRequest request);
}
