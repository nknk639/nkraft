package com.nkraft.budget.controller;

import com.nkraft.budget.dto.GoalDetailViewDTO;
import com.nkraft.budget.dto.SavingCreateDTO;
import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.service.AccountService;
import com.nkraft.budget.service.BudgetTransactionTypeService;
import com.nkraft.budget.service.CategoryService;
import com.nkraft.budget.service.GoalService;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/budget/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final AccountService accountService;
    private final BudgetTransactionTypeService budgetTransactionTypeService;
    private final CategoryService categoryService;

    @GetMapping("/{goalId}")
    public String showGoalDetail(@PathVariable Long goalId, Model model, Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        GoalDetailViewDTO goalDetail = goalService.getGoalDetail(goalId, currentUser);

        model.addAttribute("goalDetail", goalDetail);
        model.addAttribute("accounts", accountService.getAccountsByUserIdAsDTO(currentUser.getId()));
        model.addAttribute("savingCreateDTO", new SavingCreateDTO());
        // Add data needed for transaction modals and scripts
        model.addAttribute("transactionTypes", budgetTransactionTypeService.getAllTransactionTypesAsDTO());
        model.addAttribute("categories", categoryService.getCategoriesByUserIdAsDTO(currentUser.getId()));
        model.addAttribute("savingsAccounts", accountService.findSavingsAccountsByUserIdAsDTO(currentUser.getId()));

        model.addAttribute("pageTitle", goalDetail.getGoal().getGoal().getGoalName());

        return "budget/goals/detail";
    }

    @PostMapping("/{goalId}/savings")
    public String createSaving(@PathVariable Long goalId,
                               @ModelAttribute SavingCreateDTO savingCreateDTO,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        goalService.createSaving(goalId, savingCreateDTO, currentUser);
        redirectAttributes.addFlashAttribute("message", "貯金予定を登録しました。");
        return "redirect:/budget/goals/" + goalId;
    }

    @PostMapping("/savings/complete")
    @ResponseBody
    public ResponseEntity<?> completeSaving(
            @RequestParam("transactionId") Long transactionId,
            @RequestParam(value = "actualAmount", required = false) java.math.BigDecimal actualAmount,
            Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        try {
            var result = goalService.completeSaving(transactionId, actualAmount, currentUser);
            Transaction completedTransaction = (Transaction) result.get("transaction");
            return ResponseEntity.ok(java.util.Map.of("success", true, "newBalance", completedTransaction.getAccount().getBalance(), "updatedGoal", result.get("goalViewDTO")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }
}