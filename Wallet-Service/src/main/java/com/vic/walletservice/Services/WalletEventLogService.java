package com.vic.walletservice.Services;

import com.vic.walletservice.Dtos.WalletEvent;
import com.vic.walletservice.Kafka.KafkaProducer;
import com.vic.walletservice.Models.WalletEventLog;
import com.vic.walletservice.Repositories.WalletEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletEventLogService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private final WalletEventLogRepository walletEventLogRepository;

    public WalletEventLogService(WalletEventLogRepository walletEventLogRepository) {
        this.walletEventLogRepository = walletEventLogRepository;
    }

    public void saveEventLog(WalletEvent walletEvent) {

        String transactionId = walletEvent.transactionId();
        if (transactionId != null) {
            if (!walletEventLogRepository.existsByTransactionId(transactionId)) {
                WalletEventLog logEntry = getWalletEventLog(walletEvent);

                walletEventLogRepository.save(logEntry);
                log.info("Saved event to database due to Kafka failure: {}", logEntry);
            } else {
                log.warn("Not saving event to database because transactionId is null");
            }

        } else {
            log.info("Successfully sent event");
        }
    }

    /**
     * this saves the unsent messages to the eventlog for later processing
     */
    private static WalletEventLog getWalletEventLog(WalletEvent walletEventRequest) {
        WalletEventLog logEntry = new WalletEventLog();
        logEntry.setEventType(walletEventRequest.eventType());
        logEntry.setWalletId(walletEventRequest.walletId());
        logEntry.setUserId(walletEventRequest.userId());
        logEntry.setAmount(walletEventRequest.amount());
        logEntry.setSenderId(walletEventRequest.senderId());
        logEntry.setReceiverId(walletEventRequest.receiverId());
        logEntry.setTransactionId(walletEventRequest.transactionId());
        logEntry.setTimeStamp(walletEventRequest.timeStamp());
        logEntry.setSent(false);
        return logEntry;
    }
}
