package com.vic.walletservice.Models;

import com.vic.walletservice.Dtos.WalletEvent;
import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Enums.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_event_log")
public class WalletEventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EventTypes eventType;

    @Column(length = 100)
    private String walletId;

    @Column(length = 100)
    private String userId;

    private BigDecimal amount;

    @Column(length = 100)
    private String senderId;

    @Column(length = 100)
    private String receiverId;

    @Column(length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TransactionType  transactionType;


    private LocalDateTime timeStamp;

    private boolean sent = false;

    private int retryCount = 0;


    private LocalDateTime createdAt = LocalDateTime.now();

    public WalletEvent toWalletEvent() {
        return new WalletEvent(
                eventType,
                walletId,
                userId,
                amount,
                senderId,
                receiverId,
                transactionId,
                transactionType,
                timeStamp
        );}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventTypes getEventType() {
        return eventType;
    }

    public void setEventType(EventTypes eventType) {
        this.eventType = eventType;
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
