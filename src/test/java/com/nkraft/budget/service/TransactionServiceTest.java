package com.nkraft.budget.service;

import com.nkraft.budget.entity.*;
import com.nkraft.budget.repository.TransactionRepository;
import com.nkraft.user.entity.NkraftUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private NkraftUser user;
    private NkraftUser anotherUser;
    private Account account;
    private Transaction plannedWithdrawal;
    private Transaction plannedDeposit;
    private BudgetTransactionType withdrawalType;
    private BudgetTransactionType depositType;

    @BeforeEach
    void setUp() {
        user = new NkraftUser();
        user.setId(1L);

        anotherUser = new NkraftUser();
        anotherUser.setId(2L);

        account = new Account();
        account.setAccountId(1L);
        account.setNkraftUser(user);
        account.setBalance(new BigDecimal("100000"));

        withdrawalType = new BudgetTransactionType();
        withdrawalType.setId(1L);
        withdrawalType.setName("出金");

        depositType = new BudgetTransactionType();
        depositType.setId(2L);
        depositType.setName("入金");

        plannedWithdrawal = new Transaction();
        plannedWithdrawal.setId(1L);
        plannedWithdrawal.setUser(user);
        plannedWithdrawal.setAccount(account);
        plannedWithdrawal.setBudgetTransactionType(withdrawalType);
        plannedWithdrawal.setPlannedAmount(new BigDecimal("10000"));
        plannedWithdrawal.setTransactionStatus(TransactionStatus.PLANNED);
        plannedWithdrawal.setIsDeleted(false);

        plannedDeposit = new Transaction();
        plannedDeposit.setId(2L);
        plannedDeposit.setUser(user);
        plannedDeposit.setAccount(account);
        plannedDeposit.setBudgetTransactionType(depositType);
        plannedDeposit.setPlannedAmount(new BigDecimal("50000"));
        plannedDeposit.setTransactionStatus(TransactionStatus.PLANNED);
        plannedDeposit.setIsDeleted(false);
    }

    @Nested
    @DisplayName("completeTransactionメソッドのテスト")
    class CompleteTransactionTests {

        @Test
        @DisplayName("出金取引を正常に完了し残高が減算されること")
        void completeTransaction_Withdraw_Success() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));
            BigDecimal actualAmount = new BigDecimal("9500");

            Transaction result = transactionService.completeTransaction(1L, actualAmount, user);

            assertNotNull(result);
            assertEquals(TransactionStatus.COMPLETED, result.getTransactionStatus());
            assertEquals(actualAmount, result.getActualAmount());
            assertEquals(new BigDecimal("90500"), result.getAccount().getBalance()); // 100000 - 9500
            verify(transactionRepository).findById(1L);
        }

        @Test
        @DisplayName("入金取引を正常に完了し残高が増加されること")
        void completeTransaction_Deposit_Success() {
            when(transactionRepository.findById(2L)).thenReturn(Optional.of(plannedDeposit));
            BigDecimal actualAmount = new BigDecimal("51000");

            Transaction result = transactionService.completeTransaction(2L, actualAmount, user);

            assertNotNull(result);
            assertEquals(TransactionStatus.COMPLETED, result.getTransactionStatus());
            assertEquals(actualAmount, result.getActualAmount());
            assertEquals(new BigDecimal("151000"), result.getAccount().getBalance()); // 100000 + 51000
            verify(transactionRepository).findById(2L);
        }

        @Test
        @DisplayName("実績額がnullの場合、予定額で完了処理が行われること")
        void completeTransaction_WithNullActualAmount_ShouldUsePlannedAmount() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));

            Transaction result = transactionService.completeTransaction(1L, null, user);

            assertNotNull(result);
            assertEquals(TransactionStatus.COMPLETED, result.getTransactionStatus());
            assertEquals(plannedWithdrawal.getPlannedAmount(), result.getActualAmount());
            assertEquals(new BigDecimal("90000"), result.getAccount().getBalance()); // 100000 - 10000
            verify(transactionRepository).findById(1L);
        }

        @Test
        @DisplayName("存在しない取引IDの場合、EntityNotFoundExceptionがスローされること")
        void completeTransaction_NotFound_ShouldThrowException() {
            when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> transactionService.completeTransaction(99L, new BigDecimal("100"), user));
        }

        @Test
        @DisplayName("他人の取引を完了しようとした場合、SecurityExceptionがスローされること")
        void completeTransaction_PermissionDenied_ShouldThrowException() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));

            assertThrows(SecurityException.class, () -> transactionService.completeTransaction(1L, new BigDecimal("100"), anotherUser));
        }

        @Test
        @DisplayName("既に完了済みの取引の場合、IllegalStateExceptionがスローされること")
        void completeTransaction_AlreadyCompleted_ShouldThrowException() {
            plannedWithdrawal.setTransactionStatus(TransactionStatus.COMPLETED);
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));

            assertThrows(IllegalStateException.class, () -> transactionService.completeTransaction(1L, new BigDecimal("100"), user));
        }
    }

    @Test
    @DisplayName("取引情報を正常に更新できること")
    void updateTransaction_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));
        LocalDate newDate = LocalDate.now().plusDays(5);
        BigDecimal newAmount = new BigDecimal("12000");
        String newMemo = "Updated Memo";

        transactionService.updateTransaction(1L, user, newDate, newAmount, withdrawalType, null, newMemo);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();

        assertEquals(newDate, savedTransaction.getTransactionDate());
        assertEquals(newAmount, savedTransaction.getPlannedAmount());
        assertEquals(newMemo, savedTransaction.getMemo());
    }

    @Test
    @DisplayName("完了済みの取引を更新しようとした場合、IllegalStateExceptionがスローされること")
    void updateTransaction_Completed_ShouldThrowException() {
        plannedWithdrawal.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));

        assertThrows(IllegalStateException.class, () ->
                transactionService.updateTransaction(1L, user, LocalDate.now(), BigDecimal.ONE, withdrawalType, null, "memo"));
    }

    @Test
    @DisplayName("取引を正常に論理削除できること")
    void deleteTransaction_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));

        transactionService.deleteTransaction(1L, user);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        assertTrue(transactionCaptor.getValue().getIsDeleted());
    }

    @Test
    @DisplayName("他人の取引を削除しようとした場合、SecurityExceptionがスローされること")
    void deleteTransaction_PermissionDenied_ShouldThrowException() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));

        assertThrows(SecurityException.class, () -> transactionService.deleteTransaction(1L, anotherUser));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("完了済みの取引を削除しようとした場合、IllegalStateExceptionがスローされること")
    void deleteTransaction_Completed_ShouldThrowException() {
        plannedWithdrawal.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));

        assertThrows(IllegalStateException.class, () -> transactionService.deleteTransaction(1L, user));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("差額貯金の振替取引を正常に作成できること")
    void createSavingsTransfer_Success() {
        Account savingsAccount = new Account();
        savingsAccount.setAccountId(2L);
        savingsAccount.setBalance(new BigDecimal("50000"));
        BigDecimal difference = new BigDecimal("500");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(plannedWithdrawal));

        transactionService.createSavingsTransfer(user, 1L, savingsAccount, difference, withdrawalType, depositType);

        verify(transactionRepository).findById(1L);
        // 「振替」と「入金」の2つの取引が保存される
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        // 貯金口座の残高が更新されている
        assertEquals(new BigDecimal("50500"), savingsAccount.getBalance());
    }
}