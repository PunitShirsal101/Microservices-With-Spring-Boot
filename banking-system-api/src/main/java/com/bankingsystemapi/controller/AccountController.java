package com.bankingsystemapi.controller;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.User;
import com.bankingsystemapi.service.AccountService;
import com.bankingsystemapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        account.setUser(user);
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.findByAccountNumber(accountNumber));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<Double> getBalance(@PathVariable String accountNumber) {
        Account account = accountService.findByAccountNumber(accountNumber);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account.getBalance());
    }

    @GetMapping("/user")
    public ResponseEntity<List<Account>> getUserAccounts() {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(accountService.findByUser(user));
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
