package com.crypto.crypto_wallet.controller;

import com.crypto.crypto_wallet.dto.ApiResponse;
import com.crypto.crypto_wallet.dto.UserResponse;
import com.crypto.crypto_wallet.service.ReferralService;
import com.crypto.crypto_wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;
    private final UserService userService;

    @GetMapping("/code")
    public ResponseEntity<ApiResponse<String>> getReferralCode(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(ApiResponse.ok(referralService.getReferralCode(userId)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getReferredUsers(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(ApiResponse.ok(referralService.getReferredUsers(userId)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getByEmail(userDetails.getUsername()).getId();
        Map<String, Object> stats = Map.of(
                "referralCode", referralService.getReferralCode(userId),
                "totalReferrals", referralService.getReferralCount(userId),
                "referredUsers", referralService.getReferredUsers(userId)
        );
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<com.crypto.crypto_wallet.dto.ReferralSettingsResponse>> getRewardSettings() {
        return ResponseEntity.ok(ApiResponse.ok(referralService.getSettings()));
    }
}
