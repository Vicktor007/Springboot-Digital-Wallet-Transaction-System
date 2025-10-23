package com.vic.historyservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vic.historyservice.Dtos.WalletEvent;
import com.vic.historyservice.Models.Transaction_events;
import com.vic.historyservice.Repository.TransactionEventsRepository;
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

    private final TransactionEventsRepository transactionEventsRepository;


    public KafkaConsumer(TransactionEventsRepository transactionEventsRepository) {
        this.transactionEventsRepository = transactionEventsRepository;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "false",
            include = {DataAccessException.class, TimeoutException.class}
    )
    @KafkaListener(topics = "wallet_event_topic", groupId = "wallet_events")
    public void consumeWalletCreationEventNotification(ConsumerRecord<String, WalletEvent> record, Acknowledgment acknowledgment ) throws JsonProcessingException {

        WalletEvent walletEvent = record.value();
        try {
            log.info("Consuming wallet event notification :: {}", walletEvent.toString());

            // checking for idempotency
            if (transactionEventsRepository.existsByTransactionId(walletEvent.transactionId())) {
                log.info("Wallet event already exists :: {}", walletEvent.transactionId());
                acknowledgment.acknowledge();
                return;
            }

            Transaction_events newTransactionEvents = new Transaction_events();
            newTransactionEvents.setEvent_type(walletEvent.eventType());
            newTransactionEvents.setWalletId(walletEvent.walletId());
            newTransactionEvents.setUserId(walletEvent.userId());
            newTransactionEvents.setAmount(walletEvent.amount());
            newTransactionEvents.setTransactionId(walletEvent.transactionId());
            newTransactionEvents.setTransactionType(walletEvent.transactionType());
            newTransactionEvents.setEventData(walletEvent);
            newTransactionEvents.setCreatedAt(LocalDateTime.now());

            transactionEventsRepository.save(newTransactionEvents);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error while saving wallet event notification :: {}", walletEvent, e);
           acknowledgment.acknowledge();
        }
    }



}

