package com.vic.historyservice.Models;


import com.vic.historyservice.Enums.EventTypes;
import com.vic.historyservice.Enums.TransactionType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Unique ID for this transaction event", example = "tx-event-001")
@Entity
@Table(name = "Transaction_events")
@EntityListeners(AuditingEntityListener.class)
public class Transaction_events {
    @Id
    @GeneratedValue(generator = "uuid")
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "wallet_id", length = 36, nullable = false)
    private String walletId;

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private EventTypes event_type;

    @Column(length = 36, name = "transaction_id")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TransactionType transactionType;


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "event_data")
    private Object eventData;

    public Transaction_events() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public EventTypes getEvent_type() {
        return event_type;
    }

    public void setEvent_type(EventTypes event_type) {
        this.event_type = event_type;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Object getEventData() {
        return eventData;
    }

    public void setEventData(Object eventData) {
        this.eventData = eventData;
    }
}
