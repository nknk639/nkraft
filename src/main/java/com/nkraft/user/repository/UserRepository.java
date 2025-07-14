package com.nkraft.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nkraft.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * ユーザー名でユーザーを検索します。
     * @param username ユーザー名
     * @return ユーザーエンティティ（Optional）
     */
    Optional<User> findByUsername(String username);
}