package com.banking.transactionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotBlank(message = "Sender account no. is required")
    private String senderAccountNumber;

    @NotBlank(message = "Receiver account no. is required")
    private String receiverAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Ampunt must be in positive")
    private BigDecimal amount;

    private String description;

}
