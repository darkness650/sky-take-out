package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
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
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishService dishService;

    @Override
    public void save(DishDTO dish) {
        log.info("新增菜品：{}",dish);
        Dish dish1=new Dish();
        BeanUtils.copyProperties(dish, dish1);
        dishMapper.insert(dish1);
        Long id=dish1.getId();
        List<DishFlavor> dishFlavors=dish.getFlavors();
        if(dishFlavors!=null && dishFlavors.size()>0){
            dishFlavors.forEach(dishFlavor->{dishFlavor.setDishId(id);});
            dishFlavorMapper.insertBatch(dishFlavors);
        }

    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(),queryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(queryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        List<Long> setmealIds=setmealDishMapper.getSetmealDishIdsBySetmealId(ids);
        if(setmealIds!=null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        for (Long id : ids) {
            dishMapper.deleteById(id);

            dishFlavorMapper.deleteByDishId(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DishDTO dish) {
        log.info("更新菜品：{}",dish);
        Dish dish1=new Dish();
        BeanUtils.copyProperties(dish, dish1);
        dishMapper.update(dish1);
        List<DishFlavor> dishFlavors=dish.getFlavors();
        dishFlavors.forEach(dishFlavor->{dishFlavor.setDishId(dish1.getId());});
        dishFlavorMapper.deleteByDishId(dish1.getId());
        if(dishFlavors!=null && dishFlavors.size()>0){
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish=dishMapper.getById(id);
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    public void update(Integer status, Long id) {
        Dish dish=dishMapper.getById(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> list(Integer categoryId) {
        return dishMapper.listByCategoryId(categoryId);
    }
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

}
