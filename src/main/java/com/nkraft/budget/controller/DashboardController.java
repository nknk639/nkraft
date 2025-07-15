package com.nkraft.budget.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nkraft.budget.entity.Account;

import java.math.BigDecimal;

@Controller
@RequestMapping("/budget")
public class DashboardController {

    @GetMapping("/")
    public String showDashboard(Model model) {
        // TODO: データベースからデータを取得するロジックを実装する
        // (現在は仮のデータを使用)
        Account mainAccount = new Account();
        mainAccount.setAccountName("メイン口座");
        mainAccount.setBalance(new BigDecimal("1234567.89"));

        model.addAttribute("account", mainAccount);

        // TODO: 他のデータ（取引予定、繰り返し予定、グラフデータ、目標・借入）も同様に取得してModelに追加する

        return "budget/dashboard"; // "templates/budget/dashboard.html" を表示
    }

}