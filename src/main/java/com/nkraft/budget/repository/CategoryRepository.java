package com.nkraft.budget.repository;

import com.nkraft.budget.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNkraftUser_Id(Long nkraftUserId);
}