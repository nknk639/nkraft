package com.nkraft.budget.service;

import com.nkraft.budget.dto.TransactionDTO;
import com.nkraft.budget.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nkraft.budget.dto.TransactionDTO;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.budget.repository.TransactionRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(NkraftUser user, Account account, BudgetTransactionType budgetTransactionType, Category category, LocalDate transactionDate, BigDecimal plannedAmount, String memo) {
        Transaction transaction = new Transaction();
        transaction.setUser(user); // ユーザー
        transaction.setAccount(account); // 口座
        transaction.setBudgetTransactionType(budgetTransactionType); // 取引種別
        transaction.setCategory(category); // カテゴリ
        transaction.setTransactionDate(transactionDate); // 取引日
        transaction.setPlannedAmount(plannedAmount); // 予定額
        transaction.setTransactionStatus(TransactionStatus.PLANNED); // 取引ステータス（予定）
        transaction.setMemo(memo); // メモ

        transactionRepository.save(transaction);
        return transaction;
    }

    public List<Transaction> getPlannedTransactionsForAccount(NkraftUser user, Account account) {
        return transactionRepository.findByUserAndAccountAndTransactionStatusOrderByTransactionDateAsc(
            user, account, TransactionStatus.PLANNED);
    }

    /**
     * 指定された口座の取引予定をDTOのリストとして取得します。
     * @param user ユーザー
     * @param account 口座
     * @return 取引予定のDTOリスト
     */
  public List<TransactionDTO> getPlannedTransactionsForAccountAsDTO(NkraftUser user, Account account) {
    return getPlannedTransactionsForAccount(user, account).stream()
        .map(transaction -> new TransactionDTO(
            transaction.getId(),
            transaction.getTransactionDate(),
            transaction.getPlannedAmount(),
            transaction.getMemo(),
            transaction.getBudgetTransactionType().getName(),
            transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : null
        ))
        .collect(java.util.stream.Collectors.toList());
  }
}