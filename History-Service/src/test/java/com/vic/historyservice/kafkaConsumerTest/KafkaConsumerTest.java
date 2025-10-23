package com.vic.historyservice.kafkaConsumerTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vic.historyservice.Dtos.WalletEvent;
import com.vic.historyservice.Enums.EventTypes;
import com.vic.historyservice.Enums.TransactionType;
import com.vic.historyservice.Models.Transaction_events;
import com.vic.historyservice.Repository.TransactionEventsRepository;
import com.vic.historyservice.kafka.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

    @Mock
    private TransactionEventsRepository transactionEventsRepository;

    @Mock
    private Acknowledgment acknowledgment;

    @Captor
    private ArgumentCaptor<Transaction_events> transactionEventsCaptor;

    private KafkaConsumer kafkaConsumer;

    private WalletEvent testWalletEvent;
    private ConsumerRecord<String, WalletEvent> testRecord;

    @BeforeEach
    void setUp() {
        kafkaConsumer = new KafkaConsumer(transactionEventsRepository);

        testWalletEvent = new WalletEvent(
                EventTypes.WALLET_CREATED,
                "wallet-123",
                "user-456",
                new BigDecimal("100.00"),
                "sender-789",
                "receiver-012",
                "txn-001",
                TransactionType.CREATE_WALLET,
                LocalDateTime.now()
        );

        testRecord = new ConsumerRecord<>("wallet_event_topic", 0, 0, "key", testWalletEvent);
    }

    @Test
    void shouldProcessWalletEventSuccessfully() throws JsonProcessingException {
        // Given
        when(transactionEventsRepository.existsByTransactionId("txn-001")).thenReturn(false);

        // When
        kafkaConsumer.consumeWalletCreationEventNotification(testRecord, acknowledgment);

        // Then
        verify(transactionEventsRepository).existsByTransactionId("txn-001");
        verify(transactionEventsRepository).save(transactionEventsCaptor.capture());
        verify(acknowledgment).acknowledge();

        Transaction_events savedEvent = transactionEventsCaptor.getValue();
        assertAll(
                () -> assertEquals(EventTypes.WALLET_CREATED, savedEvent.getEvent_type()),
                () -> assertEquals("wallet-123", savedEvent.getWalletId()),
                () -> assertEquals("user-456", savedEvent.getUserId()),
                () -> assertEquals(new BigDecimal("100.00"), savedEvent.getAmount()),
                () -> assertEquals("txn-001", savedEvent.getTransactionId()),
                () -> assertEquals(TransactionType.CREATE_WALLET, savedEvent.getTransactionType()),
                () -> assertEquals(testWalletEvent, savedEvent.getEventData()),
                () -> assertNotNull(savedEvent.getCreatedAt())
        );
    }

    @Test
    void shouldAcknowledgeAndSkipWhenEventAlreadyExists() throws JsonProcessingException {
        // Given
        when(transactionEventsRepository.existsByTransactionId("txn-001")).thenReturn(true);

        // When
        kafkaConsumer.consumeWalletCreationEventNotification(testRecord, acknowledgment);

        // Then
        verify(transactionEventsRepository).existsByTransactionId("txn-001");
        verify(transactionEventsRepository, never()).save(any(Transaction_events.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldHandleNullValuesGracefully() throws JsonProcessingException {
        // Given
        WalletEvent walletEventWithNulls = new WalletEvent(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        ConsumerRecord<String, WalletEvent> recordWithNulls =
                new ConsumerRecord<>("wallet_event_topic", 0, 0, "key", walletEventWithNulls);

        when(transactionEventsRepository.existsByTransactionId(null)).thenReturn(false);

        // When
        kafkaConsumer.consumeWalletCreationEventNotification(recordWithNulls, acknowledgment);

        // Then
        verify(transactionEventsRepository).existsByTransactionId(null);
        verify(transactionEventsRepository).save(transactionEventsCaptor.capture());
        verify(acknowledgment).acknowledge();

        Transaction_events savedEvent = transactionEventsCaptor.getValue();
        assertAll(
                () -> assertNull(savedEvent.getEvent_type()),
                () -> assertNull(savedEvent.getWalletId()),
                () -> assertNull(savedEvent.getUserId()),
                () -> assertNull(savedEvent.getAmount()),
                () -> assertNull(savedEvent.getTransactionId()),
                () -> assertNull(savedEvent.getTransactionType()),
                () -> assertEquals(walletEventWithNulls, savedEvent.getEventData()),
                () -> assertNotNull(savedEvent.getCreatedAt())
        );
    }

    @Test
    void shouldHandleRepositoryException() throws JsonProcessingException {
        // Given
        when(transactionEventsRepository.existsByTransactionId("txn-001")).thenReturn(false);
        when(transactionEventsRepository.save(any(Transaction_events.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        kafkaConsumer.consumeWalletCreationEventNotification(testRecord, acknowledgment);

        // Then
        verify(transactionEventsRepository).existsByTransactionId("txn-001");
        verify(transactionEventsRepository).save(any(Transaction_events.class));
        // Acknowledgment should still be called even if save fails
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldHandleExistsCheckException() throws JsonProcessingException {
        // Given
        when(transactionEventsRepository.existsByTransactionId("txn-001"))
                .thenThrow(new RuntimeException("Database unavailable"));

        // When
        kafkaConsumer.consumeWalletCreationEventNotification(testRecord, acknowledgment);

        // Then
        verify(transactionEventsRepository).existsByTransactionId("txn-001");
        verify(transactionEventsRepository, never()).save(any(Transaction_events.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldProcessDifferentEventTypes() throws JsonProcessingException {
        // Given
        WalletEvent fundedEvent = new WalletEvent(
                EventTypes.WALLET_FUNDED,
                "wallet-123",
                "user-456",
                new BigDecimal("50.00"),
                "sender-789",
                "receiver-012",
                "txn-fund-001",
                TransactionType.FUND,
                LocalDateTime.now()
        );
        ConsumerRecord<String, WalletEvent> fundedRecord =
                new ConsumerRecord<>("wallet_event_topic", 0, 0, "key", fundedEvent);

        when(transactionEventsRepository.existsByTransactionId("txn-fund-001")).thenReturn(false);

        // When
        kafkaConsumer.consumeWalletCreationEventNotification(fundedRecord, acknowledgment);

        // Then
        verify(transactionEventsRepository).existsByTransactionId("txn-fund-001");
        verify(transactionEventsRepository).save(transactionEventsCaptor.capture());
        verify(acknowledgment).acknowledge();

        Transaction_events savedEvent = transactionEventsCaptor.getValue();
        assertEquals(EventTypes.WALLET_FUNDED, savedEvent.getEvent_type());
        assertEquals(TransactionType.FUND, savedEvent.getTransactionType());
    }

    @Test
    void shouldHandleZeroAmount() throws JsonProcessingException {
        // Given
        WalletEvent zeroAmountEvent = new WalletEvent(
                EventTypes.WALLET_CREATED,
                "wallet-123",
                "user-456",
                BigDecimal.ZERO,
                "sender-789",
                "receiver-012",
                "txn-zero-001",
                TransactionType.CREATE_WALLET,
                LocalDateTime.now()
        );
        ConsumerRecord<String, WalletEvent> zeroAmountRecord =
                new ConsumerRecord<>("wallet_event_topic", 0, 0, "key", zeroAmountEvent);

        when(transactionEventsRepository.existsByTransactionId("txn-zero-001")).thenReturn(false);

        // When
        kafkaConsumer.consumeWalletCreationEventNotification(zeroAmountRecord, acknowledgment);

        // Then
        verify(transactionEventsRepository).save(transactionEventsCaptor.capture());

        Transaction_events savedEvent = transactionEventsCaptor.getValue();
        assertEquals(BigDecimal.ZERO, savedEvent.getAmount());
    }
}
