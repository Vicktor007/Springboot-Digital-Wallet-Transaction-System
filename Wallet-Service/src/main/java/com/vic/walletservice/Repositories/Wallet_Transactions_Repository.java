package com.vic.walletservice.Repositories;

import com.vic.walletservice.Models.WalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactions_Repository extends JpaRepository<WalletTransactions, String> {
}
