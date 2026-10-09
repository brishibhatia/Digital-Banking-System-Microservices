package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.UpdateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private static final SecureRandom secureRandom = new SecureRandom();

    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info( "Creating account for {}", request.getEmail() );

        if(accountRepository.existsByEmail(request.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        Account account = new Account();
//        account.setAccountNumber(UUID.randomUUID().toString());
        account.setAccountNumber(generateAccountNumber());
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getIntialDeposit());
        account.setDailyTransactionLimit(
                request.getAccountType() == AccountType.SAVING
                ? new BigDecimal(10000)
                        : new BigDecimal(50000)
        );

        return mapToResponse(accountRepository.saveAndFlush(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber){
        return mapToResponse(findAccountByAccountNumber(accountNumber));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(String id) {
        return mapToResponse(findAccountById(id));
    }


    public AccountResponse updateAccount(String id, UpdateAccountRequest request) {
        Account account = findAccountById(id);
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(request.getAccountStatus());
        account.setDailyTransactionLimit(request.getDailyTransactionLimit());

        return mapToResponse(accountRepository.save(account));
    }

    public void deleteAccount(String id) {
        accountRepository.delete(findAccountById(id));
    }

    private Account findAccountById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found: " + id));
    }

    private Account findAccountByAccountNumber(String accountNumber){
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        if(account.isPresent()){
            return account.get();
        }
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Account not found: " + accountNumber
        );

    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setAccountType(account.getAccountType());
        response.setAccountStatus(account.getAccountStatus());
        response.setBalance(account.getBalance());
        response.setDailyTransactionLimit(account.getDailyTransactionLimit());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }
    private String generateAccountNumber(){
        String accountNumber;

        do {
            long number = 100_000_000_000L
                    + secureRandom.nextLong(900_000_000_000L);

            accountNumber = String.valueOf(number);

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    public BigDecimal getBalance(String accountNumber) {
            return findAccountByAccountNumber(accountNumber).getBalance();
    }

    public void blockAccount(String accountNumber) {
        log.info(
                "Account blocking for {}", accountNumber
        );

        Account account = findAccountByAccountNumber(accountNumber);

        account.setAccountStatus(AccountStatus.BLOCKED);
    }

    public void deductBalance(String accountNumber, BigDecimal amount) {
        Account account = findAccountByAccountNumber(accountNumber);

        if(account.getAccountStatus() != (AccountStatus.ACTIVE)){
        throw new RuntimeException("Account not active");
        }

        if(account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient balance");
        }
        account.setBalance(account.getBalance().subtract(amount));

        log.info(
                "Balance updated , New Balance {}", account.getBalance()
        );
    }

    public void creditBalance(String accountNumber, BigDecimal amount) {
        Account account = findAccountByAccountNumber(accountNumber);

        if(account.getAccountStatus() != (AccountStatus.ACTIVE)){
            throw new RuntimeException("Account not active");
        }
        account.setBalance(account.getBalance().add(amount));
        log.info(
                "Balance updated , New Balance {}", account.getBalance()
        );

    }
}
