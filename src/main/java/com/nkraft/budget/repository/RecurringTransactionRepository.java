package com.nkraft.budget.repository;

import com.nkraft.budget.entity.RecurringTransaction;
import com.nkraft.user.entity.NkraftUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserAndIsDeletedFalseOrderByIdDesc(NkraftUser user);
}