package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;

public interface CategoryService {
public void save(CategoryDTO categoryDTO);
public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
        void startOrStop(Integer status,long id);
        void updateCategory(CategoryDTO categoryDTO);
        void delete(long id);
}
