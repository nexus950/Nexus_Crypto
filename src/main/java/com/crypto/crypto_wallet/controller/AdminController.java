package com.crypto.crypto_wallet.controller;

import com.crypto.crypto_wallet.dto.*;
import com.crypto.crypto_wallet.service.KycService;
import com.crypto.crypto_wallet.service.TransactionService;
import com.crypto.crypto_wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final KycService kycService;
    private final PasswordEncoder passwordEncoder;

    // --- User Management ---

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Fetched all users", userService.getAllUsers()));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User created successfully", userService.createUser(request, passwordEncoder)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", userService.updateUser(id, request)));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User status toggled", userService.toggleUserStatus(id)));
    }

    // --- Deposit Management ---

    @GetMapping("/deposits")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getPendingDeposits() {
        return ResponseEntity.ok(ApiResponse.ok("Fetched pending deposits", transactionService.getPendingDeposits()));
    }

    @PostMapping("/deposits/{id}/approve")
    public ResponseEntity<ApiResponse<TransactionResponse>> approveDeposit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Deposit approved", transactionService.approveDeposit(id)));
    }

    @PostMapping("/deposits/{id}/reject")
    public ResponseEntity<ApiResponse<TransactionResponse>> rejectDeposit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Deposit rejected", transactionService.rejectDeposit(id)));
    }

    // --- KYC Management ---

    @GetMapping("/kyc")
    public ResponseEntity<ApiResponse<List<KycResponse>>> getPendingKyc() {
        return ResponseEntity.ok(ApiResponse.ok("Fetched pending KYC documents", kycService.getPendingKyc()));
    }

    @PostMapping("/kyc/{id}/approve")
    public ResponseEntity<ApiResponse<KycResponse>> approveKyc(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("KYC approved", kycService.approveKyc(id)));
    }

    @PostMapping("/kyc/{id}/reject")
    public ResponseEntity<ApiResponse<KycResponse>> rejectKyc(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String reason = payload.get("reason");
        return ResponseEntity.ok(ApiResponse.ok("KYC rejected", kycService.rejectKyc(id, reason)));
    }
}
