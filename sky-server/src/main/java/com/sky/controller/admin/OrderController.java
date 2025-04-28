package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/order")
@Tag(name="管理端订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @Operation(description = "查询订单")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询订单{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.page(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/statistics")
    @Operation(description = "各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO orderStatisticsVO=orderService.getStatistics();
        return Result.success(orderStatisticsVO);
    }
    @GetMapping("/details/{id}")
    @Operation(description = "查询订单详情")
    public Result<OrderVO> getById(@PathVariable Long id) {
        log.info("查询订单详情：{}",id);
        OrderVO orderVO=orderService.getById(id);
        return Result.success(orderVO);
    }
    @PutMapping("/confirm")
    @Operation(description = "接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单：{}",ordersConfirmDTO.getId());
        orderService.changeStatus(ordersConfirmDTO.getId(), Orders.CONFIRMED);
        return Result.success();

    }
    @PutMapping("/rejection")
    @Operation(description = "拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单：{}",ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("/cancel")
    @Operation(description = "取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单：{}",ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }
    @PutMapping("/delivery/{id}")
    @Operation(description = "配送订单")
    public Result deliver(@PathVariable Long id)
    {
        log.info("配送订单：{}",id);
        orderService.changeStatus(id, Orders.DELIVERY_IN_PROGRESS);
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @Operation(description = "完成订单")
    public Result complete(@PathVariable Long id)
    {
        log.info("配送订单：{}",id);
        orderService.changeStatus(id, Orders.COMPLETED);
        return Result.success();
    }
}
