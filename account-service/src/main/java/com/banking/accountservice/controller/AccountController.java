package com.banking.accountservice.controller;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.UpdateAccountRequest;
import com.banking.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable("id") String id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }

    @PutMapping("/{accountNumber}/block")
    public ResponseEntity<String> blockAccount(@PathVariable String accountNumber) {
        accountService.blockAccount(accountNumber);
        return ResponseEntity.ok("Account blocked");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

/*
*  SAGA STEP -1
* DEDUCT BALANCE
* Called by transaction service when transaction is intiated
*  */
    @PutMapping("/{accountNumber}/deduct")
    public ResponseEntity<String> deductBalance(@PathVariable String accountNumber , @RequestParam BigDecimal amount) {
        accountService.deductBalance(accountNumber ,amount);
        return ResponseEntity.ok("Balance deducted Successfully");

    }
/*
*   SAGA STEP - 4
* CREDIT BALANCE
* called by transaction service in 2 scenarioes
*  1 fraud detection - refund sender
*  2 transaction completed Credit receiver
* */
    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<String> creditBalance(@PathVariable String accountNumber , @RequestParam BigDecimal amount) {
        accountService.creditBalance(accountNumber ,amount);
        return ResponseEntity.ok("Balance deducted Successfully");
    }


}
