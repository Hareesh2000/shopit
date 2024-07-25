package com.shopit.project.service;

import com.shopit.project.payload.CategoryDTO;
import com.shopit.project.payload.CategoryResponse;

public interface CategoryService {
    CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO addCategory(CategoryDTO categoryDTO);
    CategoryDTO deleteCategory(Long categoryId);
    CategoryDTO permanentDeleteCategory(Long categoryId);
    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);
    CategoryDTO unDeleteCategory(Long categoryId);
}
