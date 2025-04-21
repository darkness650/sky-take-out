package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService {
    void save(DishDTO dish);

    PageResult pageQuery(DishPageQueryDTO queryDTO);

    void deleteBatch(List<Long> ids);

    void update(DishDTO dish);


    DishVO getByIdWithFlavor(Long id);

    void update(Integer status, Long id);

    List<Dish> list(Integer categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
