package com.bankingsystemapi.service;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.User;
import com.bankingsystemapi.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void testCreateAccount() {
        User user = new User("testuser", "password", "test@example.com");
        Account account = new Account("123456", 1000.0, user);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(account);

        assertEquals("123456", result.getAccountNumber());
        assertEquals(1000.0, result.getBalance());
    }

    @Test
    public void testFindByAccountNumber() {
        User user = new User("testuser", "password", "test@example.com");
        Account account = new Account("123456", 1000.0, user);
        when(accountRepository.findByAccountNumber("123456")).thenReturn(account);

        Account result = accountService.findByAccountNumber("123456");

        assertEquals("123456", result.getAccountNumber());
    }

    @Test
    public void testFindByUser() {
        User user = new User("testuser", "password", "test@example.com");
        user.setId(1L);
        Account account1 = new Account("123456", 1000.0, user);
        Account account2 = new Account("654321", 500.0, user);
        List<Account> accounts = Arrays.asList(account1, account2);
        when(accountRepository.findByUserId(1L)).thenReturn(accounts);

        List<Account> result = accountService.findByUser(user);

        assertEquals(2, result.size());
        assertEquals("123456", result.get(0).getAccountNumber());
    }
}
