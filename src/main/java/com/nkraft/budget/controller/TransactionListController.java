package com.nkraft.budget.controller;

import com.nkraft.budget.dto.TransactionSearchForm;
import com.nkraft.budget.dto.TransactionUpdateDTO;
import com.nkraft.budget.entity.BudgetTransactionType;
import com.nkraft.budget.entity.Category;
import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.service.AccountService;
import com.nkraft.budget.service.BudgetTransactionTypeService;
import com.nkraft.budget.service.CategoryService;
import com.nkraft.budget.service.TransactionService;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/budget/transactions")
@RequiredArgsConstructor
public class TransactionListController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final BudgetTransactionTypeService budgetTransactionTypeService;

    @GetMapping
    public String listTransactions(@ModelAttribute("searchForm") TransactionSearchForm form,
                                   Pageable pageable,
                                   Model model, Authentication authentication) {

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        Page<Transaction> transactionPage = transactionService.searchTransactions(form, currentUser, pageable);
        model.addAttribute("page", transactionPage);

        model.addAttribute("accounts", accountService.getAccountsByUserId(currentUser.getId()));
        model.addAttribute("categories", categoryService.getCategoriesByUserId(currentUser.getId()));
        model.addAttribute("transactionTypes", budgetTransactionTypeService.getAllTransactionTypes());

        return "budget/transactions";
    }

    /**
     * 取引編集フォームから送信されたデータを処理し、取引を更新します。
     * @param updateDTO 取引更新情報
     * @param redirectUrl リダイレクト先のURL
     * @param authentication 認証済みユーザー情報
     * @param redirectAttributes リダイレクト時の属性
     * @return リダイレクト先URL
     */
    @PostMapping("/update")
    public String updateTransaction(
            @ModelAttribute TransactionUpdateDTO updateDTO,
            @RequestParam(name = "redirectUrl", defaultValue = "/budget/transactions") String redirectUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        try {
            BudgetTransactionType budgetTransactionType = budgetTransactionTypeService.getTransactionTypeById(updateDTO.getBudgetTransactionTypeId());
            Category category = (updateDTO.getCategoryId() != null) ? categoryService.getCategoryById(updateDTO.getCategoryId()) : null;

            transactionService.updateTransaction(
                    updateDTO.getTransactionId(), currentUser, LocalDate.parse(updateDTO.getTransactionDate()),
                    updateDTO.getPlannedAmount(), budgetTransactionType, category, updateDTO.getMemo());
            redirectAttributes.addFlashAttribute("message", "取引を更新しました。");
        } catch (EntityNotFoundException | SecurityException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }
}