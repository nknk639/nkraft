package com.nkraft.budget.service;

import com.nkraft.budget.dto.RecurringTransactionCreateDTO;
import com.nkraft.budget.dto.RecurringTransactionJsDTO;
import com.nkraft.budget.dto.RecurringTransactionViewDTO;
import com.nkraft.budget.dto.RecurringTransactionUpdateDTO;
import com.nkraft.budget.entity.Account;
import com.nkraft.budget.entity.BudgetTransactionType;
import com.nkraft.budget.entity.Category;
import com.nkraft.budget.entity.RecurringTransaction;
import com.nkraft.budget.entity.RecurringTransactionRuleType;
import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.repository.AccountRepository;
import com.nkraft.budget.repository.BudgetTransactionTypeRepository;
import com.nkraft.budget.repository.CategoryRepository;
import com.nkraft.budget.repository.RecurringTransactionRepository;
import com.nkraft.budget.repository.TransactionRepository;
import com.nkraft.user.entity.NkraftUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final AccountRepository accountRepository;
    private final BudgetTransactionTypeRepository budgetTransactionTypeRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public List<RecurringTransactionViewDTO> getRecurringTransactionsForUser(NkraftUser user) {
        List<RecurringTransaction> recurringTransactions = recurringTransactionRepository.findByUserAndIsDeletedFalseOrderByIdDesc(user);
        return recurringTransactions.stream()
                .map(rt -> {
                    Optional<Transaction> latestTransaction = transactionRepository.findTopByRecurringTransactionAndIsDeletedFalseOrderByTransactionDateDesc(rt);
                    LocalDate latestDate = latestTransaction.map(Transaction::getTransactionDate).orElse(null);
                    return new RecurringTransactionViewDTO(rt, latestDate);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionJsDTO> getRecurringTransactionsForUserAsJsDTO(NkraftUser user) {
        return recurringTransactionRepository.findByUserAndIsDeletedFalseOrderByIdDesc(user).stream()
                .map(rt -> {
                    RecurringTransactionJsDTO dto = new RecurringTransactionJsDTO();
                    dto.setId(rt.getId());
                    dto.setName(rt.getName());
                    dto.setAccountId(rt.getAccount().getAccountId());
                    dto.setBudgetTransactionTypeId(rt.getBudgetTransactionType().getId());
                    dto.setCategoryId(rt.getCategory() != null ? rt.getCategory().getId() : null);
                    dto.setAmount(rt.getAmount());
                    dto.setMemo(rt.getMemo());
                    dto.setRuleType(rt.getRuleType());
                    dto.setDayOfMonth(rt.getDayOfMonth());
                    dto.setDayOfWeek(rt.getDayOfWeek());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void createRecurringTransaction(RecurringTransactionCreateDTO dto, NkraftUser user) {
        RecurringTransaction rt = new RecurringTransaction();

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + dto.getAccountId()));
        BudgetTransactionType type = budgetTransactionTypeRepository.findById(dto.getBudgetTransactionTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction type not found with id: " + dto.getBudgetTransactionTypeId()));

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId()));
        }

        rt.setUser(user);
        rt.setName(dto.getName());
        rt.setAccount(account);
        rt.setBudgetTransactionType(type);
        rt.setCategory(category);
        rt.setAmount(dto.getAmount());
        rt.setMemo(dto.getMemo());
        rt.setRuleType(dto.getRuleType());
        rt.setIsDeleted(false);

        if (dto.getRuleType() == RecurringTransactionRuleType.毎月) {
            rt.setDayOfMonth(dto.getDayOfMonth());
        } else if (dto.getRuleType() == RecurringTransactionRuleType.毎週) {
            rt.setDayOfWeek(dto.getDayOfWeek());
        }

        recurringTransactionRepository.save(rt);
    }

    @Transactional
    public void updateRecurringTransaction(Long id, RecurringTransactionUpdateDTO dto, NkraftUser user) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RecurringTransaction not found with id: " + id));

        if (!rt.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not have permission to update this recurring transaction.");
        }

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + dto.getAccountId()));
        BudgetTransactionType type = budgetTransactionTypeRepository.findById(dto.getBudgetTransactionTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction type not found with id: " + dto.getBudgetTransactionTypeId()));
        Category category = (dto.getCategoryId() != null) ? categoryRepository.findById(dto.getCategoryId()).orElse(null) : null;

        rt.setName(dto.getName());
        rt.setAccount(account);
        rt.setBudgetTransactionType(type);
        rt.setCategory(category);
        rt.setAmount(dto.getAmount());
        rt.setMemo(dto.getMemo());
        rt.setRuleType(dto.getRuleType());

        if (dto.getRuleType() == RecurringTransactionRuleType.毎月) {
            rt.setDayOfMonth(dto.getDayOfMonth());
            rt.setDayOfWeek(null);
        } else if (dto.getRuleType() == RecurringTransactionRuleType.毎週) {
            rt.setDayOfWeek(dto.getDayOfWeek());
            rt.setDayOfMonth(null);
        }

        recurringTransactionRepository.save(rt);
    }

    @Transactional
    public void deleteRecurringTransaction(Long id, NkraftUser user) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RecurringTransaction not found with id: " + id));
        if (!rt.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not have permission to delete this recurring transaction.");
        }
        rt.setIsDeleted(true);
        recurringTransactionRepository.save(rt);
    }

    @Transactional
    public void executeRecurringTransaction(Long recurringTransactionId, NkraftUser user) {
        RecurringTransaction rt = recurringTransactionRepository.findById(recurringTransactionId)
                .orElseThrow(() -> new EntityNotFoundException("RecurringTransaction not found with id: " + recurringTransactionId));

        if (!rt.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not have permission to execute this recurring transaction.");
        }

        Optional<Transaction> latestTransactionOpt = transactionRepository.findTopByRecurringTransactionAndIsDeletedFalseOrderByTransactionDateDesc(rt);
        // 最後に実行された取引がなければ、今日を基準に次の日付を計算する
        LocalDate baseDate = latestTransactionOpt.map(Transaction::getTransactionDate).orElse(LocalDate.now());

        LocalDate nextDate = calculateNextDate(baseDate, rt);

        // TransactionService を呼び出して、RecurringTransaction を渡す
        transactionService.createTransaction(
                user,
                rt.getAccount(),
                rt.getBudgetTransactionType(),
                rt.getCategory(),
                nextDate,
                rt.getAmount(),
                rt.getName(), // ルール名をメモとして利用
                rt // ★★★ ここで関連付け情報を渡す
                , null
                , null
        );
    }

    private LocalDate calculateNextDate(LocalDate baseDate, RecurringTransaction rt) {
        if (rt.getRuleType() == RecurringTransactionRuleType.毎月) {
            LocalDate next;
            try {
                // まずは同じ月で日付を設定しようと試みる
                next = baseDate.withDayOfMonth(rt.getDayOfMonth());
            } catch (java.time.DateTimeException e) {
                // 指定した日が存在しない月の場合（例: 2月30日）、その月の最終日を設定する
                next = baseDate.with(TemporalAdjusters.lastDayOfMonth());
            }

            // 計算した日付が基準日以前なら、翌月にする
            if (!next.isAfter(baseDate)) {
                LocalDate nextMonthBase = baseDate.plusMonths(1);
                try {
                    // 翌月の指定された日を設定しようと試みる
                    next = nextMonthBase.withDayOfMonth(rt.getDayOfMonth());
                } catch (java.time.DateTimeException e) {
                    // 翌月にもその日が存在しない場合（例: 1/31の翌月）、翌月の最終日を設定する
                    next = nextMonthBase.with(TemporalAdjusters.lastDayOfMonth());
                }
            }
            return next;
        } else { // 毎週
            DayOfWeek targetDayOfWeek = DayOfWeek.of(rt.getDayOfWeek() == 0 ? 7 : rt.getDayOfWeek());
            return baseDate.with(TemporalAdjusters.next(targetDayOfWeek));
        }
    }
}