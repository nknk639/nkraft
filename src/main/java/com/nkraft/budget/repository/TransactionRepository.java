package com.nkraft.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.entity.Account;
import com.nkraft.budget.entity.TransactionStatus;
import com.nkraft.user.entity.NkraftUser;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 指定されたユーザー、口座、および取引ステータスに一致する取引を、取引日の昇順で検索します。
     * @param user ユーザーエンティティ
     * @param account 口座エンティティ
     * @param status 取引ステータス
     * @return 取引のリスト
     */
    List<Transaction> findByUserAndAccountAndTransactionStatusAndIsDeletedFalseOrderByTransactionDateAsc(NkraftUser user, Account account, TransactionStatus status);
}