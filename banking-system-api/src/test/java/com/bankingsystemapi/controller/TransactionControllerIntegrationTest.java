package com.bankingsystemapi.controller;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.User;
import com.bankingsystemapi.repository.AccountRepository;
import com.bankingsystemapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User testUser;
    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    public void setup() {
        testUser = new User("testuser", "password", "test@example.com");
        userRepository.save(testUser);
        fromAccount = new Account("123456", 2000.0, testUser);
        toAccount = new Account("654321", 1000.0, testUser);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeposit() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("accountId", toAccount.getId());
        request.put("amount", 500.0);

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("deposit"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testWithdraw() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("accountId", fromAccount.getId());
        request.put("amount", 500.0);

        mockMvc.perform(post("/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("withdraw"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testTransfer() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("fromAccountId", fromAccount.getId());
        request.put("toAccountId", toAccount.getId());
        request.put("amount", 500.0);

        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("transfer"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetTransactionHistory() throws Exception {
        mockMvc.perform(get("/transactions/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
