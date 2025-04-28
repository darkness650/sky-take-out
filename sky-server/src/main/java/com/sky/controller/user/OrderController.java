package com.sky.controller.user;

import com.github.pagehelper.Page;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userOrderController")
@Slf4j
@RequestMapping("/user/order")
@Tag(name="订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @Operation(tags = "提交订单接口")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO=orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> selectById(@PathVariable Long id) {
        log.info("查询订单：{}",id);
        OrderVO orderVO=orderService.getById(id);
        return Result.success(orderVO);
    }
    @PutMapping("/payment")
    @Operation(description = "订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }
    @GetMapping("/historyOrders")
    @Operation(description = "历史订单查询")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单：{}", ordersPageQueryDTO);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageResult page=orderService.page(ordersPageQueryDTO);
        return Result.success(page);
    }
    @PostMapping("/repetition/{id}")
    @Operation(description = "再来一单")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单：{}",id);
        orderService.repeat(id);
        return Result.success();
    }
    @PutMapping("/cancel/{id}")
    @Operation(description = "取消订单")
    public Result cancel(@PathVariable Long id)
    {
        log.info("取消订单：{}",id);
        orderService.cancel(id);
        return Result.success();
    }
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id) {
        log.info("催单");
        orderService.reminder(id);
        return Result.success();
    }

}
