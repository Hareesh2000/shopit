package com.shopit.project.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.exceptions.ResourceNotFoundException;
import com.shopit.project.model.Category;
import com.shopit.project.payload.CategoryDTO;
import com.shopit.project.payload.CategoryResponse;
import com.shopit.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{

    CategoryRepository categoryRepository;

    ModelMapper modelMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper){
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    public CategoryServiceImpl(){}

    @Override
    public CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Category> categoryPage = categoryRepository.findByDeleteDateIsNull(pageDetails);
        List<Category> categories = categoryPage.getContent();

        if(categories.isEmpty()){
            throw new APIException("No Category has been created till now");
        }
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO addCategory(CategoryDTO categoryDTO) {
        Optional<Category> optionalCategory = categoryRepository.findByCategoryName(categoryDTO.getCategoryName());

        if(optionalCategory.isPresent()){
            if(optionalCategory.get().getDeleteDate() == null)
                throw new APIException("Category with the name " + categoryDTO.getCategoryName() + " already exists");
            else
                throw new APIException("Category with the name " + categoryDTO.getCategoryName() + " has been deleted");
        }

        Category category = modelMapper.map(categoryDTO, Category.class);

        Category savedCategory = categoryRepository.save(category);

        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO permanentDeleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        categoryRepository.delete(category);

        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        if(category.getDeleteDate() != null)
            throw new APIException("Category with the name " + category.getCategoryName() + " has already been deleted");

        category.setDeleteDate(new Date());

        Category deletedCategory = categoryRepository.save(category);

        return modelMapper.map(deletedCategory, CategoryDTO.class);
    }


    @Override
    public CategoryDTO unDeleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        if(category.getDeleteDate() == null)
            throw new APIException("Category with the name " + category.getCategoryName() + " has not been deleted");

        category.setDeleteDate(null);

        categoryRepository.save(category);

        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category ID", categoryId));

        if(category.getDeleteDate() != null)
            throw new APIException("Category with the name " + category.getCategoryName() + " has been deleted");

        category.setCategoryName(categoryDTO.getCategoryName());

        Category savedCategory = categoryRepository.save(category);

        return modelMapper.map(savedCategory, CategoryDTO.class);
    }



}
