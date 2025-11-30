package com.vic.walletservice.KafkaTest;

import com.vic.walletservice.Dtos.WalletEvent;
import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Enums.TransactionType;
import com.vic.walletservice.Kafka.KafkaProducer;
import com.vic.walletservice.Models.WalletEventLog;
import com.vic.walletservice.Services.WalletEventLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerTest {

    @Mock
    private KafkaTemplate<String, WalletEvent> kafkaTemplate;

    @Mock
    private WalletEventLogService walletEventLogService;

    @Captor
    private ArgumentCaptor<WalletEventLog> walletEventLogCaptor;

    @Captor
    private ArgumentCaptor<WalletEvent> walletEventCaptor;

    private KafkaProducer kafkaProducer;

    private WalletEvent testWalletEvent;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        kafkaProducer = new KafkaProducer(kafkaTemplate, walletEventLogService);
        testTimestamp = LocalDateTime.now();

        testWalletEvent = new WalletEvent(
                EventTypes.WALLET_FUNDED,
                "wallet-123",
                "user-456",
                new BigDecimal("100.50"),
                "sender-789",
                "receiver-012",
                "txn-001",
                TransactionType.FUND,
                testTimestamp
        );
    }

    @Test
    void shouldSendEventSuccessfully() {
        CompletableFuture future =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(testWalletEvent);

        verify(kafkaTemplate).send("wallet_event_topic", testWalletEvent);
        verify(walletEventLogService, never()).saveEventLog(any(WalletEvent.class));
    }

    @Test
    void shouldSaveToEventLog_WhenKafkaSendFails() {
        CompletableFuture<SendResult<String, WalletEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(testWalletEvent);

        // Wait a bit for the async completion to be processed
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(kafkaTemplate).send("wallet_event_topic", testWalletEvent);
        verify(walletEventLogService).saveEventLog(walletEventCaptor.capture());

        WalletEvent savedEvent = walletEventCaptor.getValue();
        assertThat(savedEvent.eventType()).isEqualTo(EventTypes.WALLET_FUNDED);
        assertThat(savedEvent.walletId()).isEqualTo("wallet-123");
        assertThat(savedEvent.userId()).isEqualTo("user-456");
        assertThat(savedEvent.amount()).isEqualTo(new BigDecimal("100.50"));
        assertThat(savedEvent.senderId()).isEqualTo("sender-789");
        assertThat(savedEvent.receiverId()).isEqualTo("receiver-012");
        assertThat(savedEvent.transactionId()).isEqualTo("txn-001");
        assertThat(savedEvent.timeStamp()).isEqualTo(testTimestamp);
    }

    @Test
    void shouldHandleDifferentEventTypes() {
        WalletEvent walletCreatedEvent = new WalletEvent(
                EventTypes.WALLET_CREATED,
                "wallet-new",
                "user-new",
                BigDecimal.ZERO,
                "system",
                "wallet-new",
                "create-txn",
                TransactionType.CREATE_WALLET,
                testTimestamp
        );

        CompletableFuture<SendResult<String, WalletEvent>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(walletCreatedEvent);

        verify(kafkaTemplate).send("wallet_event_topic", walletCreatedEvent);
        verify(walletEventLogService, never()).saveEventLog(any(WalletEvent.class));
    }

    @Test
    void shouldHandleFailedTransferEvent() {
        WalletEvent failedTransferEvent = new WalletEvent(
                EventTypes.TRANSFER_FAILED,
                "wallet-123",
                "user-456",
                new BigDecimal("50.00"),
                "wallet-123",
                "wallet-999",
                "transfer-fail-txn",
                TransactionType.TRANSFER_OUT,
                testTimestamp
        );

        CompletableFuture<SendResult<String, WalletEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka timeout"));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(failedTransferEvent);

        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(walletEventLogService).saveEventLog(walletEventCaptor.capture());

        WalletEvent savedEvent = walletEventCaptor.getValue();
        assertThat(savedEvent.eventType()).isEqualTo(EventTypes.TRANSFER_FAILED);
        assertThat(savedEvent.transactionId()).isEqualTo("transfer-fail-txn");
    }

    @Test
    void shouldSaveEventLog_WhenKafkaSendFails_WithNullTransactionId() {
        WalletEvent eventWithNullTransactionId = new WalletEvent(
                EventTypes.WALLET_FUNDED,
                "wallet-123",
                "user-456",
                new BigDecimal("100.50"),
                "sender-789",
                "receiver-012",
                null, // null transaction ID
                TransactionType.FUND,
                testTimestamp
        );

        CompletableFuture<SendResult<String, WalletEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(eventWithNullTransactionId);

        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(kafkaTemplate).send("wallet_event_topic", eventWithNullTransactionId);
        verify(walletEventLogService).saveEventLog(walletEventCaptor.capture());

        WalletEvent savedEvent = walletEventCaptor.getValue();
        assertThat(savedEvent.transactionId()).isNull();
    }

    @Test
    void shouldNotSaveEventLog_WhenKafkaSendSucceeds() {
        CompletableFuture<SendResult<String, WalletEvent>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        kafkaProducer.sendEvent(testWalletEvent);

        verify(kafkaTemplate).send("wallet_event_topic", testWalletEvent);
        verify(walletEventLogService, never()).saveEventLog(any(WalletEvent.class));
    }

    @Test
    void shouldHandleMultipleFailedEvents() {
        CompletableFuture<SendResult<String, WalletEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        when(kafkaTemplate.send(anyString(), any(WalletEvent.class))).thenReturn(future);

        // Send multiple events
        kafkaProducer.sendEvent(testWalletEvent);

        WalletEvent secondEvent = new WalletEvent(
                EventTypes.WALLET_CREATED,
                "wallet-456",
                "user-789",
                BigDecimal.ZERO,
                "system",
                "wallet-456",
                "txn-002",
                TransactionType.CREATE_WALLET,
                testTimestamp
        );
        kafkaProducer.sendEvent(secondEvent);

        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(walletEventLogService, times(2)).saveEventLog(any(WalletEvent.class));
    }
}