package com.vic.historyservice.kafkaConsumerTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vic.historyservice.Dtos.WalletEvent;
import com.vic.historyservice.Enums.EventTypes;
import com.vic.historyservice.Enums.TransactionType;
import com.vic.historyservice.Service.HistoryService;
import com.vic.historyservice.kafka.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

    @Mock
    private HistoryService historyService;

    @Mock
    private Acknowledgment acknowledgment;

    private KafkaConsumer kafkaConsumer;
    private WalletEvent testWalletEvent;
    private ConsumerRecord<String, WalletEvent> testRecord;

    @BeforeEach
    void setUp() {
        kafkaConsumer = new KafkaConsumer(historyService);

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
        kafkaConsumer.consumeWalletCreationEventNotification(testRecord, acknowledgment);

        verify(historyService).saveWalletEvent(testWalletEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldHandleExceptionGracefully() throws JsonProcessingException {
        doThrow(new RuntimeException("Database down"))
                .when(historyService).saveWalletEvent(testWalletEvent);

        kafkaConsumer.consumeWalletCreationEventNotification(testRecord, acknowledgment);

        verify(historyService).saveWalletEvent(testWalletEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldHandleNullEventGracefully() throws JsonProcessingException {
        ConsumerRecord<String, WalletEvent> nullRecord =
                new ConsumerRecord<>("wallet_event_topic", 0, 0, "key", null);

        kafkaConsumer.consumeWalletCreationEventNotification(nullRecord, acknowledgment);

        verify(historyService).saveWalletEvent(null);
        verify(acknowledgment).acknowledge();
    }
}
