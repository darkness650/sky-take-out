package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api("套餐接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealDTO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐：{}",id);
        SetmealDTO setmealDTO=setmealService.getById(id);
        log.info("结果{}",setmealDTO);
        return Result.success(setmealDTO);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询{}",setmealPageQueryDTO);
        PageResult setmealList=setmealService.page(setmealPageQueryDTO);
        return Result.success(setmealList);
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
    @DeleteMapping
    @ApiOperation("删除套餐")
    public Result deleteByIds(@RequestParam List<Long> ids) {
        log.info("删除套餐{}",ids);
        setmealService.deleteByIds(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("起售停售")
    public Result startOrStop(@PathVariable Integer status,Long id) {
        log.info("修改状态{}",status);
        setmealService.update(status,id);
        return Result.success();
    }
}
