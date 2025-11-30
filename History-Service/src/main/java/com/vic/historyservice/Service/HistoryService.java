package com.vic.historyservice.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vic.historyservice.Dtos.WalletEvent;
import com.vic.historyservice.Models.TransactionEvents;
import com.vic.historyservice.Repository.TransactionEventsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Service
public class HistoryService {

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);
    private final TransactionEventsRepository eventsRepository;

    public HistoryService(TransactionEventsRepository eventsRepository) {
        this.eventsRepository = eventsRepository;
    }

    /**
     * this fetches all activities of a particular wallet by its identity number
     */
    public List<TransactionEvents> getWalletHistory(String walletId) {
        List<TransactionEvents> walletHistory = new ArrayList<>();

        if (walletId == null || walletId.trim().isEmpty()) {
            return walletHistory;
        }

        try {
            walletHistory = eventsRepository.findByWalletId(walletId);
        } catch (Exception e) {
            log.error("Error getting wallet {} history:  {}", walletId, e.getMessage());
        }
        return walletHistory;
    }

    /**
     * this fetches all activities of a particular user by its identity number
     */
    public List<TransactionEvents> getUserHistory(String userId) {

        List<TransactionEvents> userHistory = new ArrayList<>();

        if (userId == null || userId.trim().isEmpty()) {
            return userHistory;
        }


        try {
            userHistory = eventsRepository.findByUserId(userId);
        }  catch (Exception e) {
            log.error("Error getting user {} history:  {}", userId, e.getMessage());
        }
        return userHistory;
    }

    public void saveWalletEvent(WalletEvent walletEvent)  {

        if (eventsRepository.existsByTransactionId(walletEvent.transactionId())) {
            log.info("Wallet event already exists :: {}", walletEvent.transactionId());
            return;
        }

        try {
            TransactionEvents newTransactionEvents = new TransactionEvents();
            newTransactionEvents.setEvent_type(walletEvent.eventType());
            newTransactionEvents.setWalletId(walletEvent.walletId());
            newTransactionEvents.setUserId(walletEvent.userId());
            newTransactionEvents.setAmount(walletEvent.amount());
            newTransactionEvents.setTransactionId(walletEvent.transactionId());
            newTransactionEvents.setTransactionType(walletEvent.transactionType());
            newTransactionEvents.setEventData(walletEvent);
            newTransactionEvents.setCreatedAt(LocalDateTime.now());

            eventsRepository.save(newTransactionEvents);
        } catch (Exception e) {
            log.error("Error while saving wallet event notification :: {}", walletEvent, e);
        }

    }

}
