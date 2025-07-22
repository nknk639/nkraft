package com.nkraft.budget.service;

import com.nkraft.budget.dto.CategoryDTO;
import com.nkraft.budget.entity.Category;
import com.nkraft.budget.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByNkraftUser_Id(userId);
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

    public Optional<Category> findByNameAndUser(String name, com.nkraft.user.entity.NkraftUser user) {
        return categoryRepository.findByCategoryNameAndNkraftUser(name, user);
    }
}