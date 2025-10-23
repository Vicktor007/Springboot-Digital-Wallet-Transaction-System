package com.vic.walletservice.Repositories;

import com.vic.walletservice.Models.WalletEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletEventLogRepository extends JpaRepository<WalletEventLog, Long> {
    List<WalletEventLog> findBySentFalse();

    boolean existsByTransactionId(String transactionId);

    Optional<WalletEventLog> findByTransactionId(String transactionId);
}
