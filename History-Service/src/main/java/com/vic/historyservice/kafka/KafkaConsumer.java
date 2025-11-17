package com.vic.historyservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vic.historyservice.Dtos.WalletEvent;
import com.vic.historyservice.Models.TransactionEvents;
import com.vic.historyservice.Repository.TransactionEventsRepository;
import com.vic.historyservice.Service.HistoryService;
import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * This kafka consumer processes events coming from wallet service ensures idempotency.
 * This service handles duplicate transaction prevention by checking
 * if a transaction with the same ID already exists in the system.
 * it manually acknowledges events after it has been saved.
 *
 */

@Service
public class KafkaConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final HistoryService historyService;

    public KafkaConsumer( HistoryService historyService) {
        this.historyService = historyService;
    }

    @RetryableTopic(
            attempts = "${kafka.retry.attempts}",
            backoff = @Backoff(
                    delayExpression = "${kafka.retry.backoff.delay}",
                    multiplierExpression = "${kafka.retry.backoff.multiplier}"
            ),
            autoCreateTopics = "${kafka.retry.auto-create-topics}",
            include = {DataAccessException.class, TimeoutException.class}
    )
    @KafkaListener(
            topics = "${kafka.consumer.topic}",
            groupId = "${kafka.consumer.group-id}"
    )
    public void consumeWalletCreationEventNotification(ConsumerRecord<String, WalletEvent> record, Acknowledgment acknowledgment ) throws JsonProcessingException {

        WalletEvent walletEvent = record.value();
        try {
            log.info("Consuming wallet event notification :: {}", walletEvent.toString());

            historyService.saveWalletEvent(walletEvent);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error while saving wallet event notification :: {}", walletEvent, e);
            throw e;
        }
    }



}

