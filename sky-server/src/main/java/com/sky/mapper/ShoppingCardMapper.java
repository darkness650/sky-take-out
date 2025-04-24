package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCardMapper {
    void update(ShoppingCart shoppingCart1);

    ShoppingCart queryByDTO(ShoppingCart shoppingCart);

    void insert(ShoppingCart shoppingCart);
    @Select("select * from shopping_cart where user_id=#{userId}")
    List<ShoppingCart> list(Long userId);
    @Delete("delete from shopping_cart where user_id=#{userId}")
    void clean(Long userId);
    @Delete("delete from shopping_cart where id=#{id}")
    void delete(ShoppingCart shoppingCart1);
}
