package com.banking.transactionservice.entity;


/*
* transaction lifecycle
* PENDING -> PROCESSSING -> COMPLETED(clean transan)
*                           PENDING_VERIFICATION (suspicious detected)
*                                   -> COMPLETED
*                                   -> FLAGGED (SAGA REFUND)
*                          -> FAILED
*                           -> FLAGGED
*
* */
public enum TransactionStatus {
    PENDING,
    PROCESSING,
    PENDING_VERIFICATION,
    COMPLETED,
    FAILED,
    FLAGGED
}
