package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.UpdateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTests {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
    }

    @Test
    void createAccountReturnsPersistedDetailsAndDefaultsMissingLimitToZero() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountHolderName("Rishi Bhatia");
        request.setEmail("rishi@example.com");
        request.setPhone("9876543210");
        request.setAccountType(AccountType.SAVING);
        request.setIntialDeposit(new BigDecimal("250.00"));
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 5, 12, 0);
        when(accountRepository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId("account-1");
            account.setCreatedAt(createdAt);
            return account;
        });

        AccountResponse response = accountService.createAccount(request);

        assertEquals("account-1", response.getId());
        assertNotNull(response.getAccountNumber());
        assertEquals("Rishi Bhatia", response.getAccountHolderName());
        assertEquals("rishi@example.com", response.getEmail());
        assertEquals("9876543210", response.getPhone());
        assertEquals(AccountType.SAVING, response.getAccountType());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());
        assertEquals(new BigDecimal("250.00"), response.getBalance());
        assertEquals(BigDecimal.ZERO, response.getDailyTransactionLimit());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void updateAccountPreservesIdentityBalanceAndCreationTime() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 5, 12, 0);
        Account account = new Account();
        account.setId("account-1");
        account.setAccountNumber("existing-number");
        account.setBalance(new BigDecimal("500.00"));
        account.setCreatedAt(createdAt);
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountHolderName("Updated Name");
        request.setEmail("updated@example.com");
        request.setPhone("9123456780");
        request.setAccountType(AccountType.CURRENT);
        request.setAccountStatus(AccountStatus.BLOCKED);
        request.setDailyTransactionLimit(new BigDecimal("100.00"));
        when(accountRepository.findById("account-1")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        AccountResponse response = accountService.updateAccount("account-1", request);

        assertEquals("account-1", response.getId());
        assertEquals("existing-number", response.getAccountNumber());
        assertEquals(new BigDecimal("500.00"), response.getBalance());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals("Updated Name", response.getAccountHolderName());
        assertEquals("updated@example.com", response.getEmail());
        assertEquals("9123456780", response.getPhone());
        assertEquals(AccountType.CURRENT, response.getAccountType());
        assertEquals(AccountStatus.BLOCKED, response.getAccountStatus());
        assertEquals(new BigDecimal("100.00"), response.getDailyTransactionLimit());
    }

    @Test
    void missingAccountReturnsNotFoundWithoutWriting() {
        when(accountRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseStatusException readError = assertThrows(ResponseStatusException.class,
                () -> accountService.getAccountById("missing"));
        ResponseStatusException updateError = assertThrows(ResponseStatusException.class,
                () -> accountService.updateAccount("missing", new UpdateAccountRequest()));
        ResponseStatusException deleteError = assertThrows(ResponseStatusException.class,
                () -> accountService.deleteAccount("missing"));

        assertEquals(HttpStatus.NOT_FOUND, readError.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, updateError.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, deleteError.getStatusCode());
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository, never()).delete(any(Account.class));
    }
}
