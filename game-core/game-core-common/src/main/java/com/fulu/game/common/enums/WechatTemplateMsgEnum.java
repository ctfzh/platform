package com.fulu.game.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WechatTemplateMsgEnum {

    TECH_AUTH_AUDIT_ING(PagePathEnum.SERVICE_USER_VERIFY.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "您的好友{}成功帮您认证,还剩{}位好友认证即可成功通过审核."), //好友认证
    TECH_AUTH_AUDIT_SUCCESS(PagePathEnum.ORDER_RECEIVING_SETTING.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "恭喜您,成功通过审核,快快前往接单啦。"),
    TECH_AUTH_AUDIT_FAIL(PagePathEnum.SERVICE_USER_VERIFY.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "审核未通过:{},请前往重新提交审核."),

    GRANT_COUPON(PagePathEnum.COUPON_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "恭喜你,获得一张{}元优惠券"),

    USER_AUTH_INFO_PASS(PagePathEnum.SERVICE_USER_VERIFY.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "您提交申请的陪玩师个人资料已经审核通过，快去玩耍接单吧!"),
    USER_AUTH_INFO_REJECT(PagePathEnum.SERVICE_USER_VERIFY.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "您提交的陪玩师个人资料申请被驳回，理由:{}"),


    ORDER_TOUSER_REJECT_RECEIVE(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "【订单状态：{}】" +
                                                                                                            "备注：陪玩师太忙了，为了不耽误您的游戏体验，拒绝了订单，望老板大人理解。"),//拒绝接单

    ORDER_TOUSER_AFFIRM_RECEIVE(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "【订单状态：{}】" +
                                                                                                            "备注：陪玩师已经接单啦，一起谈谈什么时候开始呢？超过24小时未开始，订单将自动取消。"),//已经接单

    ORDER_TOUSER_START_SERVICE(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "【订单状态：{}】" +
                                                                                                            "备注：陪玩师开始陪玩服务啦，快快上号开始吧。"), //开始服务

    ORDER_TOUSER_CHECK(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "【订单状态：{}】" +
                                                                                                   "备注：陪玩师向发起了完成服务申请，快去确认吧。"), //提交验收

    ORDER_TOUSER_APPEAL(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "【订单状态：{}】" +
                                                                                                   "备注：对于您的协商，陪玩师申请客服进行仲裁，快去提交凭证吧（2小时内提交）。"), //仲裁

    ORDER_TOUSER_CONSULT_REJECT(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "【订单状态：{}】" +
                                                                                                    "备注：对于您的协商，陪玩师进行了拒绝，快去处理吧，超过2小时未处理，将默认取消协商。"), //拒绝协商

    ORDER_TOUSER_CONSULT_AGREE(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "【订单状态：{}】" +
                                                                                                    "备注：对于您的协商，陪玩师进行了同意，金额将退回到您的微信账号。"), //同意协商


    ORDER_TOSERVICE_PAY(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "有老板向您下单啦，快去查看吧，超过24小时未接单，订单将自动取消。"), //下单
    ORDER_TOSERVICE_REMIND_RECEIVE(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "老板提醒您接单，快去处理吧，超过24小时未接单，订单将自动取消。"), //提醒接单
    ORDER_TOSERVICE_REMIND_START_SERVICE(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "老板提醒您接单，快去处理吧，超过24小时未接单，订单将自动取消。"), //提醒开始
    ORDER_TOSERVICE_AFFIRM_SERVER(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "老板已确认服务完成，收益到账了哦。"), //确认服务完成
    ORDER_TOSERVICE_ORDER_CANCEL(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "很遗憾，老板取消了订单，要再接再厉哦。"), //确认服务完成
    ORDER_TOSERVICE_CONSULT(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "老板发起协商，申请退款，快去处理吧，超过24小时未处理，将默认同意协商。"), //确认服务完成
    ORDER_TOSERVICE_APPEAL(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "老板对您的回馈不满意，申请了客服仲裁，快去提交凭证吧（2小时内提交）。"), //确认服务完成
    ORDER_TOSERVICE_CONSULT_CANCEL(PagePathEnum.ORDER_LIST_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "老板取消了协商，服务照常进行哦。"), //确认服务完成


    ORDER_USER_PAY(PagePathEnum.SERVICE_USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "{}向您下单{},快快去赚钱"), //用户付款
    ORDER_SERVER_USER_CHECK(PagePathEnum.USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "您的{}订单已完成,请前往验收"), //打手验收订单
    ORDER_SERVER_REMIND_RECEIVE_ORDER(PagePathEnum.USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "您的{}订单已完成,请前往验收"), //todo 提醒接单
    ORDER_SERVER_REMIND_START_ORDER(PagePathEnum.USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "您的{}订单已完成,请前往验收"), //todo 提醒开始
    ORDER_USER_APPEAL(PagePathEnum.USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "您的订单已成功申诉,客服正在处理,请耐心等待"),//用户申诉订单
    ORDER_SERVER_USER_APPEAL(PagePathEnum.SERVICE_USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "您的订单已被申诉,请尽快联系客服处理"),//用户申诉订单
    ORDER_USER_APPEAL_REFUND(PagePathEnum.USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "申诉成功,您支付金额将原路返回"),
    ORDER_SERVER_USER_APPEAL_REFUND(PagePathEnum.SERVICE_USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "申诉成功,您将无法获得该订单受益,还请继续努力"),
    ORDER_USER_APPEAL_COMPLETE(PagePathEnum.USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "申诉失败,您支付的金额将支付给陪玩师"),
    ORDER_SERVER_USER_APPEAL_COMPLETE(PagePathEnum.SERVICE_USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "申诉失败,订单金额将稍后到账"),
    ORDER_SYSTEM_USER_APPEAL_COMPLETE(PagePathEnum.USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "该订单已协商处理完成,协商结果请咨询客服"),
    ORDER_SYSTEM_SERVER_APPEAL_COMPLETE(PagePathEnum.SERVICE_USER_ORDER_LIST.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "该订单已协商处理完成,协商结果请咨询客服"),

    IM_MSG_PUSH(PagePathEnum.IM_MSG.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "{}:{}"),

    MARKET_ORDER_PUSH(PagePathEnum.MARKET_ORDER_PAGE.getPagePath(), WechatTemplateEnum.LEAVE_MSG, "有新的{}订单，快来看看吧");

    //跳转页面
    private String page;
    //模板代码
    private WechatTemplateEnum templateId;

    //内容
    private String content;


}
