package com.fulu.game.core.service.impl;


import com.fulu.game.core.dao.ICommonDao;
import com.fulu.game.core.dao.OrderStatusDetailsDao;
import com.fulu.game.core.entity.OrderStatusDetails;
import com.fulu.game.core.entity.vo.OrderStatusDetailsVO;
import com.fulu.game.core.service.OrderStatusDetailsService;
import com.xiaoleilu.hutool.date.DateUnit;
import com.xiaoleilu.hutool.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class OrderStatusDetailsServiceImpl extends AbsCommonService<OrderStatusDetails,Integer> implements OrderStatusDetailsService {

    @Autowired
	private OrderStatusDetailsDao orderStatusDetailsDao;

    @Override
    public ICommonDao<OrderStatusDetails, Integer> getDao() {
        return orderStatusDetailsDao;
    }


    @Override
    public void create(String orderNo,Integer orderStatus, int minute) {
        OrderStatusDetails orderStatusDetails = new OrderStatusDetails();
        orderStatusDetails.setOrderStatus(orderStatus);
        orderStatusDetails.setCountDownMinute(minute);
        orderStatusDetails.setOrderNo(orderNo);
        orderStatusDetails.setTriggerTime(new Date());
        orderStatusDetails.setIsValid(true);
        orderStatusDetails.setCreateTime(new Date());
        orderStatusDetails.setUpdateTime(new Date());
        create(orderStatusDetails);
    }

    /**
     * 重置订单状态
     * @param orderNo
     * @param orderStatus
     * @param minute
     */
    @Override
    public void resetOrderStatus(String orderNo, Integer orderStatus,int minute) {
        OrderStatusDetailsVO param = new OrderStatusDetailsVO();
        param.setOrderNo(orderNo);
        param.setOrderStatus(orderStatus);
        List<OrderStatusDetails> list = orderStatusDetailsDao.findByParameter(param);
        for(OrderStatusDetails orderStatusDetails : list){
            orderStatusDetails.setIsValid(false);
            orderStatusDetails.setUpdateTime(new Date());
            update(orderStatusDetails);
        }
        OrderStatusDetails orderStatusDetails = new OrderStatusDetails();
        orderStatusDetails.setOrderStatus(orderStatus);
        orderStatusDetails.setCountDownMinute(minute);
        orderStatusDetails.setOrderNo(orderNo);
        orderStatusDetails.setTriggerTime(new Date());
        orderStatusDetails.setIsValid(true);
        orderStatusDetails.setCreateTime(new Date());
        orderStatusDetails.setUpdateTime(new Date());
        create(orderStatusDetails);
    }


    @Override
    public OrderStatusDetails findByOrderAndStatus(String orderNo, Integer orderStatus) {
        OrderStatusDetailsVO param = new OrderStatusDetailsVO();
        param.setOrderNo(orderNo);
        param.setOrderStatus(orderStatus);
        param.setIsValid(true);
        List<OrderStatusDetails> list = orderStatusDetailsDao.findByParameter(param);
        if(!list.isEmpty()){
            return list.get(0);
        }else{
            OrderStatusDetails orderStatusDetails = new OrderStatusDetails();
            orderStatusDetails.setOrderNo(orderNo);
            orderStatusDetails.setOrderStatus(orderStatus);
            orderStatusDetails.setTriggerTime(new Date());
            orderStatusDetails.setCountDownMinute(0);
            return orderStatusDetails;
        }
    }



    public long getCountDown(String orderNo,Integer orderStatus){
        OrderStatusDetails orderStatusDetails = findByOrderAndStatus(orderNo,orderStatus);
        if(orderStatusDetails.getCountDownMinute().equals(0)){
            return  0L;
        }else{
            long differSecond = DateUtil.between(orderStatusDetails.getTriggerTime(),new Date(), DateUnit.SECOND);
            long storeCountDown =  orderStatusDetails.getCountDownMinute()*60;
            long countDown = storeCountDown-differSecond<0?0:storeCountDown-differSecond;
            return countDown;
        }
    }

    @Override
    public List<OrderStatusDetails> findOrderProcess(String orderNo) {
        return orderStatusDetailsDao.findOrderProcessByOrderNo(orderNo);
    }
}
