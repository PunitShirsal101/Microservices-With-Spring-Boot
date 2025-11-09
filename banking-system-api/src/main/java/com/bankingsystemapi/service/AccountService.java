package com.bankingsystemapi.service;

import com.bankingsystemapi.entity.Account;
import com.bankingsystemapi.entity.User;
import com.bankingsystemapi.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    public List<Account> findByUser(User user) {
        return accountRepository.findByUserId(user.getId());
    }
}
