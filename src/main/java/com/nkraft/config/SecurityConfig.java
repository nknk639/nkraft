package com.nkraft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        // CSSやJSなどの静的リソースへのアクセスを許可
                        .requestMatchers("/css/**", "/js/**", "/webjars/**").permitAll()
                        // ログインページへのアクセスを許可
                        .requestMatchers("/login").permitAll()
                        // その他のリクエストはすべて認証が必要
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        // ログインページのURLを指定
                        .loginPage("/login")
                        // ログイン処理を行うURL (Spring Securityが自動で処理)
                        .loginProcessingUrl("/login")
                        // ログイン成功時のリダイレクト先
                        .defaultSuccessUrl("/budget/", true) // コントローラーのマッピングに合わせて修正
                        // ログインページは全員にアクセスを許可
                        .permitAll())
                .logout(logout -> logout
                        // ログアウト成功時のリダイレクト先
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}