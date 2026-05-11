package com.crypto.crypto_wallet.repository;

import com.crypto.crypto_wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId);
    Optional<Wallet> findByUserIdAndCoinSymbol(Long userId, String coinSymbol);
}
