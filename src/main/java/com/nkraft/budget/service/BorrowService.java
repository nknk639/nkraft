package com.nkraft.budget.service;

import com.nkraft.budget.dto.BorrowCreateDTO;
import com.nkraft.budget.dto.BorrowDetailViewDTO;
import com.nkraft.budget.dto.BorrowSummaryDTO;
import com.nkraft.budget.dto.BorrowViewDTO;
import com.nkraft.budget.dto.RepaymentCreateDTO;
import com.nkraft.budget.entity.Account;
import com.nkraft.budget.entity.Borrow;
import com.nkraft.budget.entity.Repayment;
import com.nkraft.budget.repository.BorrowRepository;
import com.nkraft.budget.repository.RepaymentRepository;
import com.nkraft.user.entity.NkraftUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final RepaymentRepository repaymentRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<BorrowViewDTO> getBorrowsForUser(NkraftUser user) {
        List<Borrow> borrows = borrowRepository.findByNkraftUserAndIsDeletedFalseOrderByBorrowIdDesc(user);
        return borrows.stream()
                .map(this::mapToBorrowViewDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BorrowDetailViewDTO getBorrowDetail(Long borrowId, NkraftUser user) {
        Borrow borrow = getBorrowById(borrowId, user);
        BorrowViewDTO borrowViewDTO = mapToBorrowViewDTO(borrow);

        // 1. この借入に紐づく返済予定リストを取得
        var repaymentTransactions = transactionService.getPlannedTransactionsForBorrow(borrow);

        // 2. メイン口座の取引予定リストを取得
        Optional<Account> mainAccountOpt = accountService.findMainAccountByUserId(user.getId());
        if (mainAccountOpt.isEmpty()) {
            return new BorrowDetailViewDTO(borrowViewDTO, repaymentTransactions, BigDecimal.ZERO, Collections.emptyList());
        }

        Account mainAccount = mainAccountOpt.get();
        var mainAccountPlannedTransactions = transactionService.getPlannedTransactionsForAccountAsDTO(user, mainAccount);
        return new BorrowDetailViewDTO(borrowViewDTO, repaymentTransactions, mainAccount.getBalance(), mainAccountPlannedTransactions);
    }

    @Transactional
    public void createBorrow(BorrowCreateDTO dto, NkraftUser user) {
        Account depositAccount = accountService.getAccountById(dto.getDepositAccountId());

        // 1. borrowsテーブルにレコード作成
        Borrow borrow = new Borrow();
        borrow.setNkraftUser(user);
        borrow.setBorrowName(dto.getBorrowName());
        borrow.setTotalAmount(dto.getTotalAmount());
        borrow.setRepaidAmount(BigDecimal.ZERO);
        borrow.setIsDeleted(false);
        Borrow savedBorrow = borrowRepository.save(borrow);

        // 2. 自動で「入金」取引を作成
        String memo = String.format("「%s」からの借入", dto.getBorrowName());
        transactionService.createAndCompleteTransaction(
                user,
                depositAccount,
                "入金",
                null, // category
                LocalDate.now(), // 借入日は本日とする
                dto.getTotalAmount(),
                memo,
                savedBorrow,
                null
        );
    }

    @Transactional
    public void createRepayment(Long borrowId, RepaymentCreateDTO dto, NkraftUser user) {
        Borrow borrow = getBorrowById(borrowId, user);
        Account sourceAccount = accountService.getAccountById(dto.getSourceAccountId());
        // 「借入返済」カテゴリを取得。なければnull
        var repaymentCategory = categoryService.findByNameAndUser("借入返済", user).orElse(null);

        // 1. 「支出」「予定」の取引を作成
        var transaction = transactionService.createTransaction(
                user,
                sourceAccount,
                transactionService.getExpenditureTransactionType(),
                repaymentCategory,
                dto.getRepaymentDate(),
                dto.getRepaymentAmount(),
                dto.getMemo(),
                null, // 繰り返し予定ではない
                borrow, // 借入と紐付け
                null
        );

        // 2. repaymentsテーブルにレコード作成
        Repayment repayment = new Repayment();
        repayment.setBorrow(borrow);
        repayment.setTransaction(transaction);
        repaymentRepository.save(repayment);
    }

    @Transactional
    public Map<String, Object> completeRepayment(Long transactionId, BigDecimal actualAmount, NkraftUser user) {
        var completedTransaction = transactionService.completeTransaction(transactionId, actualAmount, user);
        Borrow borrow = completedTransaction.getBorrow();
        BorrowViewDTO updatedBorrowDTO = null;

        // The borrow amount is already updated inside transactionService.completeTransaction
        if (borrow != null) {
            getBorrowById(borrow.getBorrowId(), user); // 所有者チェック
            updatedBorrowDTO = mapToBorrowViewDTO(borrow);
        }

        return Map.of("transaction", completedTransaction, "borrowViewDTO", updatedBorrowDTO);
    }

    @Transactional(readOnly = true)
    public BorrowViewDTO getBorrowViewDTO(Long borrowId, NkraftUser user) {
        Borrow borrow = getBorrowById(borrowId, user);
        return mapToBorrowViewDTO(borrow);
    }

    private BorrowViewDTO mapToBorrowViewDTO(Borrow borrow) {
        BigDecimal total = borrow.getTotalAmount();
        BigDecimal repaid = borrow.getRepaidAmount();
        BigDecimal rate = BigDecimal.ZERO;

        if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            rate = repaid.divide(total, 4, RoundingMode.HALF_UP);
        }

        int ratePercent = rate.multiply(BigDecimal.valueOf(100)).intValue();
        boolean isCompleted = rate.compareTo(BigDecimal.ONE) >= 0;

        BorrowSummaryDTO summaryDTO = new BorrowSummaryDTO(
                borrow.getBorrowId(),
                borrow.getBorrowName(),
                borrow.getTotalAmount(),
                borrow.getRepaidAmount()
        );

        return new BorrowViewDTO(summaryDTO, rate, ratePercent, isCompleted);
    }

    private Borrow getBorrowById(Long borrowId, NkraftUser user) {
        return borrowRepository.findByBorrowIdAndNkraftUserAndIsDeletedFalse(borrowId, user)
                .orElseThrow(() -> new EntityNotFoundException("Borrow not found with id: " + borrowId));
    }
}