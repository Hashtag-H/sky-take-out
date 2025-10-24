package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import io.swagger.models.auth.In;

import java.util.List;

public interface CategoryService {
public void save(CategoryDTO categoryDTO);
public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
        void startOrStop(Integer status,long id);
        void updateCategory(CategoryDTO categoryDTO);
        void delete(long id);
        List<Category> list(Integer status);
}
