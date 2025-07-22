package com.nkraft.budget.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.entity.Account;
import com.nkraft.budget.entity.TransactionStatus;
import com.nkraft.budget.entity.Goal;
import com.nkraft.budget.entity.Borrow;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.budget.entity.RecurringTransaction;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * 指定されたユーザー、口座、および取引ステータスに一致する取引を、取引日の昇順で検索します。
     * @param user ユーザーエンティティ
     * @param account 口座エンティティ
     * @param status 取引ステータス
     * @return 取引のリスト
     */
    List<Transaction> findByUserAndAccountAndTransactionStatusAndIsDeletedFalseOrderByTransactionDateAsc(NkraftUser user, Account account, TransactionStatus status);

    List<Transaction> findByBorrowAndTransactionStatusAndIsDeletedFalseOrderByTransactionDateAsc(Borrow borrow, TransactionStatus status);

    List<Transaction> findByGoalAndTransactionStatusAndIsDeletedFalseOrderByTransactionDateAsc(Goal goal, TransactionStatus status);

    Optional<Transaction> findTopByRecurringTransactionAndIsDeletedFalseOrderByTransactionDateDesc(RecurringTransaction recurringTransaction);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.account.isMain = true AND t.budgetTransactionType.name = '出金' AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month AND t.isDeleted = false")
    List<Transaction> findMainAccountExpendituresForMonth(@Param("user") NkraftUser user, @Param("year") int year, @Param("month") int month);

    @Query("SELECT MIN(t.transactionDate) FROM Transaction t WHERE t.user = :user AND t.account.isMain = true AND t.budgetTransactionType.name = '出金' AND t.isDeleted = false")
    Optional<LocalDate> findOldestExpenditureDate(@Param("user") NkraftUser user);
}