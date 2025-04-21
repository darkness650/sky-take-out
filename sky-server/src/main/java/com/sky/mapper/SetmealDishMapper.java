package com.sky.mapper;



import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {


    List<Long> getSetmealDishIdsBySetmealId(List<Long> setmealId);
    void save(List<SetmealDish> setmealDishList);
    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> getSetmealDishsBySetmealId(Long id);
    @Delete("delete from setmeal_dish where setmeal_id=#{id}")
    void deleteBySetmealId(Long id);


    void deleteBySetmealIds(List<Long> ids);
}
