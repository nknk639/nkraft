package com.nkraft.budget.controller;

import com.nkraft.budget.dto.BorrowCreateDTO;
import com.nkraft.budget.dto.GoalCreateDTO;
import com.nkraft.budget.dto.RepaymentCreateDTO;
import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.service.AccountService;
import com.nkraft.budget.service.BudgetTransactionTypeService;
import com.nkraft.budget.service.CategoryService;
import com.nkraft.budget.service.BorrowService;
import com.nkraft.budget.service.GoalService;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/budget/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final GoalService goalService;
    private final AccountService accountService;
    private final BudgetTransactionTypeService budgetTransactionTypeService;
    private final CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(BorrowController.class);

    @GetMapping
    public String showBorrowList(Model model, Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        model.addAttribute("borrows", borrowService.getBorrowsForUser(currentUser));
        model.addAttribute("goals", goalService.getGoalsForUser(currentUser));
        model.addAttribute("accounts", accountService.getAccountsByUserIdAsDTO(currentUser.getId()));
        model.addAttribute("borrowCreateDTO", new BorrowCreateDTO());
        model.addAttribute("goalCreateDTO", new GoalCreateDTO());

        return "budget/borrows";
    }

    @PostMapping("/create")
    public String createBorrow(@ModelAttribute BorrowCreateDTO borrowCreateDTO,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        borrowService.createBorrow(borrowCreateDTO, currentUser);
        redirectAttributes.addFlashAttribute("message", "新しい借入を登録しました。");
        return "redirect:/budget/borrows";
    }

    @PostMapping("/create-goal")
    public String createGoal(@ModelAttribute GoalCreateDTO goalCreateDTO,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        goalService.createGoal(goalCreateDTO, currentUser);
        redirectAttributes.addFlashAttribute("message", "新しい目標を登録しました。");
        return "redirect:/budget/borrows";
    }

    @GetMapping("/{borrowId}")
    public String showBorrowDetail(@PathVariable Long borrowId, Model model, Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        var borrowDetail = borrowService.getBorrowDetail(borrowId, currentUser);

        model.addAttribute("borrowDetail", borrowDetail);
        model.addAttribute("accounts", accountService.getAccountsByUserIdAsDTO(currentUser.getId()));
        model.addAttribute("repaymentCreateDTO", new RepaymentCreateDTO());
        // Add data needed for transaction modals and scripts
        model.addAttribute("transactionTypes", budgetTransactionTypeService.getAllTransactionTypesAsDTO());
        model.addAttribute("categories", categoryService.getCategoriesByUserIdAsDTO(currentUser.getId()));
        model.addAttribute("savingsAccounts", accountService.findSavingsAccountsByUserIdAsDTO(currentUser.getId()));

        model.addAttribute("pageTitle", borrowDetail.getBorrow().getBorrow().getBorrowName());

        return "budget/borrows/detail";
    }

    @PostMapping("/{borrowId}/repayments")
    public String createRepayment(@PathVariable Long borrowId,
                                  @ModelAttribute RepaymentCreateDTO repaymentCreateDTO,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        borrowService.createRepayment(borrowId, repaymentCreateDTO, currentUser);
        redirectAttributes.addFlashAttribute("message", "返済予定を登録しました。");
        return "redirect:/budget/borrows/" + borrowId;
    }

    @PostMapping("/repayments/complete")
    @ResponseBody
    public ResponseEntity<?> completeRepayment(
            @RequestParam("transactionId") Long transactionId,
            @RequestParam(value = "actualAmount", required = false) java.math.BigDecimal actualAmount,
            Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        try {
            var result = borrowService.completeRepayment(transactionId, actualAmount, currentUser);
            Transaction completedTransaction = (Transaction) result.get("transaction");
            return ResponseEntity.ok(java.util.Map.of("success", true, "newBalance", completedTransaction.getAccount().getBalance(), "updatedBorrow", result.get("borrowViewDTO")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }
}