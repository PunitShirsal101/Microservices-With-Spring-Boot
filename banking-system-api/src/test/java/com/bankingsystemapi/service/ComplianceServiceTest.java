package com.bankingsystemapi.service;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.Transaction;
import com.bankingsystemapi.entity.User;
import com.bankingsystemapi.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComplianceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ComplianceService complianceService;

    @Test
    public void testGenerateComplianceReport() {
        User user = new User("testuser", "password", "test@example.com");
        Account fromAccount = new Account("123456", 2000.0, user);
        Account toAccount = new Account("654321", 1000.0, user);
        Transaction transaction1 = new Transaction("transfer", 500.0, fromAccount, toAccount);
        Transaction transaction2 = new Transaction("deposit", 1000.0, null, toAccount);
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> report = complianceService.generateComplianceReport();

        assertEquals(2, report.size());
        assertEquals("transfer", report.get(0).getType());
    }
}
