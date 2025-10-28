package com.vic.walletservice.Services;


import com.vic.walletservice.Dtos.WalletEvent;
import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Enums.TransactionStatus;
import com.vic.walletservice.Enums.TransactionType;
import com.vic.walletservice.Exceptions.GlobalExceptionHandler;
import com.vic.walletservice.Exceptions.UserNotFoundException;
import com.vic.walletservice.Exceptions.WalletNotFoundException;
import com.vic.walletservice.Kafka.KafkaProducer;
import com.vic.walletservice.Mappers.WalletMapper;
import com.vic.walletservice.Models.Wallet;
import com.vic.walletservice.Models.WalletTransactions;
import com.vic.walletservice.Repositories.WalletRepository;
import com.vic.walletservice.Repositories.WalletTransactions_Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * this service controls the logic of saving and fetching information
 */

@Service
public class walletService {

    private final WalletRepository walletRepository;
    private final WalletTransactions_Repository transactionsRepository;
    private final KafkaProducer kafkaProducer;

    private final Logger log = LoggerFactory.getLogger(walletService.class);


    public walletService(WalletRepository walletRepository, WalletTransactions_Repository transactionsRepository, KafkaProducer kafkaProducer) {
        this.walletRepository = walletRepository;
        this.transactionsRepository = transactionsRepository;

        this.kafkaProducer = kafkaProducer;
    }

    /**
     * this creates a new wallet with a user identification number and sends the event message eventually
     */
    @Transactional
    public String createWallet(String userId) {

        if (userId == null || userId.isEmpty()) {
            throw new GlobalExceptionHandler.GlobalException("userId is required");
        }

        Wallet newWallet = WalletMapper.toModel(userId);


        Wallet savedWallet = walletRepository.save(newWallet);

        sendKafkaEvent(
                new WalletEvent(
                        EventTypes.WALLET_CREATED,
                        savedWallet.getId(),
                        userId,
                        savedWallet.getBalance(),
                        "",
                        "",
                        "",
                        TransactionType.CREATE_WALLET,
                        savedWallet.getCreatedAt()
                )
        );

        return savedWallet.getId();

    }

    /**
     * this funds a wallet with a user identification number and walletId and sends the event message eventually whether successful or not
     */
    public TransactionStatus fundWallet(String walletId, String userId, BigDecimal amount) {

        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));


        WalletTransactions transactions = new WalletTransactions();
        transactions.setWallet(wallet);
        transactions.setAmount(amount);
        transactions.setReceiverId(walletId);
        transactions.setSenderId(walletId);
        transactions.setCreatedAt(LocalDateTime.now());
        transactions.setType(TransactionType.FUND);

        TransactionStatus status = TransactionStatus.FAILED;

        if (Objects.equals(userId, wallet.getUserId())) {
            try {
                wallet.setBalance(wallet.getBalance().add(amount));

                walletRepository.save(wallet);

                status = TransactionStatus.COMPLETED;
            } catch (Exception e) {
                log.error("Error while funding wallet", e);

            }
        }
        transactions.setStatus(status);
       WalletTransactions savedTransaction = transactionsRepository.save(transactions);

        sendKafkaEvent(
                new WalletEvent(
                        (status == TransactionStatus.COMPLETED) ? EventTypes.WALLET_FUNDED : EventTypes.WALLET_FUNDING_FAILED,
                        walletId,
                        userId,
                        amount,
                        walletId,
                        walletId,
                        savedTransaction.getId(),
                        savedTransaction.getType(),
                        wallet.getUpdatedAt()
                )
        );
        return status;
    }

    /**
     * this processes funds transfer between two wallets, saves the transaction information and sends event message eventually if it was successful or not
     */
    public TransactionStatus transferFunds(String fromWalletId, String fromUserId, String toWalletId, BigDecimal amount) {
        if (fromWalletId.equals(toWalletId)) {
            throw new GlobalExceptionHandler.GlobalException("Cannot transfer funds to the same wallet");
        }

        String firstId = fromWalletId.compareTo(toWalletId) < 0 ? fromWalletId : toWalletId;
        String secondId = fromWalletId.compareTo(toWalletId) < 0 ? toWalletId : fromWalletId;

        Wallet first = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        Wallet second = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        Wallet fromWallet = fromWalletId.equals(firstId) ? first : second;
        Wallet toWallet = fromWalletId.equals(firstId) ? second : first;

        WalletTransactions transactionsFrom = new WalletTransactions();
        transactionsFrom.setWallet(fromWallet);
        transactionsFrom.setAmount(amount);
        transactionsFrom.setSenderId(fromWalletId);
        transactionsFrom.setReceiverId(toWalletId);
        transactionsFrom.setCreatedAt(LocalDateTime.now());
        transactionsFrom.setType(TransactionType.TRANSFER_OUT);

        WalletTransactions transactionTo = new WalletTransactions();
        transactionTo.setWallet(toWallet);
        transactionTo.setAmount(amount);
        transactionTo.setSenderId(fromWalletId);
        transactionTo.setReceiverId(toWalletId);
        transactionTo.setCreatedAt(LocalDateTime.now());
        transactionTo.setType(TransactionType.TRANSFER_IN);


        TransactionStatus status = TransactionStatus.FAILED;

        if (fromWallet.getBalance().compareTo(amount) >= 0) {
            try {
                fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
                toWallet.setBalance(toWallet.getBalance().add(amount));

                walletRepository.save(fromWallet);
                walletRepository.save(toWallet);

                status = TransactionStatus.COMPLETED;

            } catch (Exception e) {
                log.error("Fund transfer error", e);
            }


            transactionsFrom.setStatus(status);
            transactionsRepository.save(transactionsFrom);

            sendKafkaEvent(
                    new WalletEvent(
                            (status == TransactionStatus.COMPLETED) ? EventTypes.TRANSFER_COMPLETED : EventTypes.TRANSFER_FAILED,
                            fromWalletId,
                            fromUserId,
                            amount,
                            fromWalletId,
                            toWalletId,
                            transactionsFrom.getId(),
                            transactionsFrom.getType(),
                            fromWallet.getUpdatedAt()
                    )
            );
            transactionTo.setStatus(status);
            transactionsRepository.save(transactionTo);

            sendKafkaEvent(
                    new WalletEvent(
                            (status == TransactionStatus.COMPLETED) ? EventTypes.TRANSFER_COMPLETED : EventTypes.TRANSFER_FAILED,
                            toWalletId,
                            toWallet.getUserId(),
                            amount,
                            fromWalletId,
                            toWalletId,
                            transactionTo.getId(),
                            transactionTo.getType(),
                            toWallet.getUpdatedAt()
                    )
            );

        }
        return status;
    }

    /**
     * this gets the balance of a wallet and returns it as string
     */
    public String getBalance(String fromWalletId) {
        Wallet wallet = walletRepository.findById(fromWalletId).orElseThrow(() -> new  WalletNotFoundException("Wallet not found"));

        return "Available balance: " + wallet.getBalance();
    }

    /**
     * this gets a user's wallet or wallets if multiple wallets are found. it returns an empty list otherwise
     */
    public List<Wallet> getUserWallets(String userId) {
        List<Wallet> userWallets = null;
        try {
            userWallets = walletRepository.findByUserId(userId);

        } catch (UserNotFoundException e) {
            log.error("User not found", e);
        }
        return userWallets;
    }

    /**
     * this processes the events by the side so as not to block main operations
     */
    @Async
    public void sendKafkaEvent(WalletEvent walletEventRequest) {
        try {
            kafkaProducer.sendEvent(walletEventRequest);
            log.info("Sent event asynchronously {}", walletEventRequest);
        } catch (Exception e) {
            log.error("Error while sending event asynchronously {}", walletEventRequest, e);
        }
    }
}
