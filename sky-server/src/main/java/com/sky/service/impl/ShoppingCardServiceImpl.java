package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCardMapper;
import com.sky.service.ShoppingCardService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCardServiceImpl implements ShoppingCardService {
    @Autowired
    private ShoppingCardMapper shoppingCardMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId= BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        ShoppingCart shoppingCart1=shoppingCardMapper.queryByDTO(shoppingCart);
        if(shoppingCart1!=null)
        {
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCardMapper.update(shoppingCart1);
            return;
        }
        else
        {
            if(shoppingCartDTO.getSetmealId()!=null)
            {
                SetmealDTO setmealDTO=setmealMapper.getById(shoppingCartDTO.getSetmealId());
                BeanUtils.copyProperties(setmealDTO, shoppingCart);
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCart.setAmount(setmealDTO.getPrice());
            }
            else
            {
                Dish dishDTO=dishMapper.getById(shoppingCartDTO.getDishId());
                BeanUtils.copyProperties(dishDTO, shoppingCart);
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCart.setAmount(dishDTO.getPrice());
            }
            shoppingCardMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        Long userId=BaseContext.getCurrentId();
        return shoppingCardMapper.list(userId);
    }

    @Override
    public void clean() {
        Long userId=BaseContext.getCurrentId();
        shoppingCardMapper.clean(userId);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId= BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        ShoppingCart shoppingCart1=shoppingCardMapper.queryByDTO(shoppingCart);
        if(shoppingCart1.getNumber()!=1)
        {
            shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
            shoppingCardMapper.update(shoppingCart1);
            return;
        }
        else
        {
            shoppingCardMapper.delete(shoppingCart1);
        }
    }
}
