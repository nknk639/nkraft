package com.nkraft.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nkraft.user.entity.NkraftUser;
import com.nkraft.user.repository.NkraftUserRepository;

import java.util.List;

@Configuration
public class DataInitializer {

    /**
     * アプリケーション起動時にDBの初期パスワードをハッシュ化します。
     */
    @Bean
    CommandLineRunner initDatabase(NkraftUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            List<NkraftUser> users = userRepository.findAll();
            for (NkraftUser user : users) {
                // パスワードがBCrypt形式でない場合（＝平文の場合）のみハッシュ化して保存
                if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(user);
                }
            }
        };
    }
}