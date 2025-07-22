package com.nkraft.budget.repository;

import com.nkraft.budget.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByNkraftUser_IdAndIsDeletedFalse(Long userId);

    Optional<Account> findByNkraftUser_IdAndIsMainTrue(Long userId);

    List<Account> findByNkraftUser_IdAndIsSavingsTrueAndIsDeletedFalse(Long userId);
}