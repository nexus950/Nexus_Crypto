package com.crypto.crypto_wallet.serviceImpl;

import com.crypto.crypto_wallet.dto.TradeRequest;
import com.crypto.crypto_wallet.dto.TradeResponse;
import com.crypto.crypto_wallet.entity.*;
import com.crypto.crypto_wallet.exception.BadRequestException;
import com.crypto.crypto_wallet.exception.InsufficientBalanceException;
import com.crypto.crypto_wallet.exception.ResourceNotFoundException;
import com.crypto.crypto_wallet.repository.TradeOrderRepository;
import com.crypto.crypto_wallet.repository.UserRepository;
import com.crypto.crypto_wallet.repository.WalletRepository;
import com.crypto.crypto_wallet.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final TradeOrderRepository tradeOrderRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TradeResponse placeOrder(Long userId, TradeRequest request) {
        if (request.getPair() == null || !request.getPair().contains("/")) {
            throw new BadRequestException("Invalid trading pair. Format: BASE/QUOTE (e.g. BTC/USDT)");
        }

        String[] parts     = request.getPair().split("/");
        String baseCoin    = parts[0];   // e.g. BTC
        String quoteCoin   = parts[1];   // e.g. USDT

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getOrderType() == OrderType.LIMIT && request.getPrice() == null) {
            throw new BadRequestException("Price is required for LIMIT orders");
        }

        // Deduct balance from the spending wallet
        if (request.getSide() == OrderSide.BUY) {
            // Buyer spends quote coin (USDT)
            BigDecimal cost = request.getOrderType() == OrderType.MARKET
                    ? request.getAmount().multiply(BigDecimal.valueOf(1)) // market uses actual price — simplified
                    : request.getAmount().multiply(request.getPrice());

            Wallet quoteWallet = walletRepository.findByUserIdAndCoinSymbol(userId, quoteCoin)
                    .orElseThrow(() -> new InsufficientBalanceException("No " + quoteCoin + " wallet found"));

            if (quoteWallet.getBalance().compareTo(cost) < 0) {
                throw new InsufficientBalanceException("Insufficient " + quoteCoin + " balance");
            }
            quoteWallet.setBalance(quoteWallet.getBalance().subtract(cost));
            walletRepository.save(quoteWallet);

            // Credit base coin
            Wallet baseWallet = walletRepository.findByUserIdAndCoinSymbol(userId, baseCoin)
                    .orElseGet(() -> {
                        Wallet w = Wallet.builder().user(user).coinSymbol(baseCoin).build();
                        return walletRepository.save(w);
                    });
            baseWallet.setBalance(baseWallet.getBalance().add(request.getAmount()));
            walletRepository.save(baseWallet);

        } else {
            // Seller spends base coin
            Wallet baseWallet = walletRepository.findByUserIdAndCoinSymbol(userId, baseCoin)
                    .orElseThrow(() -> new InsufficientBalanceException("No " + baseCoin + " wallet found"));

            if (baseWallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient " + baseCoin + " balance");
            }
            baseWallet.setBalance(baseWallet.getBalance().subtract(request.getAmount()));
            walletRepository.save(baseWallet);

            // Credit quote coin
            BigDecimal proceeds = request.getOrderType() == OrderType.MARKET
                    ? request.getAmount()
                    : request.getAmount().multiply(request.getPrice());

            Wallet quoteWallet = walletRepository.findByUserIdAndCoinSymbol(userId, quoteCoin)
                    .orElseGet(() -> {
                        Wallet w = Wallet.builder().user(user).coinSymbol(quoteCoin).build();
                        return walletRepository.save(w);
                    });
            quoteWallet.setBalance(quoteWallet.getBalance().add(proceeds));
            walletRepository.save(quoteWallet);
        }

        TradeOrder order = TradeOrder.builder()
                .user(user)
                .pair(request.getPair())
                .side(request.getSide())
                .orderType(request.getOrderType())
                .amount(request.getAmount())
                .price(request.getPrice())
                .executedPrice(request.getOrderType() == OrderType.MARKET ? request.getAmount() : request.getPrice())
                .status(OrderStatus.FILLED)
                .executedAt(LocalDateTime.now())
                .build();

        tradeOrderRepository.save(order);
        return toResponse(order);
    }

    @Override
    public List<TradeResponse> getTradeHistory(Long userId) {
        return tradeOrderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TradeResponse cancelOrder(Long userId, Long orderId) {
        TradeOrder order = tradeOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        tradeOrderRepository.save(order);
        return toResponse(order);
    }

    private TradeResponse toResponse(TradeOrder o) {
        return TradeResponse.builder()
                .id(o.getId())
                .pair(o.getPair())
                .side(o.getSide())
                .orderType(o.getOrderType())
                .amount(o.getAmount())
                .price(o.getPrice())
                .executedPrice(o.getExecutedPrice())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .executedAt(o.getExecutedAt())
                .build();
    }
}
