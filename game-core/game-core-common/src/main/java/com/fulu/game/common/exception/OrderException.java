package com.fulu.game.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单异常处理
 */
@Getter
public class OrderException  extends BizException{


    private String orderNo;
    private ExceptionCode exceptionCode;



    @AllArgsConstructor
    @Getter
    public enum ExceptionCode{
        ORDER_NOT_EXIST(7001, "该订单不存在"),
        ORDER_TYPE_MISMATCHING(7002, "该订单类型不匹配"),
        ORDER_ALREADY_RECEIVE(7003, "该订单已经被接单"),
        ORDER_USER_NOT_VERIFIED(7011, "您没通过陪玩师认证"),
        ORDER_USER_NOT_HAS_TECH(7012, "您没有认证该游戏");
        private int code;
        private String msg;
    }



    public OrderException(ExceptionCode exceptionCode,String orderNo) {
        this.exceptionCode = exceptionCode;
        this.orderNo = orderNo;
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMsg();
    }


    public OrderException(String orderNo,String message) {
        String msg = "["+orderNo+"]"+message;
        this.orderNo = orderNo;
        this.message = msg;
    }

}
