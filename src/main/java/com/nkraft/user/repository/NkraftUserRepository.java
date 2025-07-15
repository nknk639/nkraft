package com.nkraft.user.repository;

import com.nkraft.user.entity.NkraftUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NkraftUserRepository extends JpaRepository<NkraftUser, Long> {
    Optional<NkraftUser> findByUsername(String username);
}