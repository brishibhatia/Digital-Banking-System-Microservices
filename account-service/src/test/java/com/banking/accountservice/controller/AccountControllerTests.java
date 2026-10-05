package com.banking.accountservice.controller;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.UpdateAccountRequest;
import com.banking.accountservice.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AccountControllerTests {

    private static final String CREATE_REQUEST = """
            {
              "accountHolderName": "Rishi Bhatia",
              "email": "rishi@example.com",
              "phone": "9876543210",
              "accountType": "SAVING",
              "intialDeposit": 250.00,
              "dailyTransactionLimit": 100.00
            }
            """;

    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AccountController(accountService)).build();
    }

    @Test
    void createReturnsCreatedWithLocationAndResponseDto() throws Exception {
        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/accounts/account-1"))
                .andExpect(jsonPath("$.id").value("account-1"))
                .andExpect(jsonPath("$.balance").value(250.0));
    }

    @Test
    void readEndpointsReturnResponseDtos() throws Exception {
        when(accountService.getAllAccounts()).thenReturn(List.of(response()));
        when(accountService.getAccountById("account-1")).thenReturn(response());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("account-1"));
        mockMvc.perform(get("/api/accounts/account-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("account-1"));
    }

    @Test
    void updateAcceptsUpdateDtoAndReturnsOk() throws Exception {
        when(accountService.updateAccount(eq("account-1"), any(UpdateAccountRequest.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/accounts/account-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountHolderName": "Updated Name",
                                  "email": "updated@example.com",
                                  "phone": "9123456780",
                                  "accountType": "CURRENT",
                                  "accountStatus": "ACTIVE",
                                  "dailyTransactionLimit": 100.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("account-1"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/accounts/account-1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(accountService).deleteAccount("account-1");
    }

    @Test
    void missingAccountReturnsNotFound() throws Exception {
        when(accountService.getAccountById("missing"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        mockMvc.perform(get("/api/accounts/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidRequestsReturnBadRequestWithoutCallingService() throws Exception {
        for (String invalidRequest : List.of(
                CREATE_REQUEST.replace("rishi@example.com", "invalid-email"),
                CREATE_REQUEST.replace("250.00", "0.00"),
                CREATE_REQUEST.replace("100.00", "-1.00"),
                CREATE_REQUEST.replace("100.00", "100.001"))) {
            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(put("/api/accounts/account-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    private AccountResponse response() {
        AccountResponse response = new AccountResponse();
        response.setId("account-1");
        response.setBalance(new BigDecimal("250.00"));
        return response;
    }
}
