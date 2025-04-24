package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Slf4j
@RequestMapping("/user/shop")
@Tag(name = "店铺相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    public static final String KEY="SHOP_STATUS";
    @GetMapping("/status")
    @Operation(description="获得营业状态")
    public Result<Integer> getStatus(){
        Integer status=(Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取到的营业状态为：{}",status==1?"营业中":"打烊中");
        return Result.success(status);
    }
}
