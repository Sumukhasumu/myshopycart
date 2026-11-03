package com.myshopycart.productservice.service;

import com.myshopycart.productservice.entity.Category;
import com.myshopycart.productservice.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found: " + id));
    }

    public Category updateCategory(String id, Category category) {

        Category existingCategory = getCategoryById(id);

        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setParentCategoryId(
                category.getParentCategoryId()
        );

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(String id) {

        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found: " + id);
        }

        categoryRepository.deleteById(id);
    }
}