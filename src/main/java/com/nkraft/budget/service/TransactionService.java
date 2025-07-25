package com.nkraft.budget.service;

import com.nkraft.budget.dto.TransactionDTO;
import com.nkraft.budget.dto.TransactionSearchForm;
import com.nkraft.budget.repository.BorrowRepository;
import com.nkraft.budget.entity.*;
import com.nkraft.budget.dto.TransactionDateUpdateDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nkraft.user.entity.NkraftUser;
import com.nkraft.budget.repository.TransactionRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BudgetTransactionTypeService budgetTransactionTypeService;
    private final BorrowRepository borrowRepository;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Transactional(readOnly = true)
    public Page<Transaction> searchTransactions(TransactionSearchForm form, NkraftUser user, Pageable pageable) {
        return transactionRepository.findAll(getSpecification(form, user), pageable);
    }

    private Specification<Transaction> getSpecification(TransactionSearchForm form, NkraftUser user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user"), user));
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
                predicates.add(cb.like(root.get("memo"), "%" + form.getKeyword() + "%"));
            }
            if (form.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), form.getStartDate()));
            }
            if (form.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), form.getEndDate()));
            }
            if (form.getAccountIds() != null && !form.getAccountIds().isEmpty()) {
                predicates.add(root.get("account").get("accountId").in(form.getAccountIds()));
            }
            if (form.getCategoryIds() != null && !form.getCategoryIds().isEmpty()) {
                predicates.add(root.get("category").get("id").in(form.getCategoryIds()));
            }
            if (form.getStatuses() != null && !form.getStatuses().isEmpty()) {
                predicates.add(root.get("transactionStatus").in(form.getStatuses()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

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

        // ユーザーが取引の所有者であるか確認（セキュリティ）
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to update this transaction.");
        }

        // 完了済みの取引は編集不可
        if (transaction.getTransactionStatus() == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("完了済みの取引は編集できません。");
        }

        // 更新可能なフィールドをセット
        transaction.setTransactionDate(transactionDate);
        transaction.setPlannedAmount(plannedAmount);
        transaction.setBudgetTransactionType(budgetTransactionType);
        transaction.setCategory(category);
        transaction.setMemo(memo);

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

        // F-B06: 借入返済の場合、借入の返済済み額を更新する
        if (transaction.getBorrow() != null) {
            Borrow borrow = transaction.getBorrow();
            // 念のため所有者チェック
            if (!borrow.getNkraftUser().getId().equals(user.getId())) {
                throw new SecurityException("User does not have permission to update the related borrow record.");
            }
            borrow.setRepaidAmount(borrow.getRepaidAmount().add(finalActualAmount));
        }
        // F-B07: 目標貯金の場合、目標の貯金済み額を更新する
        if (transaction.getGoal() != null) {
            Goal goal = transaction.getGoal();
            // 念のため所有者チェック
            if (!goal.getNkraftUser().getId().equals(user.getId())) {
                throw new SecurityException("User does not have permission to update the related goal record.");
            }
            goal.setSavedAmount(goal.getSavedAmount().add(finalActualAmount));
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
        Category category = sourceTransaction.getCategory();

        // 1. 元口座からの「振替」取引を作成し、残高を更新
        // createAndCompleteTransactionは内部で残高を更新し、取引を保存する
        createAndCompleteTransaction(user, sourceAccount, transferType.getName(), category, LocalDate.now(), differenceAmount, memo, null, null);

        // 2. 貯金口座への「入金」取引を作成し、残高を更新
        createAndCompleteTransaction(user, savingsAccount, depositType.getName(), category, LocalDate.now(), differenceAmount, memo, null, null);
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

        // 完了済みの取引は削除不可
        if (transaction.getTransactionStatus() == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("完了済みの取引は削除できません。");
        }

        transaction.setIsDeleted(true);
        logger.info("Step 3: Set isDeleted=true for transaction id: {}", transactionId);

        transactionRepository.save(transaction);
        logger.info("Step 4: Successfully saved (logically deleted) transaction id: {}", transactionId);
    }

    @Transactional
    public Transaction createTransaction(NkraftUser user, Account account, BudgetTransactionType budgetTransactionType,
                                         Category category, LocalDate transactionDate, BigDecimal plannedAmount, String memo,
                                         RecurringTransaction recurringTransaction, Borrow borrow, Goal goal) {
        Transaction transaction = new Transaction();
        transaction.setUser(user); // ユーザー
        transaction.setAccount(account); // 口座
        transaction.setBudgetTransactionType(budgetTransactionType); // 取引種別
        transaction.setCategory(category); // カテゴリ
        transaction.setTransactionDate(transactionDate); // 取引日
        transaction.setPlannedAmount(plannedAmount); // 予定額
        transaction.setTransactionStatus(TransactionStatus.PLANNED); // 取引ステータス（予定）
        transaction.setMemo(memo); // メモ
        transaction.setRecurringTransaction(recurringTransaction);
        transaction.setBorrow(borrow);
        transaction.setGoal(goal);

        transactionRepository.save(transaction);
        return transaction;
    }

    public List<Transaction> getPlannedTransactionsForAccount(NkraftUser user, Account account) {
        return transactionRepository.findByUserAndAccountAndTransactionStatusAndIsDeletedFalseOrderByTransactionDateAsc(
                user, account, TransactionStatus.PLANNED).stream()
                .filter(t -> !"振替".equals(t.getBudgetTransactionType().getName())) // 「振替」取引を除外
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 指定された口座の取引予定をDTOのリストとして取得します。
     * @param user ユーザー
     * @param account 口座
     * @return 取引予定のDTOリスト
     */
  public List<TransactionDTO> getPlannedTransactionsForAccountAsDTO(NkraftUser user, Account account) {
        return getPlannedTransactionsForAccount(user, account).stream()
                .map(this::mapTransactionToDto)
                .collect(java.util.stream.Collectors.toList());
  }

    /**
     * TransactionエンティティをTransactionDTOに変換します。
     * @param transaction 変換元のTransactionエンティティ
     * @return 変換後のTransactionDTO
     */
    public TransactionDTO mapTransactionToDto(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO(
                transaction.getId(),
                transaction.getTransactionDate(),
                transaction.getPlannedAmount(),
                transaction.getMemo(),
                transaction.getBudgetTransactionType().getName(),
                transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : null
        );
        dto.setBudgetTransactionTypeId(transaction.getBudgetTransactionType().getId());
        if (transaction.getCategory() != null) {
            dto.setCategoryId(transaction.getCategory().getId());
        }
        if (transaction.getBorrow() != null) {
            dto.setBorrowId(transaction.getBorrow().getBorrowId());
            dto.setBorrowName(transaction.getBorrow().getBorrowName());
        }
        if (transaction.getGoal() != null) {
            dto.setGoalId(transaction.getGoal().getGoalId());
            dto.setGoalName(transaction.getGoal().getGoalName());
        }
        return dto;
    }

    /**
     * 指定された借入に紐づく「予定」ステータスの取引をDTOのリストとして取得します。
     * @param borrow 借入エンティティ
     * @return 取引予定のDTOリスト
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getPlannedTransactionsForBorrow(Borrow borrow) {
        return transactionRepository.findByBorrowAndTransactionStatusAndIsDeletedFalseOrderByTransactionDateAsc(borrow, TransactionStatus.PLANNED)
                .stream()
                .map(this::mapTransactionToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 指定された目標に紐づく「予定」ステータスの取引をDTOのリストとして取得します。
     * @param goal 目標エンティティ
     * @return 取引予定のDTOリスト
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getPlannedTransactionsForGoal(Goal goal) {
        return transactionRepository.findByGoalAndTransactionStatusAndIsDeletedFalseOrderByTransactionDateAsc(goal, TransactionStatus.PLANNED)
                .stream()
                .map(this::mapTransactionToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 複数の取引の日付を一括で更新します。
     *
     * @param dtoList 取引IDと新しい日付のペアのリスト
     * @param user 現在のユーザー
     */
    @Transactional
    public void updateTransactionDates(List<TransactionDateUpdateDTO> dtoList, NkraftUser user) {
        Map<Long, LocalDate> dateMap = dtoList.stream()
                .collect(java.util.stream.Collectors.toMap(TransactionDateUpdateDTO::getTransactionId, TransactionDateUpdateDTO::getNewDate));

        List<Long> ids = dtoList.stream().map(TransactionDateUpdateDTO::getTransactionId).collect(java.util.stream.Collectors.toList());
        List<Transaction> transactionsToUpdate = transactionRepository.findAllById(ids);

        for (Transaction transaction : transactionsToUpdate) {
            if (!transaction.getUser().getId().equals(user.getId())) {
                throw new SecurityException("User " + user.getId() + " does not have permission to update transaction " + transaction.getId());
            }
            LocalDate newDate = dateMap.get(transaction.getId());
            if (newDate != null) {
                transaction.setTransactionDate(newDate);
            }
        }
    }

    /**
     * 完了済みの取引を直接作成するためのプライベートヘルパーメソッド。
     */
    private Transaction createCompletedTransaction(NkraftUser user, Account account, BudgetTransactionType budgetTransactionType, Category category, LocalDate transactionDate, BigDecimal amount, String memo, RecurringTransaction recurringTransaction) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setBudgetTransactionType(budgetTransactionType);
        transaction.setCategory(category);
        transaction.setTransactionDate(transactionDate);
        transaction.setPlannedAmount(amount);
        transaction.setActualAmount(amount); // 実績額も同額で設定
        transaction.setTransactionStatus(TransactionStatus.COMPLETED); // ステータスを「完了」に設定
        transaction.setMemo(memo);
        transaction.setRecurringTransaction(recurringTransaction);

        return transactionRepository.save(transaction);
    }

    /**
     * 借入や目標達成など、即時完了とする取引を作成します。
     * 口座残高も更新します。
     *
     * @param user ユーザー
     * @param account 対象口座
     * @param transactionTypeName 取引種別名（"入金", "出金"など）
     * @param transactionDate 取引日
     * @param amount 金額
     * @param memo メモ
     * @param borrow 関連する借入 (任意)
     * @param goal 関連する目標 (任意)
     * @return 作成された取引
     */
    @Transactional
    public Transaction createAndCompleteTransaction(NkraftUser user, Account account, String transactionTypeName, Category category, LocalDate transactionDate, BigDecimal amount, String memo, Borrow borrow, Goal goal) {
        BudgetTransactionType transactionType = budgetTransactionTypeService.getTransactionTypeByName(transactionTypeName);

        Transaction transaction = createCompletedTransaction(user, account, transactionType, category, transactionDate, amount, memo, null);
        transaction.setBorrow(borrow);
        transaction.setGoal(goal);

        // 口座残高を更新
        if ("入金".equals(transactionTypeName)) {
            account.setBalance(account.getBalance().add(amount));
        } else { // 出金 or 振替
            account.setBalance(account.getBalance().subtract(amount));
        }

        return transactionRepository.save(transaction);
    }

    public BudgetTransactionType getExpenditureTransactionType() {
        return budgetTransactionTypeService.getTransactionTypeByName("出金");
    }
}