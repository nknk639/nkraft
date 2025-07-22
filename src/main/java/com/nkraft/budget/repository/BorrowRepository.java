package com.nkraft.budget.repository;

import com.nkraft.budget.entity.Borrow;
import com.nkraft.user.entity.NkraftUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    List<Borrow> findByNkraftUserAndIsDeletedFalseOrderByBorrowIdDesc(NkraftUser user);
    Optional<Borrow> findByBorrowIdAndNkraftUserAndIsDeletedFalse(Long borrowId, NkraftUser user);
}