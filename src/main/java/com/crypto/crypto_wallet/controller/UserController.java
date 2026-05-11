package com.crypto.crypto_wallet.controller;

import com.crypto.crypto_wallet.dto.ApiResponse;
import com.crypto.crypto_wallet.dto.UserResponse;
import com.crypto.crypto_wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(userId)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        Long userId = userService.getByEmail(userDetails.getUsername()).getId();
        String fullName = body.get("fullName");
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",
                userService.updateProfile(userId, fullName)));
    }
}
