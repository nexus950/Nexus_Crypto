package com.crypto.crypto_wallet.service;

import com.crypto.crypto_wallet.dto.DepositRequest;
import com.crypto.crypto_wallet.dto.TransactionResponse;
import com.crypto.crypto_wallet.dto.WithdrawRequest;
import java.util.List;

public interface TransactionService {
    TransactionResponse deposit(Long userId, DepositRequest request);
    TransactionResponse withdraw(Long userId, WithdrawRequest request);
    List<TransactionResponse> getTransactions(Long userId);

    // Admin: deposits
    TransactionResponse approveDeposit(Long txId);
    List<TransactionResponse> getPendingDeposits();
    TransactionResponse rejectDeposit(Long txId, String reason);

    // Admin: withdrawals
    List<TransactionResponse> getPendingWithdrawals();
    TransactionResponse approveWithdrawal(Long txId);
    TransactionResponse rejectWithdrawal(Long txId, String reason);

    // Withdrawal limit
    java.math.BigDecimal getDailyWithdrawalLimit(Long userId);
    java.math.BigDecimal getDailyWithdrawalRemaining(Long userId);
}
