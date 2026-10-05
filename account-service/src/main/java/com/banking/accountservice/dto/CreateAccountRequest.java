package com.banking.accountservice.dto;

import com.banking.accountservice.entity.AccountType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account Holder name is Required")
    private String accountHolderName;

    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is Required")
    private String phone;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @Positive
    @NotNull(message = "Intial Deposit is Must")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal intialDeposit;

    @PositiveOrZero
    @Digits(integer = 13, fraction = 2)
    private BigDecimal dailyTransactionLimit;


}
