package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        //向菜品表加入1条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        //向口味表中加入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null &&flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(pageQueryDTO);
        long size = page.getTotal();
        List<DishVO> result = page.getResult();
        PageResult pageResult = new PageResult(size,result);

        return pageResult;
    }

    @Override
    @Transactional
    public void dishDelete(List<Long> ids) {

        //是否不是在起售状态
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //是否套餐中没有菜品
        List<Long> setmealIds = setmealDishMapper.getSetMealIdsByDishIds(ids);
        log.info("setmealIds.size():{}",setmealIds.size());
        if(setmealIds != null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }



            //删除菜品中数据
            dishMapper.delete(ids);
            //删除口味中数据
            dishFlavorMapper.delete(ids);


    }

    @Override
    public DishVO getById(Long id) {
        //先查菜品表
        Dish dish = dishMapper.getById(id);
        //根据id后查关联的口味
        List<DishFlavor> flavor = dishFlavorMapper.getFlavorById(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavor);

        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();BeanUtils.copyProperties(dishDTO,dish);

        //修改菜品基本信息
            dishMapper.update(dish);
            //删除原有口味数据
        dishFlavorMapper.delete(Collections.singletonList(dishDTO.getId()));
            //添加相关菜品
        //向口味表中加入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null &&flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void StartOrStop(Long id,Integer status) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }
}
