package com.nkraft.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 認証関連の画面遷移を制御するコントローラー
 */
@Controller
public class LoginController {

    /**
     * ログイン画面を表示します。
     * @return ログイン画面のテンプレート名
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}