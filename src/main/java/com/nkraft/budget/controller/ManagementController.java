package com.nkraft.budget.controller;

import com.nkraft.budget.dto.AccountCreateDTO;
import com.nkraft.budget.dto.AccountUpdateDTO;
import com.nkraft.budget.dto.CategoryCreateDTO;
import com.nkraft.budget.dto.CategoryUpdateDTO;
import com.nkraft.budget.service.AccountService;
import com.nkraft.budget.service.CategoryService;
import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.model.LoginUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/budget/management")
@RequiredArgsConstructor
public class ManagementController {

    private final AccountService accountService;
    private final CategoryService categoryService;

    @GetMapping
    public String showManagementPage(Model model, Authentication authentication) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        NkraftUser currentUser = userDetails.getNkraftUser();

        model.addAttribute("accounts", accountService.getAccountsByUserId(currentUser.getId()));
        model.addAttribute("categories", categoryService.getCategoriesByUserId(currentUser.getId()));

        // DTOs for modals
        model.addAttribute("accountCreateDTO", new AccountCreateDTO());
        model.addAttribute("categoryCreateDTO", new CategoryCreateDTO());
        model.addAttribute("accountUpdateDTO", new AccountUpdateDTO());
        model.addAttribute("categoryUpdateDTO", new CategoryUpdateDTO());

        return "budget/management";
    }

    // --- Account Actions ---
    @PostMapping("/accounts/create")
    public String createAccount(@ModelAttribute AccountCreateDTO dto, Authentication authentication, RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        accountService.createAccount(dto, userDetails.getNkraftUser());
        redirectAttributes.addFlashAttribute("message", "口座を登録しました。");
        return "redirect:/budget/management";
    }

    @PostMapping("/accounts/update/{id}")
    public String updateAccount(@PathVariable Long id, @ModelAttribute AccountUpdateDTO dto, Authentication authentication, RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        try {
            accountService.updateAccount(id, dto, userDetails.getNkraftUser());
            redirectAttributes.addFlashAttribute("message", "口座を更新しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新中にエラーが発生しました: " + e.getMessage());
        }
        return "redirect:/budget/management";
    }

    @PostMapping("/accounts/delete/{id}")
    public String deleteAccount(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        try {
            accountService.deleteAccount(id, userDetails.getNkraftUser());
            redirectAttributes.addFlashAttribute("message", "口座を削除しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "削除中にエラーが発生しました: " + e.getMessage());
        }
        return "redirect:/budget/management";
    }

    // --- Category Actions ---
    @PostMapping("/categories/create")
    public String createCategory(@ModelAttribute CategoryCreateDTO dto, Authentication authentication, RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        categoryService.createCategory(dto, userDetails.getNkraftUser());
        redirectAttributes.addFlashAttribute("message", "カテゴリを登録しました。");
        return "redirect:/budget/management";
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute CategoryUpdateDTO dto, Authentication authentication, RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        try {
            categoryService.updateCategory(id, dto, userDetails.getNkraftUser());
            redirectAttributes.addFlashAttribute("message", "カテゴリを更新しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新中にエラーが発生しました: " + e.getMessage());
        }
        return "redirect:/budget/management";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        try {
            categoryService.deleteCategory(id, userDetails.getNkraftUser());
            redirectAttributes.addFlashAttribute("message", "カテゴリを削除しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "削除中にエラーが発生しました: " + e.getMessage());
        }
        return "redirect:/budget/management";
    }
}