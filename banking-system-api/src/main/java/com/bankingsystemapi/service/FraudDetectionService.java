package com.bankingsystemapi.service;

import com.bankingsystemapi.entity.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class FraudDetectionService {

    public boolean isFraudulent(Transaction transaction) {
        // Fraud detection rules as per banking standards
        double amount = transaction.getAmount();

        // Rule 1: High amount threshold
        if (amount > 10000) {
            return true;
        }

        // Rule 2: Suspicious time - transactions between 2 AM and 6 AM
        LocalDateTime timestamp = transaction.getTimestamp();
        if (timestamp != null) {
            LocalTime time = timestamp.toLocalTime();
            if (time.isAfter(LocalTime.of(2, 0)) && time.isBefore(LocalTime.of(6, 0))) {
                return true;
            }
        }

        // Rule 3: Large withdrawals or transfers during business hours but high amount
        if ((transaction.getType().equals("withdraw") || transaction.getType().equals("transfer")) && amount > 5000) {
            return true;
        }

        // Rule 4: Very small amounts might be tested transactions, but for now, allow

        return false;
    }
}
