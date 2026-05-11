package com.crypto.crypto_wallet.service;

import com.crypto.crypto_wallet.dto.UserResponse;
import java.util.List;

public interface ReferralService {
    String getReferralCode(Long userId);
    List<UserResponse> getReferredUsers(Long userId);
    long getReferralCount(Long userId);
}
