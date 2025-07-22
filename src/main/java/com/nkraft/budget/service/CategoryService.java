package com.nkraft.budget.service;

import com.nkraft.budget.dto.CategoryCreateDTO;
import com.nkraft.budget.dto.CategoryUpdateDTO;
import com.nkraft.budget.dto.CategoryDTO;
import com.nkraft.budget.entity.Category;
import com.nkraft.budget.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByNkraftUser_IdAndIsDeletedFalse(userId);
    }

    /**
     * ユーザーのカテゴリリストをDTOとして取得します。
     * @param userId ユーザーID
     * @return カテゴリDTOのリスト
     */
    public List<CategoryDTO> getCategoriesByUserIdAsDTO(Long userId) {
        return getCategoriesByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO mapToDTO(Category entity) {
        return new CategoryDTO(entity.getId(), entity.getCategoryName());
    }
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public void createCategory(CategoryCreateDTO dto, com.nkraft.user.entity.NkraftUser user) {
        Category category = new Category();
        category.setNkraftUser(user);
        category.setCategoryName(dto.getCategoryName());
        category.setIsDeleted(false);
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Long categoryId, CategoryUpdateDTO dto, com.nkraft.user.entity.NkraftUser user) {
        Category category = getCategoryByIdAndUser(categoryId, user);
        category.setCategoryName(dto.getCategoryName());
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId, com.nkraft.user.entity.NkraftUser user) {
        Category category = getCategoryByIdAndUser(categoryId, user);
        if ("借入返済".equals(category.getCategoryName()) || "目標貯金".equals(category.getCategoryName())) {
            throw new IllegalStateException("システムカテゴリは削除できません。");
        }
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

    private Category getCategoryByIdAndUser(Long categoryId, com.nkraft.user.entity.NkraftUser user) {
        Category category = getCategoryById(categoryId);
        if (!category.getNkraftUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not have permission to access this category.");
        }
        return category;
    }

    public Optional<Category> findByNameAndUser(String name, com.nkraft.user.entity.NkraftUser user) {
        return categoryRepository.findByCategoryNameAndNkraftUser(name, user);
    }
}