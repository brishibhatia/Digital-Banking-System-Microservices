package com.banking.transactionservice.dto;

import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.entity.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private String id;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private TransactionStatus status;
    private TransactionType type;
    private String description;
    private String failureReason;
    private String refernceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
