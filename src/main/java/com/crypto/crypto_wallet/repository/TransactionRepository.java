package com.crypto.crypto_wallet.repository;

import com.crypto.crypto_wallet.entity.Transaction;
import com.crypto.crypto_wallet.entity.TransactionStatus;
import com.crypto.crypto_wallet.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Transaction> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, TransactionType type);
    List<Transaction> findByTypeAndStatus(TransactionType type, TransactionStatus status);
    List<Transaction> findAllByOrderByCreatedAtDesc();
    boolean existsByUserIdAndTypeAndStatus(Long userId, TransactionType type, TransactionStatus status);
}
