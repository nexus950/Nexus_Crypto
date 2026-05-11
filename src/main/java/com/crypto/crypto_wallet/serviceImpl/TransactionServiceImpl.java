package com.crypto.crypto_wallet.serviceImpl;

import com.crypto.crypto_wallet.dto.DepositRequest;
import com.crypto.crypto_wallet.dto.TransactionResponse;
import com.crypto.crypto_wallet.dto.WithdrawRequest;
import com.crypto.crypto_wallet.entity.*;
import com.crypto.crypto_wallet.exception.InsufficientBalanceException;
import com.crypto.crypto_wallet.exception.ResourceNotFoundException;
import com.crypto.crypto_wallet.repository.TransactionRepository;
import com.crypto.crypto_wallet.repository.UserRepository;
import com.crypto.crypto_wallet.repository.WalletRepository;
import com.crypto.crypto_wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse deposit(Long userId, DepositRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Save the deposit as PENDING — balance is NOT credited until admin approves
        Transaction tx = Transaction.builder()
                .user(user)
                .type(TransactionType.DEPOSIT)
                .coinSymbol(request.getCoinSymbol().toUpperCase())
                .amount(request.getAmount())
                .txHash(request.getTxHash())
                .status(TransactionStatus.PENDING)   // awaiting admin approval
                .build();

        transactionRepository.save(tx);
        return toResponse(tx);
    }

    /** Admin-only: approve a pending deposit → credit the wallet and mark COMPLETED */
    @Override
    @Transactional
    public TransactionResponse approveDeposit(Long txId) {
        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (tx.getType() != TransactionType.DEPOSIT)
            throw new IllegalStateException("Transaction is not a deposit");
        if (tx.getStatus() == TransactionStatus.COMPLETED)
            throw new IllegalStateException("Deposit already completed");

        // Credit the user's wallet
        User user = tx.getUser();
        Wallet wallet = walletRepository.findByUserIdAndCoinSymbol(user.getId(), tx.getCoinSymbol())
                .orElseGet(() -> {
                    Wallet w = Wallet.builder()
                            .user(user)
                            .coinSymbol(tx.getCoinSymbol())
                            .build();
                    return walletRepository.save(w);
                });
        wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
        walletRepository.save(wallet);

        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);
        return toResponse(tx);
    }

    @Override
    public List<TransactionResponse> getPendingDeposits() {
        return transactionRepository.findAll().stream()
                .filter(tx -> tx.getType() == TransactionType.DEPOSIT && tx.getStatus() == TransactionStatus.PENDING)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse rejectDeposit(Long txId) {
        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (tx.getType() != TransactionType.DEPOSIT)
            throw new IllegalStateException("Transaction is not a deposit");
        if (tx.getStatus() != TransactionStatus.PENDING)
            throw new IllegalStateException("Deposit is not pending");

        tx.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(tx);
        return toResponse(tx);
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(Long userId, WithdrawRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserIdAndCoinSymbol(userId, request.getCoinSymbol())
                .orElseThrow(() -> new InsufficientBalanceException(
                        "No " + request.getCoinSymbol() + " wallet found"));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .user(user)
                .type(TransactionType.WITHDRAWAL)
                .coinSymbol(request.getCoinSymbol().toUpperCase())
                .amount(request.getAmount())
                .toAddress(request.getToAddress())
                .status(TransactionStatus.PENDING)
                .build();

        transactionRepository.save(tx);
        return toResponse(tx);
    }

    @Override
    public List<TransactionResponse> getTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .coinSymbol(tx.getCoinSymbol())
                .amount(tx.getAmount())
                .txHash(tx.getTxHash())
                .toAddress(tx.getToAddress())
                .status(tx.getStatus())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
