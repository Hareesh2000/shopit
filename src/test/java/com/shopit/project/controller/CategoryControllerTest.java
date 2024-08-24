package com.shopit.project.controller;

import com.shopit.project.payload.CategoryDTO;
import com.shopit.project.payload.CategoryResponse;
import com.shopit.project.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    void testGetCategories() throws Exception {
        CategoryResponse mockResponse = new CategoryResponse();
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(1L);
        categoryDTO.setCategoryName("test");

        mockResponse.setContent(new ArrayList<>(List.of(categoryDTO)));
        when(categoryService.getCategories(anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/public/categories")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].categoryId")
                                .value(categoryDTO.getCategoryId()))
                        .andExpect(jsonPath("$.content[0].categoryName")
                                .value(categoryDTO.getCategoryName()));

        verify(categoryService, times(1)).getCategories(0, 10, "name", "asc");
    }

    @Test
    void testAddCategory() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO();
        when(categoryService.addCategory(any(CategoryDTO.class))).thenReturn(categoryDTO);

        mockMvc.perform(post("/api/admin/category")
                        .contentType("application/json")
                        .content("{\"categoryName\":\"Electronics\"}"))
                .andExpect(status().isCreated());

        verify(categoryService, times(1)).addCategory(any(CategoryDTO.class));
    }

    @Test
    void testDeleteCategory() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO();
        when(categoryService.deleteCategory(anyLong())).thenReturn(categoryDTO);

        mockMvc.perform(delete("/api/admin/category/1"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    void testPermanentDeleteCategory() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO();
        when(categoryService.permanentDeleteCategory(anyLong())).thenReturn(categoryDTO);

        mockMvc.perform(delete("/api/admin/category/1/permanent"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).permanentDeleteCategory(1L);
    }

    @Test
    void testUpdateCategory() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO();
        when(categoryService.updateCategory(anyLong(), any(CategoryDTO.class))).thenReturn(categoryDTO);

        mockMvc.perform(put("/api/admin/category/1")
                        .contentType("application/json")
                        .content("{\"name\":\"Electronics Updated\"}"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).updateCategory(anyLong(), any(CategoryDTO.class));
    }

    @Test
    void testUnDeleteCategory() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO();
        when(categoryService.unDeleteCategory(anyLong())).thenReturn(categoryDTO);

        mockMvc.perform(put("/api/admin/categories/1/undelete"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).unDeleteCategory(1L);
    }
}