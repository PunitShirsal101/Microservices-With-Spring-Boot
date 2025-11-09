package com.bankingsystemapi.controller;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.Transaction;
import com.bankingsystemapi.service.AccountService;
import com.bankingsystemapi.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<Transaction> processTransaction(@RequestBody Transaction transaction) {
        // Assume fromAccount and toAccount are provided with ids
        if (transaction.getFromAccount() != null && transaction.getFromAccount().getId() != null) {
            Account from = accountService.getAccountById(transaction.getFromAccount().getId());
            transaction.setFromAccount(from);
        }
        if (transaction.getToAccount() != null && transaction.getToAccount().getId() != null) {
            Account to = accountService.getAccountById(transaction.getToAccount().getId());
            transaction.setToAccount(to);
        }
        return ResponseEntity.ok(transactionService.processTransaction(transaction));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestBody Map<String, Object> request) {
        Long accountId = Long.valueOf(request.get("accountId").toString());
        Double amount = Double.valueOf(request.get("amount").toString());
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        Transaction transaction = new Transaction("deposit", amount, null, account);
        return ResponseEntity.ok(transactionService.processTransaction(transaction));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@RequestBody Map<String, Object> request) {
        Long accountId = Long.valueOf(request.get("accountId").toString());
        Double amount = Double.valueOf(request.get("amount").toString());
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        Transaction transaction = new Transaction("withdraw", amount, account, null);
        return ResponseEntity.ok(transactionService.processTransaction(transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@RequestBody Map<String, Object> request) {
        Long fromAccountId = Long.valueOf(request.get("fromAccountId").toString());
        Long toAccountId = Long.valueOf(request.get("toAccountId").toString());
        Double amount = Double.valueOf(request.get("amount").toString());
        Account fromAccount = accountService.getAccountById(fromAccountId);
        Account toAccount = accountService.getAccountById(toAccountId);
        if (fromAccount == null || toAccount == null) {
            throw new RuntimeException("Account not found");
        }
        Transaction transaction = new Transaction("transfer", amount, fromAccount, toAccount);
        return ResponseEntity.ok(transactionService.processTransaction(transaction));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getTransactionHistory() {
        // For simplicity, return all, but should filter by user accounts
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}
