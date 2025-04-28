package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.socketserver.WebSocketServer;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCardMapper shoppingCardMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId= BaseContext.getCurrentId();
        Orders orders = Orders.builder()
                .orderTime(LocalDateTime.now())
                .payMethod(1)
                .payStatus(0)
                .userId(userId)
                .build();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        BeanUtils.copyProperties(addressBook, orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        orderMapper.insert(orders);
        List<ShoppingCart> list=shoppingCardMapper.list(userId);
        if(list==null || list.size()==0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insert(orderDetailList);
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount()).build();


        return orderSubmitVO;
    }
    @Transactional
    @Override
    public OrderVO getById(Long id) {
        Orders order=orderMapper.getById(id);
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
/*        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }
*/
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrderPaidStatus = Orders.PAID;//支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        LocalDateTime check_out_time = LocalDateTime.now();//更新支付时间
        int id=orderMapper.getId(userId,ordersPaymentDTO.getOrderNumber());
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, (long) id);
        Map map=new HashMap();
        map.put("type",1);
        map.put("orderId",id);
        map.put("content","订单号："+ordersPaymentDTO.getOrderNumber());
        String json= JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);


    }

    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //Long userId = BaseContext.getCurrentId();
        //ordersPageQueryDTO.setUserId(userId);
        Page<OrderVO> page=orderMapper.getByUser(ordersPageQueryDTO);
        if(page==null || page.size()==0) {
            return new PageResult(0,page);
        }
        List<OrderVO> ordersDTOList=page.getResult();
        for (OrderVO ordersDTO : ordersDTOList) {
            List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(ordersDTO.getId());
            ordersDTO.setOrderDetailList(orderDetailList);
        }
        Long total=page.getTotal();
        return new PageResult(total,ordersDTOList);
    }

    @Override
    public void repeat(Long id) {
        Orders byId = orderMapper.getById(id);
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(id);
        byId.setId(null);
        byId.setStatus(Orders.UN_PAID);
        byId.setPayStatus(Orders.UN_PAID);
        byId.setCheckoutTime(LocalDateTime.now());
        byId.setNumber(String.valueOf(System.currentTimeMillis()));
        byId.setOrderTime(LocalDateTime.now());
        byId.setCancelReason(null);
        byId.setRejectionReason(null);
        orderMapper.insert(byId);
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(byId.getId());
        }
        orderDetailMapper.insert(orderDetailList);
    }

    @Override
    public void cancel(Long id) {
        Orders byId = orderMapper.getById(id);
        byId.setStatus(Orders.CANCELLED);
        byId.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(byId);
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO=orderMapper.getStatistics();
        return orderStatisticsVO;
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        Orders order=orderMapper.getById(id);
        order.setCheckoutTime(LocalDateTime.now());
        order.setStatus(status);
        orderMapper.update(order);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order=orderMapper.getById(ordersRejectionDTO.getId());
        order.setCheckoutTime(LocalDateTime.now());
        order.setStatus(Orders.CANCELLED);
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders order=orderMapper.getById(ordersCancelDTO.getId());
        order.setStatus(Orders.CANCELLED);
        order.setCheckoutTime(LocalDateTime.now());
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void reminder(Long id) {
        Orders byId = orderMapper.getById(id);
        Map map=new HashMap();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号："+byId.getNumber());
        String json= JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

}
