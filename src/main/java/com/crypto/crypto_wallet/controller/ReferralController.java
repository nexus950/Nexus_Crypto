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
    private final com.crypto.crypto_wallet.service.TransactionService transactionService;

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
        
        List<UserResponse> referredUsers = referralService.getReferredUsers(userId);
        long totalReferrals = referredUsers.size();
        
        long activeReferrals = referredUsers.stream()
                .filter(u -> u.getKycStatus() != null && "VERIFIED".equalsIgnoreCase(u.getKycStatus().name()))
                .count();
                
        com.crypto.crypto_wallet.dto.ReferralSettingsResponse settings = referralService.getSettings();
        java.math.BigDecimal totalEarned = transactionService.getTotalReferralBonus(userId);
        if (totalEarned == null) totalEarned = java.math.BigDecimal.ZERO;
        
        String currentTier = "Bronze";
        int commission = 10;
        int nextTierReq = 10;
        String nextTierName = "Silver";
        
        if (activeReferrals >= 60) {
            currentTier = "Platinum";
            commission = 30;
            nextTierReq = 0;
            nextTierName = "None";
        } else if (activeReferrals >= 30) {
            currentTier = "Gold";
            commission = 25;
            nextTierReq = 60;
            nextTierName = "Platinum";
        } else if (activeReferrals >= 10) {
            currentTier = "Silver";
            commission = 20;
            nextTierReq = 30;
            nextTierName = "Gold";
        }
        
        java.math.BigDecimal spotFees = totalEarned.multiply(java.math.BigDecimal.valueOf(0.65)).setScale(2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal futuresFees = totalEarned.multiply(java.math.BigDecimal.valueOf(0.25)).setScale(2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal stakingBonus = totalEarned.multiply(java.math.BigDecimal.valueOf(0.07)).setScale(2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal conversionBonus = totalEarned.multiply(java.math.BigDecimal.valueOf(0.03)).setScale(2, java.math.RoundingMode.HALF_UP);
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("referralCode", referralService.getReferralCode(userId));
        stats.put("totalReferrals", totalReferrals);
        stats.put("activeReferrals", activeReferrals);
        stats.put("referredUsers", referredUsers);
        stats.put("totalEarned", totalEarned.setScale(2, java.math.RoundingMode.HALF_UP));
        stats.put("currentTier", currentTier);
        stats.put("commissionRate", commission);
        stats.put("nextTierReq", nextTierReq);
        stats.put("nextTierName", nextTierName);
        stats.put("spotFees", spotFees);
        stats.put("futuresFees", futuresFees);
        stats.put("stakingBonus", stakingBonus);
        stats.put("conversionBonus", conversionBonus);
        
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<com.crypto.crypto_wallet.dto.ReferralSettingsResponse>> getRewardSettings() {
        return ResponseEntity.ok(ApiResponse.ok(referralService.getSettings()));
    }
}
