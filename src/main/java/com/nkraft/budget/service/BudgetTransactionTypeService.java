package com.nkraft.budget.service;

import com.nkraft.budget.entity.BudgetTransactionType;
import com.nkraft.budget.repository.BudgetTransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetTransactionTypeService {
    private final BudgetTransactionTypeRepository budgetTransactionTypeRepository;

    public List<BudgetTransactionType> getAllTransactionTypes() {
        return budgetTransactionTypeRepository.findAll();
    }

    public BudgetTransactionType getTransactionTypeById(Long id) {
        return budgetTransactionTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BudgetTransactionType not found with id: " + id));
    }

    public BudgetTransactionType getTransactionTypeByName(String name) {
        return budgetTransactionTypeRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("BudgetTransactionType not found with name: " + name));
    }
}