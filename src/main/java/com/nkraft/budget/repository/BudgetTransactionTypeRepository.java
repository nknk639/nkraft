package com.nkraft.budget.repository;

import com.nkraft.budget.entity.BudgetTransactionType;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository // ★ 追加
public interface BudgetTransactionTypeRepository extends JpaRepository<BudgetTransactionType, Long> {
    Optional<BudgetTransactionType> findByName(String name);
}