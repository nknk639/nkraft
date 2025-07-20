package com.nkraft.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nkraft.budget.dto.TransactionDateUpdateDTO;
import com.nkraft.budget.entity.Account;
import com.nkraft.budget.entity.BudgetTransactionType;
import com.nkraft.budget.entity.Category;
import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.service.*;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import com.nkraft.user.service.UserDetailsServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;
    @MockBean
    private BudgetTransactionTypeService budgetTransactionTypeService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private RecurringTransactionService recurringTransactionService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService; // Spring Securityの依存関係で必要

    private NkraftUser testUser;
    private LoginUserDetails testUserDetails;
    private Account mainAccount;

    @BeforeEach
    void setUp() {
        testUser = new NkraftUser();
        testUser.setId(1L);
        testUser.setUsername("test");
        testUser.setPassword("password");

        testUserDetails = new LoginUserDetails(testUser);

        mainAccount = new Account();
        mainAccount.setAccountId(1L);
        mainAccount.setAccountName("メイン口座");
        mainAccount.setBalance(new BigDecimal("100000"));
        mainAccount.setIsMain(true);
        mainAccount.setNkraftUser(testUser);
    }

    @Test
    void showDashboard_shouldReturnDashboardViewWithData() throws Exception {
        // Given
        when(accountService.getAccountsByUserId(anyLong())).thenReturn(List.of(mainAccount));
        when(budgetTransactionTypeService.getAllTransactionTypes()).thenReturn(Collections.emptyList());
        when(categoryService.getCategoriesByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(accountService.findSavingsAccountsByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(accountService.findMainAccountByUserId(anyLong())).thenReturn(Optional.of(mainAccount));
        when(transactionService.getPlannedTransactionsForAccountAsDTO(any(NkraftUser.class), any(Account.class))).thenReturn(Collections.emptyList());
        when(recurringTransactionService.getRecurringTransactionsForUser(any(NkraftUser.class))).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/budget/").with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("budget/dashboard"))
                .andExpect(model().attributeExists("accounts", "transactionTypes", "categories", "savingsAccounts", "mainAccountBalance", "transactions", "recurringTransactions"));
    }

    @Test
    void showDashboard_whenNoMainAccount_shouldReturnDashboardView() throws Exception {
        // Given
        when(accountService.getAccountsByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(accountService.findMainAccountByUserId(anyLong())).thenReturn(Optional.empty());
        when(budgetTransactionTypeService.getAllTransactionTypes()).thenReturn(Collections.emptyList());
        when(categoryService.getCategoriesByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(accountService.findSavingsAccountsByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(recurringTransactionService.getRecurringTransactionsForUser(any(NkraftUser.class))).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/budget/").with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("budget/dashboard"))
                .andExpect(model().attribute("transactions", Collections.emptyList()));
    }

    @Test
    void createTransaction_shouldCreateTransactionAndRedirect() throws Exception {
        // Given
        when(accountService.getAccountById(anyLong())).thenReturn(new Account());
        when(budgetTransactionTypeService.getTransactionTypeById(anyLong())).thenReturn(new BudgetTransactionType());
        when(categoryService.getCategoryById(anyLong())).thenReturn(new Category());

        // When & Then
        mockMvc.perform(post("/budget/transactions")
                        .with(csrf())
                        .with(user(testUserDetails))
                        .param("accountId", "1")
                        .param("transactionDate", "2023-01-01")
                        .param("amount", "5000")
                        .param("budgetTransactionTypeId", "1")
                        .param("categoryId", "1")
                        .param("memo", "Test Memo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budget/"))
                .andExpect(flash().attribute("message", "取引を登録しました。"));

        verify(transactionService, times(1)).createTransaction(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void completeTransaction_shouldReturnSuccessJson() throws Exception {
        // Given
        Transaction completedTransaction = new Transaction();
        completedTransaction.setAccount(mainAccount);
        BudgetTransactionType type = new BudgetTransactionType();
        type.setName("出金");
        completedTransaction.setBudgetTransactionType(type);
        completedTransaction.setPlannedAmount(new BigDecimal("1000"));
        completedTransaction.setActualAmount(new BigDecimal("800"));

        when(transactionService.completeTransaction(anyLong(), any(), any(NkraftUser.class))).thenReturn(completedTransaction);

        // When & Then
        mockMvc.perform(post("/budget/transactions/complete")
                        .with(csrf())
                        .with(user(testUserDetails))
                        .param("transactionId", "1")
                        .param("actualAmount", "800"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.newBalance").value(100000))
                .andExpect(jsonPath("$.savingsDifference").value(200));
    }

    @Test
    void completeTransaction_whenServiceThrowsException_shouldReturnBadRequest() throws Exception {
        // Given
        when(transactionService.completeTransaction(anyLong(), any(), any(NkraftUser.class)))
                .thenThrow(new EntityNotFoundException("Transaction not found"));

        // When & Then
        mockMvc.perform(post("/budget/transactions/complete")
                        .with(csrf())
                        .with(user(testUserDetails))
                        .param("transactionId", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    @Test
    void createSavingsTransfer_shouldReturnSuccessJson() throws Exception {
        // Given
        when(accountService.getAccountById(anyLong())).thenReturn(new Account());
        when(budgetTransactionTypeService.getTransactionTypeByName(anyString())).thenReturn(new BudgetTransactionType());
        doNothing().when(transactionService).createSavingsTransfer(any(), anyLong(), any(), any(), any(), any());

        // When & Then
        mockMvc.perform(post("/budget/transactions/create-savings-transfer")
                        .with(csrf())
                        .with(user(testUserDetails))
                        .param("transactionId", "1")
                        .param("savingsAccountId", "2")
                        .param("differenceAmount", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("差額貯金への振替を登録しました。"));
    }

    @Test
    void deleteTransaction_shouldReturnSuccessJson() throws Exception {
        // Given
        doNothing().when(transactionService).deleteTransaction(anyLong(), any(NkraftUser.class));

        // When & Then
        mockMvc.perform(delete("/budget/transactions/{transactionId}", 1L)
                        .with(csrf())
                        .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(transactionService, times(1)).deleteTransaction(eq(1L), any(NkraftUser.class));
    }

    @Test
    void deleteTransaction_whenServiceThrowsException_shouldReturnBadRequest() throws Exception {
        // Given
        doThrow(new SecurityException("Permission denied")).when(transactionService).deleteTransaction(anyLong(), any(NkraftUser.class));

        // When & Then
        mockMvc.perform(delete("/budget/transactions/{transactionId}", 1L)
                        .with(csrf())
                        .with(user(testUserDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Permission denied"));
    }

    @Test
    void deleteTransaction_whenTransactionIsCompleted_shouldReturnBadRequest() throws Exception {
        // Given
        doThrow(new IllegalStateException("完了済みの取引は削除できません。")).when(transactionService).deleteTransaction(anyLong(), any(NkraftUser.class));

        // When & Then
        mockMvc.perform(delete("/budget/transactions/{transactionId}", 1L)
                        .with(csrf())
                        .with(user(testUserDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("完了済みの取引は削除できません。"));
    }

    @Test
    void updateTransactionDates_shouldReturnSuccessJson() throws Exception {
        // Given
        TransactionDateUpdateDTO dto = new TransactionDateUpdateDTO();
        dto.setTransactionId(1L);
        dto.setNewDate(LocalDate.now());
        List<TransactionDateUpdateDTO> dtoList = List.of(dto);

        doNothing().when(transactionService).updateTransactionDates(anyList(), any(NkraftUser.class));

        // When & Then
        mockMvc.perform(post("/budget/transactions/update-dates")
                        .with(csrf())
                        .with(user(testUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}