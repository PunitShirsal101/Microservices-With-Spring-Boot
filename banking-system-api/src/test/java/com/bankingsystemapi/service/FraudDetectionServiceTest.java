package com.bankingsystemapi.service;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.Transaction;
import com.bankingsystemapi.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class FraudDetectionServiceTest {

    private FraudDetectionService fraudDetectionService = new FraudDetectionService();

    @Test
    public void testIsFraudulent_LowAmount() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction = new Transaction("transfer", 5000.0, fromAccount, toAccount);

        boolean result = fraudDetectionService.isFraudulent(transaction);

        assertFalse(result);
    }

    @Test
    public void testIsFraudulent_HighAmount() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction = new Transaction("transfer", 15000.0, fromAccount, toAccount);

        boolean result = fraudDetectionService.isFraudulent(transaction);

        assertTrue(result);
    }

    @Test
    public void testIsFraudulent_SuspiciousTime() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction = new Transaction("deposit", 1000.0, null, toAccount);
        transaction.setTimestamp(LocalDateTime.of(2025, 11, 9, 3, 0)); // 3 AM

        boolean result = fraudDetectionService.isFraudulent(transaction);

        assertTrue(result);
    }

    @Test
    public void testIsFraudulent_LargeWithdraw() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Transaction transaction = new Transaction("withdraw", 6000.0, fromAccount, null);
        transaction.setTimestamp(LocalDateTime.of(2025, 11, 9, 10, 0)); // 10 AM

        boolean result = fraudDetectionService.isFraudulent(transaction);

        assertTrue(result);
    }

    @Test
    public void testIsFraudulent_LargeTransfer() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction = new Transaction("transfer", 6000.0, fromAccount, toAccount);
        transaction.setTimestamp(LocalDateTime.of(2025, 11, 9, 14, 0)); // 2 PM

        boolean result = fraudDetectionService.isFraudulent(transaction);

        assertTrue(result);
    }
}
