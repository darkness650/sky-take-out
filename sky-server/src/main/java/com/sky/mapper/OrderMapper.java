package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);


    Page<OrderVO> getByUser(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO getStatistics();
    @Select("select id from orders where user_id=#{userId} and number=#{orderNumber}")
    int getId(Long userId, String orderNumber);
    @Select("select id from orders where status=1 and order_time<#{now}")
    List<Long> findOutOfTime(Date now);
    @Select("select id from orders where status=#{status}")
    List<Long> findStatus(Integer status);

    Integer countByMap(Map map);

    Double sumByMap(Map map);
}
