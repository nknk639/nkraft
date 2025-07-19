package com.nkraft.budget.controller;

import com.nkraft.budget.entity.Account;
import com.nkraft.budget.entity.BudgetTransactionType;
import com.nkraft.budget.entity.Transaction;
import com.nkraft.budget.entity.Category;
import com.nkraft.budget.dto.RecurringTransactionViewDTO;
import com.nkraft.budget.dto.TransactionDateUpdateDTO;
import com.nkraft.budget.dto.TransactionDTO;
import com.nkraft.budget.service.AccountService;
import com.nkraft.budget.service.BudgetTransactionTypeService;
import com.nkraft.budget.service.TransactionService;
import com.nkraft.budget.service.CategoryService;
import com.nkraft.budget.service.RecurringTransactionService;
import com.nkraft.budget.dto.TransactionUpdateDTO; // DTOのインポートを追加
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // ModelAttributeのインポートを追加
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final RecurringTransactionService recurringTransactionService;

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

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

        // 2.6 繰り返し予定リスト
        model.addAttribute("recurringTransactions", recurringTransactionService.getRecurringTransactionsForUser(currentUser));

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
        transactionService.createTransaction(currentUser, account, budgetTransactionType, category, LocalDate.parse(transactionDate), plannedAmount, memo, null);

        redirectAttributes.addFlashAttribute("message", "取引を登録しました。");
        return "redirect:/budget/";
    }
    
    /**
     * 取引編集フォームから送信されたデータを処理し、取引を更新します。
     * @param updateDTO 取引更新情報
     * @param authentication 認証済みユーザー情報
     * @param redirectAttributes リダイレクト時の属性
     * @return リダイレクト先URL
     */
    @PostMapping("/transactions/update")
    public String updateTransaction(
            @ModelAttribute TransactionUpdateDTO updateDTO,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        // 各IDから関連エンティティを取得
        BudgetTransactionType budgetTransactionType = budgetTransactionTypeService.getTransactionTypeById(updateDTO.getBudgetTransactionTypeId());
        Category category = (updateDTO.getCategoryId() != null) ? categoryService.getCategoryById(updateDTO.getCategoryId()) : null;

        // 取引更新サービスを呼び出し
        try {
            transactionService.updateTransaction(
                    updateDTO.getTransactionId(),
                    currentUser,
                    LocalDate.parse(updateDTO.getTransactionDate()),
                    updateDTO.getPlannedAmount(),
                    budgetTransactionType,
                    category,
                    updateDTO.getMemo()
            );
            redirectAttributes.addFlashAttribute("message", "取引を更新しました。");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定された取引が見つかりませんでした。");
        }
        return "redirect:/budget/";
    }

    /**
     * 取引を完了ステータスに更新します。
     * このメソッドはAjaxリクエストから呼び出されることを想定しています。
     * @param transactionId 完了する取引のID
     * @param actualAmount 実績額 (任意)
     * @param authentication 認証情報
     * @return 処理結果を含むJSONレスポンス
     */
    @PostMapping("/transactions/complete")
    @ResponseBody
    public ResponseEntity<?> completeTransaction(
            @RequestParam("transactionId") Long transactionId,
            @RequestParam(value = "actualAmount", required = false) BigDecimal actualAmount,
            Authentication authentication) {

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        try {
            Transaction completedTransaction = transactionService.completeTransaction(transactionId, actualAmount, currentUser);
            Account updatedAccount = completedTransaction.getAccount();

            // F-B09: 差額貯金のロジック
            BigDecimal savingsDifference = BigDecimal.ZERO;
            // "出金"の場合のみ差額貯金の対象とする
            if ("出金".equals(completedTransaction.getBudgetTransactionType().getName())) {
                BigDecimal planned = completedTransaction.getPlannedAmount();
                BigDecimal actual = completedTransaction.getActualAmount();
                if (actual.compareTo(planned) < 0) {
                    savingsDifference = planned.subtract(actual);
                }
            }

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("newBalance", updatedAccount.getBalance());
            response.put("savingsDifference", savingsDifference);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException | SecurityException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "サーバーエラーが発生しました。"));
        }
    }

    /**
     * 差額貯金のための振替取引を作成します。
     * @param transactionId 元の取引ID
     * @param savingsAccountId 貯金先口座ID
     * @param differenceAmount 差額
     * @param authentication 認証情報
     * @return 処理結果
     */
    @PostMapping("/transactions/create-savings-transfer")
    @ResponseBody
    public ResponseEntity<?> createSavingsTransfer(
            @RequestParam("transactionId") Long transactionId,
            @RequestParam("savingsAccountId") Long savingsAccountId,
            @RequestParam("differenceAmount") BigDecimal differenceAmount,
            Authentication authentication) {

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        try {
            Account savingsAccount = accountService.getAccountById(savingsAccountId);
            BudgetTransactionType transferType = budgetTransactionTypeService.getTransactionTypeByName("振替");
            BudgetTransactionType depositType = budgetTransactionTypeService.getTransactionTypeByName("入金");

            transactionService.createSavingsTransfer(currentUser, transactionId, savingsAccount, differenceAmount, transferType, depositType);
            return ResponseEntity.ok(Map.of("success", true, "message", "差額貯金への振替を登録しました。"));
        } catch (Exception e) {
            logger.error("Error creating savings transfer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "差額貯金の処理中にエラーが発生しました。"));
        }
    }

    /**
     * 取引を論理削除します。
     * このメソッドはAjaxリクエストから呼び出されることを想定しています。
     * @param transactionId 削除する取引のID
     * @param authentication 認証情報
     * @return 処理結果を含むJSONレスポンス
     */
    @DeleteMapping("/transactions/{transactionId}")
    @ResponseBody
    public ResponseEntity<?> deleteTransaction(
            @PathVariable("transactionId") Long transactionId,
            Authentication authentication) {

        logger.info("[START] DELETE /transactions/{}", transactionId);

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        try {
            logger.info("Calling transactionService.deleteTransaction for id: {}", transactionId);
            transactionService.deleteTransaction(transactionId, currentUser);
            logger.info("Successfully completed transactionService.deleteTransaction for id: {}", transactionId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (EntityNotFoundException | SecurityException e) {
            logger.warn("Failed to delete transaction {}: {}", transactionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Throwable t) {
            logger.error("An unexpected error occurred while deleting transaction {}", transactionId, t);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "An unexpected server error occurred."));
        }
    }

    /**
     * シミュレーションによって変更された取引の日付を一括で更新します。
     * @param dtoList 取引IDと新しい日付のペアのリスト
     * @param authentication 認証情報
     * @return 処理結果
     */
    @PostMapping("/transactions/update-dates")
    @ResponseBody
    public ResponseEntity<?> updateTransactionDates(
            @RequestBody List<TransactionDateUpdateDTO> dtoList,
            Authentication authentication) {

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();
        try {
            transactionService.updateTransactionDates(dtoList, currentUser);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error updating transaction dates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "日付の更新中にエラーが発生しました。"));
        }
    }
}