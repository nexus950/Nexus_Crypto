package com.crypto.crypto_wallet.controller;

import com.crypto.crypto_wallet.dto.*;
import com.crypto.crypto_wallet.service.TransactionService;
import com.crypto.crypto_wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DepositRequest request) {
        Long userId = userService.getByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(ApiResponse.ok("Deposit successful",
                transactionService.deposit(userId, request)));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WithdrawRequest request) {
        Long userId = userService.getByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(ApiResponse.ok("Withdrawal submitted",
                transactionService.withdraw(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getTransactions(userId)));
    }

    /** Admin: approve a pending deposit → credits the wallet */
    @PostMapping("/{txId}/approve")
    public ResponseEntity<ApiResponse<TransactionResponse>> approve(
            @PathVariable Long txId) {
        return ResponseEntity.ok(ApiResponse.ok("Deposit approved",
                transactionService.approveDeposit(txId)));
    }
}
