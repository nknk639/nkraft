package com.nkraft.budget.service;

import com.nkraft.budget.dto.TransactionDTO;
import com.nkraft.budget.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    /**
     * 取引を更新します。
     *
     * @param transactionId 取引ID
     * @param user ユーザー
     * @param transactionDate 取引日
     * @param plannedAmount 予定額
     * @param budgetTransactionType 取引種別
     * @param category カテゴリ
     * @param memo メモ
     * @throws EntityNotFoundException 指定されたIDの取引が見つからない場合
     */
    @Transactional
    public void updateTransaction(Long transactionId, NkraftUser user, LocalDate transactionDate, BigDecimal plannedAmount,
                                  BudgetTransactionType budgetTransactionType, Category category, String memo) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));

        // 更新可能なフィールドをセット
        transaction.setTransactionDate(transactionDate);
        transaction.setPlannedAmount(plannedAmount);
        transaction.setBudgetTransactionType(budgetTransactionType);
        transaction.setCategory(category);
        transaction.setMemo(memo);

        // ユーザーが取引の所有者であるか確認（セキュリティ）
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to update this transaction.");
        }
        transactionRepository.save(transaction);
    }

    /**
     * 取引を完了ステータスに更新し、口座残高を更新します。
     *
     * @param transactionId 完了する取引のID
     * @param actualAmount 実績額 (nullの場合は予定額が使用されます)
     * @param user 現在のユーザー
     * @return 更新後の口座エンティティ
     * @throws EntityNotFoundException 取引が見つからない場合
     * @throws SecurityException 取引の所有者でない場合
     * @throws IllegalStateException 取引が既に完了している場合
     */
    @Transactional
    public Transaction completeTransaction(Long transactionId, BigDecimal actualAmount, NkraftUser user) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to complete this transaction.");
        }

        if (transaction.getTransactionStatus() == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Transaction is already completed.");
        }

        BigDecimal finalActualAmount = (actualAmount != null) ? actualAmount : transaction.getPlannedAmount();
        transaction.setActualAmount(finalActualAmount);
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);

        Account account = transaction.getAccount();
        if ("入金".equals(transaction.getBudgetTransactionType().getName())) {
            account.setBalance(account.getBalance().add(finalActualAmount));
        } else { // 出金 or 振替
            account.setBalance(account.getBalance().subtract(finalActualAmount));
        }
        // The transaction object now has the updated status and actualAmount
        return transaction;
    }

    /**
     * 差額貯金のための振替取引を2件作成します。
     *
     * @param user ユーザー
     * @param sourceTransactionId 元となった支出取引のID
     * @param savingsAccount 振替先の貯金口座
     * @param differenceAmount 貯金する差額
     * @param transferType 「振替」の取引種別
     * @param depositType 「入金」の取引種別
     */
    @Transactional
    public void createSavingsTransfer(NkraftUser user, Long sourceTransactionId, Account savingsAccount, BigDecimal differenceAmount, BudgetTransactionType transferType, BudgetTransactionType depositType) {
        Transaction sourceTransaction = transactionRepository.findById(sourceTransactionId)
                .orElseThrow(() -> new EntityNotFoundException("Source transaction not found with id: " + sourceTransactionId));

        Account sourceAccount = sourceTransaction.getAccount();
        String memo = "差額貯金（" + sourceTransaction.getMemo() + "）";
        createTransaction(user, sourceAccount, transferType, sourceTransaction.getCategory(), LocalDate.now(), differenceAmount, memo);
        createTransaction(user, savingsAccount, depositType, sourceTransaction.getCategory(), LocalDate.now(), differenceAmount, memo);
    }

    /**
     * 指定された取引を論理削除します。
     *
     * @param transactionId 削除する取引のID
     * @param user 現在のユーザー
     * @throws EntityNotFoundException 取引が見つからない場合
     * @throws SecurityException 取引の所有者でない場合
     */
    @Transactional
    public void deleteTransaction(Long transactionId, NkraftUser user) {
        logger.info("Step 1: Attempting to find transaction with id: {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    logger.warn("Step 1 FAILED: Transaction with id {} not found.", transactionId);
                    return new EntityNotFoundException("Transaction not found with id: " + transactionId);
                });
        logger.info("Step 1 SUCCESS: Found transaction id: {}", transaction.getId());

        logger.info("Step 2: Checking ownership for transaction id: {}", transactionId);
        if (!transaction.getUser().getId().equals(user.getId())) {
            logger.warn("Step 2 FAILED: Security check failed. User {} attempted to delete transaction {} owned by user {}.",
                    user.getId(), transaction.getId(), transaction.getUser().getId());
            throw new SecurityException("You do not have permission to delete this transaction.");
        }
        logger.info("Step 2 SUCCESS: Ownership check passed for transaction id: {}", transactionId);

        transaction.setIsDeleted(true);
        logger.info("Step 3: Set isDeleted=true for transaction id: {}", transactionId);

        transactionRepository.save(transaction);
        logger.info("Step 4: Successfully saved (logically deleted) transaction id: {}", transactionId);
    }

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
        return transactionRepository.findByUserAndAccountAndTransactionStatusAndIsDeletedFalseOrderByTransactionDateAsc(
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