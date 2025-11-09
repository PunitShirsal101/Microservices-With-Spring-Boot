package com.bankingsystemapi.service;

import com.bankingsystemapi.entity.Transaction;
import com.bankingsystemapi.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplianceService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> generateComplianceReport() {
        // For simplicity, return all transactions
        return transactionRepository.findAll();
    }
}
