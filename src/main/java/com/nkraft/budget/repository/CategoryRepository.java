package com.nkraft.budget.repository;

import com.nkraft.budget.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNkraftUser_IdAndIsDeletedFalse(Long nkraftUserId);
    Optional<Category> findByCategoryNameAndNkraftUser(String categoryName, com.nkraft.user.entity.NkraftUser user);
}