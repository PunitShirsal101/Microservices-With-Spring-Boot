package com.bankingsystemapi.service;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.Transaction;
import com.bankingsystemapi.entity.User;
import com.bankingsystemapi.repository.AccountRepository;
import com.bankingsystemapi.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void testProcessTransaction_Success() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction = new Transaction("transfer", 500.0, fromAccount, toAccount);

        when(fraudDetectionService.isFraudulent(transaction)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(fromAccount, toAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.processTransaction(transaction);

        assertEquals("transfer", result.getType());
        assertEquals(500.0, result.getAmount());
    }

    @Test
    public void testProcessTransaction_FraudDetected() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction = new Transaction("transfer", 15000.0, fromAccount, toAccount);

        when(fraudDetectionService.isFraudulent(transaction)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(transaction));
    }

    @Test
    public void testProcessTransaction_InsufficientFunds() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 100.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction = new Transaction("transfer", 500.0, fromAccount, toAccount);

        when(fraudDetectionService.isFraudulent(transaction)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(transaction));
    }

    @Test
    public void testGetAllTransactions() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction1 = new Transaction("transfer", 500.0, fromAccount, toAccount);
        Transaction transaction2 = new Transaction("deposit", 1000.0, null, toAccount);
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertEquals(2, result.size());
        assertEquals("transfer", result.get(0).getType());
    }
}
