package com.vic.walletservice.Services;

import com.vic.walletservice.Kafka.KafkaProducer;
import com.vic.walletservice.Models.WalletEventLog;
import com.vic.walletservice.Repositories.WalletEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * this fetches kafka's unsent messages from database and retries them again every 5 hours
 */
@Component
public class WalletEventRetryScheduler {

    private final Logger log = LoggerFactory.getLogger(WalletEventRetryScheduler.class);
    private final WalletEventLogRepository walletEventLogRepository;
    private final KafkaProducer kafkaProducer;


    public WalletEventRetryScheduler(WalletEventLogRepository walletEventLogRepository, KafkaProducer kafkaProducer) {
        this.walletEventLogRepository = walletEventLogRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 5) // every 5 hours
    public void resendFailedEvents() {
        List<WalletEventLog> pendingEvents = walletEventLogRepository.findBySentFalse();

        if (pendingEvents.isEmpty()) return;

        log.info("Resending {} unsent events", pendingEvents.size());

        for (WalletEventLog walletEventLog : pendingEvents) {
            try {
                kafkaProducer.sendEvent(walletEventLog.toWalletEvent());
                log.info("Successfully resent event with body {}", walletEventLog.getId());
                walletEventLog.setSent(true);
                walletEventLog.setRetryCount(walletEventLog.getRetryCount() + 1);
                walletEventLogRepository.save(walletEventLog);
            } catch (Exception e) {
                log.error("Failed to send event with body {}", walletEventLog.getId(), e);
            }
        }
    }
}
