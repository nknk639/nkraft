package com.nkraft.budget.controller;

import com.nkraft.budget.entity.Account;
import com.nkraft.budget.entity.BudgetTransactionType;
import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.entity.Category;
import com.nkraft.budget.dto.TransactionDTO;
import com.nkraft.budget.service.AccountService;
import com.nkraft.budget.service.BudgetTransactionTypeService;
import com.nkraft.budget.service.TransactionService;
import com.nkraft.budget.service.CategoryService;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

/**
 * 予算管理ダッシュボードを表示するコントローラー。
 * 画面仕様 3.2 予算管理モジュールの画面一覧 に基づいています。
 */
@Controller
@RequestMapping("/budget") // URLのベースパス
@RequiredArgsConstructor // Lombokのアノテーション。コンストラクタを自動生成
public class DashboardController {

    // サービスをインジェクション（注入）
    private final AccountService accountService;
    private final BudgetTransactionTypeService budgetTransactionTypeService;
    private final CategoryService categoryService;
    private final TransactionService transactionService;

    /**
     * ダッシュボード画面を表示します。
     *
     * @param model     Thymeleafテンプレートに渡すデータを格納するModelオブジェクト
     * @param principal 認証されたユーザー情報を持つPrincipalオブジェクト
     * @return テンプレート名 ("budget/dashboard")
     */
    @GetMapping("/") // "/budget/" へのGETリクエストを処理
    public String showDashboard(Model model, Authentication principal) {

        // 1. ログインユーザーの情報を取得
        LoginUserDetails userDetails = (LoginUserDetails) principal.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        Long userId = currentUser.getId();

        // 2. 必要なデータを取得し、Modelに追加

        // 2.1 口座情報
        List<Account> accounts = accountService.getAccountsByUserId(userId);
        model.addAttribute("accounts", accounts);

        // 2.2 取引種別リスト
        List<BudgetTransactionType> transactionTypes = budgetTransactionTypeService.getAllTransactionTypes();
        model.addAttribute("transactionTypes", transactionTypes);

        // 2.3 カテゴリリスト
        List<Category> categories = categoryService.getCategoriesByUserId(userId);
        model.addAttribute("categories", categories);

        // 2.4 F-B09(差額貯金)用の貯金口座リスト
         List<Account> savingsAccounts = accountService.findSavingsAccountsByUserId(userId);
         model.addAttribute("savingsAccounts", savingsAccounts);

        // 2.5 メイン口座の取引予定と残高を取得 (F-B01, F-B03)
         Optional<Account> mainAccount = accountService.findMainAccountByUserId(userId);
         if (mainAccount.isPresent()) {
             model.addAttribute("mainAccountBalance", mainAccount.get().getBalance());
             List<TransactionDTO> transactionDTOs = transactionService.getPlannedTransactionsForAccountAsDTO(currentUser, mainAccount.get());
             model.addAttribute("transactions", transactionDTOs);
         } else {
             // メイン口座がない場合は、空のリストを渡す
             model.addAttribute("transactions", Collections.emptyList());
         }

        return "budget/dashboard"; // "templates/budget/dashboard.html" を表示
    }

    /**
     * 取引クイック登録フォームから送信されたデータを処理し、新しい取引を登録します。
     * @param accountId 口座ID
     * @param transactionDate 取引日
     * @param amount 金額
     * @param budgetTransactionTypeId 取引種別ID
     * @param categoryId カテゴリID
     * @param memo メモ
     * @param principal 認証済みユーザー情報
     * @param redirectAttributes リダイレクト時の属性
     * @return リダイレクト先URL
     */
    @PostMapping("/transactions")
    public String createTransaction(
            @RequestParam("accountId") Long accountId,
            @RequestParam("transactionDate") String transactionDate,
            @RequestParam("amount") BigDecimal plannedAmount,
            @RequestParam("budgetTransactionTypeId") Long budgetTransactionTypeId,
            @RequestParam(value = "categoryId", required = false) Long categoryId, // 未分類の場合はnullを許容
            @RequestParam("memo") String memo,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        // 各IDから関連エンティティを取得
        Account account = accountService.getAccountById(accountId);
        BudgetTransactionType budgetTransactionType = budgetTransactionTypeService.getTransactionTypeById(budgetTransactionTypeId);
        Category category = (categoryId != null) ? categoryService.getCategoryById(categoryId) : null;

        // 取引登録サービスを呼び出し
        transactionService.createTransaction(currentUser, account, budgetTransactionType, category, LocalDate.parse(transactionDate), plannedAmount, memo);

        redirectAttributes.addFlashAttribute("message", "取引を登録しました。");
        return "redirect:/budget/";
    }
}