package com.nkraft.budget.service;

import com.nkraft.budget.dto.BudgetTransactionTypeDTO;
import com.nkraft.budget.entity.BudgetTransactionType;
import com.nkraft.budget.repository.BudgetTransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetTransactionTypeService {
    private final BudgetTransactionTypeRepository budgetTransactionTypeRepository;

    public List<BudgetTransactionType> getAllTransactionTypes() {
        return budgetTransactionTypeRepository.findAll();
    }

    public List<BudgetTransactionTypeDTO> getAllTransactionTypesAsDTO() {
        return budgetTransactionTypeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BudgetTransactionTypeDTO mapToDTO(BudgetTransactionType entity) {
        return new BudgetTransactionTypeDTO(entity.getId(), entity.getName());
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