package com.vic.historyservice.Repository;

import com.vic.historyservice.Models.Transaction_events;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionEventsRepository extends JpaRepository<Transaction_events, String> {
    List<Transaction_events> findByWalletId(String walletId);

    List<Transaction_events> findByUserId(String userId);

    boolean existsByTransactionId(String transactionId);
}
