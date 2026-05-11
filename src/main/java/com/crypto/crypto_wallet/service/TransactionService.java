package com.crypto.crypto_wallet.service;

import com.crypto.crypto_wallet.dto.DepositRequest;
import com.crypto.crypto_wallet.dto.TransactionResponse;
import com.crypto.crypto_wallet.dto.WithdrawRequest;
import java.util.List;

public interface TransactionService {
    TransactionResponse deposit(Long userId, DepositRequest request);
    TransactionResponse withdraw(Long userId, WithdrawRequest request);
    List<TransactionResponse> getTransactions(Long userId);
    TransactionResponse approveDeposit(Long txId);   // admin use
    List<TransactionResponse> getPendingDeposits();
    TransactionResponse rejectDeposit(Long txId);
}
