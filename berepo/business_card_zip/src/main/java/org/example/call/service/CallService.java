package org.example.call.service;

import org.example.call.dto.CallResponse;

public interface CallService {
    CallResponse getCardInfoByPhone(String phone, Integer userId);
}
