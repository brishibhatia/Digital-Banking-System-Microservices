package com.banking.transactionservice.service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionStatus;
import com.banking.transactionservice.entity.TransactionType;
import com.banking.transactionservice.event.TransactionInitiatedEvent;
import com.banking.transactionservice.repository.TransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final AccountServiceClient accountServiceClient;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";


/*
* SAGA STEP -1
* deducts from the sender account
* saves transaction as processing
* publish event to kafka for fraud check
* Returns
* */

    public TransactionResponse transfer(@Valid TransactionRequest transactionRequest) {
        log.info("SAGA STEP -1 starting transfer {} -> {} amount : {}",
                transactionRequest.getSenderAccountNumber(),
                transactionRequest.getReceiverAccountNumber(),
                transactionRequest.getAmount() );

        accountServiceClient.deductBalance(
                transactionRequest.getSenderAccountNumber(),
                transactionRequest.getAmount()
        );

        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(transactionRequest.getSenderAccountNumber());
        transaction.setReceiverAccountNumber(transactionRequest.getReceiverAccountNumber());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setDescription(transactionRequest.getDescription());
        transaction.setRefernceNumber(generateReferenceNumber());

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction saved as processing : {}", savedTransaction.getId());

        TransactionInitiatedEvent transactionInitiatedEvent = new TransactionInitiatedEvent();
        transactionInitiatedEvent.setTransactionId(savedTransaction.getId());
        transactionInitiatedEvent.setSenderAccountNumber(transactionRequest.getSenderAccountNumber());
        transactionInitiatedEvent.setReceiverAccountNumber(transactionRequest.getReceiverAccountNumber());
        transactionInitiatedEvent.setAmount(transactionRequest.getAmount());
        transactionInitiatedEvent.setDescription(transactionRequest.getDescription());


        kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC , savedTransaction.getId() , transactionInitiatedEvent);

        log.info("SAGA STEP -2 Transaction initiated event published: {}", savedTransaction.getId());

        return maptoResponse(savedTransaction);

    }

    private String generateReferenceNumber(){
        String referenceNumber;
        do{
            referenceNumber = UUID.randomUUID().toString();
        }while(transactionRepository.existsByReferenceNumber(referenceNumber));
        return referenceNumber;
    }

    private TransactionResponse maptoResponse(Transaction transaction){
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setId(transaction.getId());
        transactionResponse.setSenderAccountNumber(transaction.getSenderAccountNumber());
        transactionResponse.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setDescription(transaction.getDescription());
        transactionResponse.setRefernceNumber(generateReferenceNumber());
        transactionResponse.setType(transaction.getType());
        transactionResponse.setStatus(transaction.getStatus());
        return transactionResponse;
    }

    public TransactionResponse getTransaction(String transactionId) {
        return maptoResponse(transactionRepository.findById(transactionId)
        .orElseThrow(() -> new RuntimeException("Transaction with id " + transactionId + " not found")));
    }

    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        List<Transaction> transactions =  transactionRepository.findAllByAccountNumber(accountNumber);
        return transactions.stream().map( transaction -> maptoResponse(transaction)).toList();
    }
}
