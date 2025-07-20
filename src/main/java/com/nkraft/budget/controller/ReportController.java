package com.nkraft.budget.controller;

import com.nkraft.budget.dto.ReportDTO;
import com.nkraft.budget.service.ReportService;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;

@Controller
@RequestMapping("/budget")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/report")
    public String showReport(
            @RequestParam(name = "target", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth targetMonth,
            Model model,
            Authentication authentication) {

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        YearMonth currentMonth = (targetMonth == null) ? YearMonth.now() : targetMonth;
        model.addAttribute("reportData", reportService.generateReport(currentUser, currentMonth));
        return "budget/report";
    }
}