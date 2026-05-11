package com.crypto.crypto_wallet.serviceImpl;

import com.crypto.crypto_wallet.dto.UserResponse;
import com.crypto.crypto_wallet.exception.ResourceNotFoundException;
import com.crypto.crypto_wallet.repository.UserRepository;
import com.crypto.crypto_wallet.service.ReferralService;
import com.crypto.crypto_wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public String getReferralCode(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getReferralCode();
    }

    @Override
    public List<UserResponse> getReferredUsers(Long userId) {
        String referralCode = getReferralCode(userId);
        return userRepository.findAll().stream()
                .filter(u -> referralCode.equals(u.getReferredBy()))
                .map(userService::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getReferralCount(Long userId) {
        return getReferredUsers(userId).size();
    }
}
