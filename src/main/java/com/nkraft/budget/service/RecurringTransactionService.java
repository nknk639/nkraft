package com.nkraft.budget.service;

import com.nkraft.budget.entity.RecurringTransaction;
import com.nkraft.budget.repository.RecurringTransactionRepository;
import com.nkraft.user.entity.NkraftUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;

    @Transactional(readOnly = true)
    public List<RecurringTransaction> getRecurringTransactionsForUser(NkraftUser user) {
        return recurringTransactionRepository.findByUserAndIsDeletedFalseOrderByIdDesc(user);
    }
}