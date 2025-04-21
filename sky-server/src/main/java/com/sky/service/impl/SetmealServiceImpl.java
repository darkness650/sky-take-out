package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        List<SetmealDish> setmealDishList=setmealDTO.getSetmealDishes();
        for(SetmealDish setmealDish:setmealDishList){
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealDishMapper.save(setmealDishList);
        setmealMapper.save(setmeal);
    }
    @Transactional
    @Override
    public SetmealDTO getById(Long id) {
        SetmealDTO setmealDTO=setmealMapper.getById(id);
        List<SetmealDish> list=setmealDishMapper.getSetmealDishsBySetmealId(id);
        setmealDTO.setSetmealDishes(list);
        return setmealDTO;
    }

    @Override
    @Transactional
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> list=setmealMapper.page(setmealPageQueryDTO);
        Long total=list.getTotal();
        List<Setmeal> setmealList=list.getResult();
        return new PageResult(total,setmealList);
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        List<SetmealDish> setmealDishList=setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishList) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        setmealDishMapper.save(setmealDishList);
        setmealMapper.update(setmeal);
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    @Override
    public void update(Integer status, Long id) {
        Setmeal setmeal=new Setmeal();
        SetmealDTO setmealDTO= setmealMapper.getById(id);
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
