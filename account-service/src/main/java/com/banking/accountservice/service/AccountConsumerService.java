package com.banking.accountservice.service;

import com.banking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountConsumerService {
    private final AccountService accountService;

    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(
            @Payload Map<String,Object> payload
            ){
        try{

        String receiverAccount = (String) payload.get("accountNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            accountService.creditBalance(receiverAccount,amount);

        }
        catch(Exception e){
            log.error("Error while consuming transaction completed",e);
        }

    }

    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetection(
            @Payload Map<String,Object> payload
    ){
        try{
            String accountNumber = payload.get("accountNumber").toString();
            accountService.blockAccount(accountNumber);
            log.info("Account with Account Number {} has been blocked", accountNumber);

        }
        catch(Exception e){
            log.error("Error blocking account {}: ", e.getMessage());
        }

    }

}
