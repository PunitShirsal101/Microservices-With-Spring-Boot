package com.bankingsystemapi.service;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.Transaction;
import com.bankingsystemapi.repository.AccountRepository;
import com.bankingsystemapi.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    public Transaction processTransaction(Transaction transaction) {
        if (fraudDetectionService.isFraudulent(transaction)) {
            throw new RuntimeException("Fraud detected");
        }
        Account from = transaction.getFromAccount();
        if (from != null) {
            if (from.getBalance() < transaction.getAmount()) {
                throw new RuntimeException("Insufficient funds");
            }
            from.setBalance(from.getBalance() - transaction.getAmount());
            accountRepository.save(from);
        }
        Account to = transaction.getToAccount();
        if (to != null) {
            to.setBalance(to.getBalance() + transaction.getAmount());
            accountRepository.save(to);
        }
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
