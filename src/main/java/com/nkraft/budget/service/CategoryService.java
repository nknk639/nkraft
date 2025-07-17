package com.nkraft.budget.service;

import com.nkraft.budget.entity.Category;
import com.nkraft.budget.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByNkraftUser_Id(userId);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }
}