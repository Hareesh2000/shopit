package com.shopit.project.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.model.Category;
import com.shopit.project.payload.CategoryDTO;
import com.shopit.project.payload.CategoryResponse;
import com.shopit.project.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryName("Electronics");
    }

    @Test
    void getCategories_shouldReturnCategoryResponse() {
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        List<Category> categories = new ArrayList<>();
        categories.add(category);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("categoryName").ascending());
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findByDeleteDateIsNull(any(Pageable.class))).thenReturn(categoryPage);

        CategoryResponse response = categoryService.getCategories(0, 10, "categoryName", "asc");

        verify(categoryRepository, times(1)).findByDeleteDateIsNull(any(Pageable.class));
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void addCategory_shouldReturnSavedCategoryDTO() {
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        when(modelMapper.map(any(CategoryDTO.class), eq(Category.class))).thenReturn(category);
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = categoryService.addCategory(categoryDTO);

        verify(categoryRepository, times(1)).findByCategoryName(anyString());
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertNotNull(result);
        assertEquals(categoryDTO.getCategoryName(), result.getCategoryName());
    }

    @Test
    void addCategory_shouldThrowException_whenCategoryAlreadyExists() {
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.of(category));

        assertThrows(APIException.class, () -> categoryService.addCategory(categoryDTO));

        verify(categoryRepository, times(1)).findByCategoryName(anyString());
    }

    @Test
    void permanentDeleteCategory_shouldReturnDeletedCategoryDTO() {
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        CategoryDTO result = categoryService.permanentDeleteCategory(1L);

        verify(categoryRepository, times(1)).findById(anyLong());
        verify(categoryRepository, times(1)).delete(any(Category.class));
        assertNotNull(result);
        assertEquals(categoryDTO.getCategoryName(), result.getCategoryName());
    }

    @Test
    void deleteCategory_shouldReturnDeletedCategoryDTO() {
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).findById(anyLong());
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertNotNull(result);
    }

    @Test
    void unDeleteCategory_shouldReturnRestoredCategoryDTO() {
        category.setDeleteDate(new Date());

        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = categoryService.unDeleteCategory(1L);

        verify(categoryRepository, times(1)).findById(anyLong());
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertNotNull(result);
    }

    @Test
    void updateCategory_shouldReturnUpdatedCategoryDTO() {
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = categoryService.updateCategory(1L, categoryDTO);

        verify(categoryRepository, times(1)).findById(anyLong());
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertNotNull(result);
        assertEquals(categoryDTO.getCategoryName(), result.getCategoryName());
    }

    @Test
    void updateCategory_shouldThrowException_whenCategoryIsDeleted() {
        category.setDeleteDate(new Date());

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        assertThrows(APIException.class, () -> categoryService.updateCategory(1L, categoryDTO));

        verify(categoryRepository, times(1)).findById(anyLong());
    }
}