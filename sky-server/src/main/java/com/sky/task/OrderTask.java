package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderService orderService;
    @Scheduled(cron="0 * * * * *")
    public void clearOutOfTime()
    {
        Date now = new Date();
        now.setTime(now.getTime()-15*60*1000);
        List<Long> idList=orderMapper.findOutOfTime(now);
        for (Long i : idList) {
            orderService.changeStatus(i, Orders.CANCELLED);
        }
    }
    @Scheduled(cron="0 0 0 * * *")
    public void finishOrder()
    {
        List<Long> finsh=orderMapper.findStatus(Orders.DELIVERY_IN_PROGRESS);
        for (Long i : finsh) {
            orderService.changeStatus(i,Orders.COMPLETED);
        }
    }
}
