package com.vic.walletservice.Repositories;

import com.vic.walletservice.Models.Wallet_transactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Wallet_Transactions_Repository extends JpaRepository<Wallet_transactions, String> {
}
