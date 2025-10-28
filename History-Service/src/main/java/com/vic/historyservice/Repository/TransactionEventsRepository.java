package com.vic.historyservice.Repository;

import com.vic.historyservice.Models.TransactionEvents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionEventsRepository extends JpaRepository<TransactionEvents, String> {
    List<TransactionEvents> findByWalletId(String walletId);

    List<TransactionEvents> findByUserId(String userId);

    boolean existsByTransactionId(String transactionId);
}
