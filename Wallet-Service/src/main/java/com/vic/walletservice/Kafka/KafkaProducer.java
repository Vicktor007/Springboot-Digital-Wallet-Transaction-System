package com.vic.walletservice.Kafka;

import com.vic.walletservice.Dtos.WalletEvent;
import com.vic.walletservice.Models.WalletEventLog;
import com.vic.walletservice.Repositories.WalletEventLogRepository;
import com.vic.walletservice.Services.WalletEventLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * this sends the event messages to the consumer for processing
 */
@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private static final String TOPIC = "wallet_event_topic";

    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;
    private final WalletEventLogService walletEventLogService;

    public KafkaProducer(KafkaTemplate<String, WalletEvent> kafkaTemplate,
                         WalletEventLogService walletEventLogService) {
        this.kafkaTemplate = kafkaTemplate;
        this.walletEventLogService = walletEventLogService;
    }

    public void sendEvent(WalletEvent walletEventRequest) {
        logger.info("Sending event with body {}", walletEventRequest);

        var future = kafkaTemplate.send(TOPIC, walletEventRequest);

        future.whenComplete((result, ex) -> {
            if (ex == null) {

                logger.info("Successfully sent event with body {}", walletEventRequest);
            } else {

                logger.error("Failed to send event with body {}", walletEventRequest, ex);

                walletEventLogService.saveEventLog(walletEventRequest);

            }
        });
    }

}
