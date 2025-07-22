package com.nkraft.budget.service;

import com.nkraft.budget.dto.GoalCreateDTO;
import com.nkraft.budget.dto.GoalDetailViewDTO;
import com.nkraft.budget.dto.GoalSummaryDTO;
import com.nkraft.budget.dto.GoalViewDTO;
import com.nkraft.budget.dto.SavingCreateDTO;
import com.nkraft.budget.entity.Account;
import com.nkraft.budget.entity.Goal;
import com.nkraft.budget.repository.GoalRepository;
import com.nkraft.user.entity.NkraftUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<GoalViewDTO> getGoalsForUser(NkraftUser user) {
        List<Goal> goals = goalRepository.findByNkraftUserAndIsDeletedFalseOrderByGoalIdDesc(user);
        return goals.stream()
                .map(this::mapToGoalViewDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GoalDetailViewDTO getGoalDetail(Long goalId, NkraftUser user) {
        Goal goal = getGoalById(goalId, user);
        GoalViewDTO goalViewDTO = mapToGoalViewDTO(goal);

        // 1. この目標に紐づく貯金予定リストを取得
        var savingTransactions = transactionService.getPlannedTransactionsForGoal(goal);

        // 2. メイン口座の取引予定リストを取得
        Optional<Account> mainAccountOpt = accountService.findMainAccountByUserId(user.getId());
        if (mainAccountOpt.isEmpty()) {
            return new GoalDetailViewDTO(goalViewDTO, savingTransactions, BigDecimal.ZERO, Collections.emptyList());
        }

        Account mainAccount = mainAccountOpt.get();
        var mainAccountPlannedTransactions = transactionService.getPlannedTransactionsForAccountAsDTO(user, mainAccount);
        return new GoalDetailViewDTO(goalViewDTO, savingTransactions, mainAccount.getBalance(), mainAccountPlannedTransactions);
    }


    @Transactional
    public void createGoal(GoalCreateDTO dto, NkraftUser user) {
        Goal goal = new Goal();
        goal.setNkraftUser(user);
        goal.setGoalName(dto.getGoalName());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setSavedAmount(BigDecimal.ZERO);
        goal.setIsDeleted(false);
        goalRepository.save(goal);
    }

    @Transactional
    public void createSaving(Long goalId, SavingCreateDTO dto, NkraftUser user) {
        Goal goal = getGoalById(goalId, user);
        Account sourceAccount = accountService.getAccountById(dto.getSourceAccountId());
        // 「目標貯金」カテゴリを取得。なければnull
        var savingCategory = categoryService.findByNameAndUser("目標貯金", user).orElse(null);

        // 「支出」「予定」の取引を作成
        transactionService.createTransaction(
                user,
                sourceAccount,
                transactionService.getExpenditureTransactionType(),
                savingCategory,
                dto.getSavingDate(),
                dto.getSavingAmount(),
                dto.getMemo(),
                null, // 繰り返し予定ではない
                null,
                goal // 目標と紐付け
        );
    }

    @Transactional
    public Map<String, Object> completeSaving(Long transactionId, BigDecimal actualAmount, NkraftUser user) {
        var completedTransaction = transactionService.completeTransaction(transactionId, actualAmount, user);
        Goal goal = completedTransaction.getGoal();
        GoalViewDTO updatedGoalDTO = null;

        if (goal != null) {
            getGoalById(goal.getGoalId(), user); // 所有者チェック
            updatedGoalDTO = mapToGoalViewDTO(goal);
        }

        return Map.of("transaction", completedTransaction, "goalViewDTO", updatedGoalDTO);
    }

    private Goal getGoalById(Long goalId, NkraftUser user) {
        return goalRepository.findByGoalIdAndNkraftUserAndIsDeletedFalse(goalId, user)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found with id: " + goalId));
    }

    private GoalViewDTO mapToGoalViewDTO(Goal goal) {
        BigDecimal target = goal.getTargetAmount();
        BigDecimal saved = goal.getSavedAmount();
        BigDecimal rate = BigDecimal.ZERO;

        if (target != null && target.compareTo(BigDecimal.ZERO) > 0) {
            rate = saved.divide(target, 4, RoundingMode.HALF_UP);
        }

        int ratePercent = rate.multiply(BigDecimal.valueOf(100)).intValue();
        boolean isCompleted = rate.compareTo(BigDecimal.ONE) >= 0;

        GoalSummaryDTO summaryDTO = new GoalSummaryDTO(
                goal.getGoalId(), goal.getGoalName(), goal.getTargetAmount(), goal.getSavedAmount());

        return new GoalViewDTO(summaryDTO, rate, ratePercent, isCompleted);
    }

    public GoalViewDTO getGoalViewDTO(Long goalId, NkraftUser user) {
        Goal goal = getGoalById(goalId, user);
        return mapToGoalViewDTO(goal);
    }
}