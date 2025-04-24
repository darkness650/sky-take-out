package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Tag(name= "菜品管理")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @Operation(description="保存菜品")
    @CacheEvict(cacheNames = "dish",key="#dish.categoryId")
    public Result sava(@RequestBody DishDTO dish)
    {
        dishService.save(dish);
        return Result.success();
    }
    @Operation(description="菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO queryDTO)
    {
        log.info("菜品分页查询");
        PageResult pageResult=dishService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @Operation(description="批量删除")
    @CacheEvict(cacheNames = "dish",allEntries = true)
    public Result delete(@RequestParam List<Long> ids)
    {
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @PutMapping
    @Operation(description = "修改菜品")
    @CacheEvict(cacheNames = "dish",allEntries = true)
    public Result update(@RequestBody DishDTO dish)
    {
        dishService.update(dish);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(description = "根据id查询菜品")

    public Result<DishVO> getById(@PathVariable Long id)
    {
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "dish",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id)
    {
        dishService.update(status,id);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<List<Dish>> list(Integer categoryId){
        log.info("查询{}", categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

}
