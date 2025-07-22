package com.nkraft.budget.controller;

import com.nkraft.budget.dto.RecurringTransactionCreateDTO;
import com.nkraft.budget.dto.RecurringTransactionUpdateDTO;
import com.nkraft.budget.service.AccountService;
import com.nkraft.budget.service.BudgetTransactionTypeService;
import com.nkraft.budget.service.CategoryService;
import com.nkraft.budget.service.RecurringTransactionService;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/budget/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;
    private final AccountService accountService;
    private final BudgetTransactionTypeService budgetTransactionTypeService;
    private final CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(RecurringTransactionController.class);

    @GetMapping
    public String showRecurringTransactions(Model model, Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        Long userId = currentUser.getId();

        // Data for the list view
        model.addAttribute("weekdays", new String[]{"日", "月", "火", "水", "木", "金", "土"});
        model.addAttribute("recurringTransactions", recurringTransactionService.getRecurringTransactionsForUser(currentUser));

        // Data for JavaScript (Edit Modal)
        model.addAttribute("recurringTransactionsJs", recurringTransactionService.getRecurringTransactionsForUserAsJsDTO(currentUser));

        // Data for the "Create New" modal
        model.addAttribute("accounts", accountService.getAccountsByUserIdAsDTO(userId));
        model.addAttribute("transactionTypes", budgetTransactionTypeService.getAllTransactionTypesAsDTO());
        model.addAttribute("categories", categoryService.getCategoriesByUserIdAsDTO(userId));

        return "budget/recurring";
    }

    @PostMapping("/create")
    public String createRecurringTransaction(@ModelAttribute RecurringTransactionCreateDTO createDTO,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        try {
            recurringTransactionService.createRecurringTransaction(createDTO, currentUser);
            redirectAttributes.addFlashAttribute("message", "繰り返し予定を登録しました。");
        } catch (Exception e) {
            logger.error("Error creating recurring transaction", e);
            redirectAttributes.addFlashAttribute("errorMessage", "登録中にエラーが発生しました。");
        }

        return "redirect:/budget/recurring";
    }

    @PostMapping("/{id}/execute")
    public String executeRecurringTransaction(@PathVariable Long id,
                                              @RequestParam(name = "source", defaultValue = "recurring") String source,
                                              Authentication authentication,
                                              RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        try {
            recurringTransactionService.executeRecurringTransaction(id, currentUser);
            redirectAttributes.addFlashAttribute("message", "繰り返し予定を実行し、新しい取引を作成しました。");
        } catch (Exception e) {
            logger.error("Error executing recurring transaction with id: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "繰り返し予定の実行中にエラーが発生しました。");
        }

        String redirectUrl = "/budget/recurring"; // デフォルト
        if ("dashboard".equals(source)) {
            redirectUrl = "/budget/";
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/update/{id}")
    public String updateRecurringTransaction(@PathVariable Long id,
                                             @ModelAttribute RecurringTransactionUpdateDTO updateDTO,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        try {
            recurringTransactionService.updateRecurringTransaction(id, updateDTO, currentUser);
            redirectAttributes.addFlashAttribute("message", "繰り返し予定を更新しました。");
        } catch (Exception e) {
            logger.error("Error updating recurring transaction with id: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "更新中にエラーが発生しました。");
        }
        return "redirect:/budget/recurring";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteRecurringTransaction(@PathVariable Long id,
                                                        Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        try {
            recurringTransactionService.deleteRecurringTransaction(id, currentUser);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (EntityNotFoundException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "サーバーエラーが発生しました。"));
        }
    }
}