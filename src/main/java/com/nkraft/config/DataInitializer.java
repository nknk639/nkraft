package com.nkraft.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nkraft.user.entity.User;
import com.nkraft.user.repository.UserRepository;

import java.util.List;

@Configuration
public class DataInitializer {

    /**
     * アプリケーション起動時にDBの初期パスワードをハッシュ化します。
     */
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            List<User> users = userRepository.findAll();
            for (User user : users) {
                // パスワードがBCrypt形式でない場合（＝平文の場合）のみハッシュ化して保存
                if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(user);
                }
            }
        };
    }
}