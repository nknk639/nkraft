package com.nkraft.budget.service;

import com.nkraft.budget.entity.Account;
import com.nkraft.budget.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByNkraftUser_Id(userId);
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + accountId));
    }

    public Optional<Account> findMainAccountByUserId(Long userId) {
        return accountRepository.findByNkraftUser_IdAndIsMainTrue(userId);
    }

    public List<Account> findSavingsAccountsByUserId(Long userId) {
        return accountRepository.findByNkraftUser_IdAndIsSavingsTrue(userId);
    }
}