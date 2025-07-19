package com.nkraft.budget.controller;

import com.nkraft.budget.entity.RecurringTransaction;
import com.nkraft.budget.service.RecurringTransactionService;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/budget/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public String showRecurringTransactions(Model model, Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        model.addAttribute("weekdays", new String[]{"日", "月", "火", "水", "木", "金", "土"});
        model.addAttribute("recurringTransactions", recurringTransactionService.getRecurringTransactionsForUser(currentUser));
        return "budget/recurring";
    }
}