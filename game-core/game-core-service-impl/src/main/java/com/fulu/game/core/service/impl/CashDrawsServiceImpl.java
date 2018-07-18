package com.fulu.game.core.service.impl;

import com.fulu.game.common.Constant;
import com.fulu.game.common.enums.CashProcessStatusEnum;
import com.fulu.game.common.enums.MoneyOperateTypeEnum;
import com.fulu.game.common.exception.CashException;
import com.fulu.game.common.exception.UserException;
import com.fulu.game.core.dao.CashDrawsDao;
import com.fulu.game.core.dao.ICommonDao;
import com.fulu.game.core.dao.MoneyDetailsDao;
import com.fulu.game.core.entity.Admin;
import com.fulu.game.core.entity.CashDraws;
import com.fulu.game.core.entity.MoneyDetails;
import com.fulu.game.core.entity.User;
import com.fulu.game.core.entity.vo.CashDrawsVO;
import com.fulu.game.core.entity.vo.MoneyDetailsVO;
import com.fulu.game.core.service.AdminService;
import com.fulu.game.core.service.CashDrawsService;
import com.fulu.game.core.service.MoneyDetailsService;
import com.fulu.game.core.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.date.Week;
import com.xiaoleilu.hutool.util.BeanUtil;
import com.xiaoleilu.hutool.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service("/cashDrawsService")
@Slf4j
public class CashDrawsServiceImpl extends AbsCommonService<CashDraws, Integer> implements CashDrawsService {

    @Autowired
    private CashDrawsDao cashDrawsDao;
    @Autowired
    private UserService userService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private MoneyDetailsService mdService;
    @Autowired
    private MoneyDetailsDao moneyDetailsDao;

    @Override
    public ICommonDao<CashDraws, Integer> getDao() {
        return cashDrawsDao;
    }

    /**
     * 1 判断提现金额参数是否合法
     * 2 是否超过账户余额
     * 3 生成t_cash_draws记录
     * 4 修改提现后余额，生成t_money_details记录
     * 5 修改t_user记录
     *
     * @param cashDrawsVO
     * @return
     */
    @Override
    public CashDrawsVO save(CashDrawsVO cashDrawsVO) {
        log.info("====调用提现申请接口====");
        User user = userService.getCurrentUser();
        if (null == user) {
            log.error("提款申请异常，当前操作用户不存在");
            throw new UserException(UserException.ExceptionCode.USER_NOT_EXIST_EXCEPTION);
        }
        BigDecimal money = cashDrawsVO.getMoney();
        if (money.compareTo(BigDecimal.ZERO) == -1) {
            log.error("提款申请异常，提款金额小于0，用户id:{}", user.getId());
            throw new CashException(CashException.ExceptionCode.CASH_NEGATIVE_EXCEPTION);
        }
        user = userService.findById(user.getId());
        BigDecimal balance = user.getBalance();
        if (money.compareTo(balance) == 1) {
            log.error("提款申请异常，金额超出账户余额，用户id:{}", user.getId());
            throw new CashException(CashException.ExceptionCode.CASH_EXCEED_EXCEPTION);
        }
        log.info("====提现用户id:{},提现金额:{},账户余额:{}", user.getId(), money, balance);
        //提现后的余额
        BigDecimal newBalance = balance.subtract(money);
        CashDraws cashDraws = new CashDraws();
        BeanUtil.copyProperties(cashDrawsVO, cashDraws);
        cashDraws.setUserId(user.getId());
        cashDraws.setNickname(user.getNickname());
        cashDraws.setMobile(user.getMobile());
        cashDraws.setCashStatus(CashProcessStatusEnum.WAITING.getType());
        cashDraws.setCreateTime(new Date());
        cashDrawsDao.create(cashDraws);
        log.info("生成提款申请记录");
        MoneyDetails moneyDetails = new MoneyDetails();
        moneyDetails.setOperatorId(user.getId());
        moneyDetails.setTargetId(user.getId());
        moneyDetails.setAction(MoneyOperateTypeEnum.USER_DRAW_CASH.getType());
        moneyDetails.setMoney(money.negate());
        moneyDetails.setSum(newBalance);
        moneyDetails.setCashId(cashDraws.getCashId());
        moneyDetails.setCreateTime(new Date());
        mdService.drawSave(moneyDetails);
        log.info("记录账户提款流水至t_money_details");
        user.setBalance(newBalance);
        userService.update(user);
        log.info("更新用户余额");
        userService.updateRedisUser(user);
        log.info("更新redisUser");
        CashDrawsVO vo = new CashDrawsVO();
        BeanUtil.copyProperties(cashDraws , vo);
        vo.setTips(getCashDrawsTips());
        return vo;
    }

    /**
     * 根据提现时间获取提醒文案
     * 注：每周二、周五进行打款（周二办理周四-周日的提现申请，周五办理周一到周三的提现申请）
     * @return
     */
    public String getCashDrawsTips() {
        int weekInt = DateUtil.thisDayOfWeekEnum().getValue();
        boolean flag = weekInt >= Week.MONDAY.getValue() && weekInt <= Week.WEDNESDAY.getValue();
        if(flag) {
            return Constant.CASH_DRAWS_NEXT_FRIDAY_TIPS;
        }
        return Constant.CASH_DRAWS_NEXT_TUESDAY_TIPS;
    }

    @Override
    public PageInfo<CashDraws> list(CashDrawsVO cashDrawsVO, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize, "create_time DESC");
        List<CashDraws> list = cashDrawsDao.findByParameter(cashDrawsVO);
        return new PageInfo(list);
    }

    @Override
    public List<CashDraws> list(CashDrawsVO cashDrawsVO) {
        List<CashDraws> list = cashDrawsDao.findListOrderByCreateTime(cashDrawsVO);
        return list;
    }


    /**
     * 管理员-打款
     *
     * @param cashId
     * @param comment
     * @return
     */
    @Override
    public CashDraws draw(Integer cashId, String comment) {
        Admin admin = adminService.getCurrentUser();
        log.info("调用打款接口,入参cashId:{},操作人:{}", cashId, admin.getId());
        CashDraws cashDraws = findById(cashId);
        if (null == cashDraws) {
            return null;
        }
        cashDraws.setOperator(admin.getName());
        cashDraws.setComment(comment);
        //修改为已处理状态
        cashDraws.setCashStatus(CashProcessStatusEnum.DONE.getType());
        //订单处理号暂做保留
        cashDraws.setCashNo(null);
        cashDraws.setProcessTime(new Date());
        cashDrawsDao.update(cashDraws);

        MoneyDetailsVO detailsVO = new MoneyDetailsVO();
        detailsVO.setCashId(cashId);
        detailsVO.setAction(MoneyOperateTypeEnum.USER_DRAW_CASH.getType());
        List<MoneyDetails> moneyDetailsList = moneyDetailsDao.findByParameter(detailsVO);
        if(CollectionUtil.isEmpty(moneyDetailsList)) {
            return null;
        }
        MoneyDetails details = moneyDetailsList.get(0);
        details.setAction(MoneyOperateTypeEnum.USER_DRAW_CASH_DONE.getType());
        mdService.update(details);
        return cashDraws;
    }

    @Override
    public boolean refuse(Integer cashId, String comment) {
        Admin admin = adminService.getCurrentUser();
        log.info("调用拒绝打款接口,入参cashId:{},操作人:{}", cashId, admin.getId());
        CashDraws cashDraws = findById(cashId);
        if (null == cashDraws) {
            return false;
        }
        cashDraws.setOperator(admin.getName());
        cashDraws.setComment(comment);
        //修改为"已拒绝"状态
        cashDraws.setCashStatus(CashProcessStatusEnum.REFUSE.getType());
        cashDraws.setProcessTime(new Date());
        cashDrawsDao.update(cashDraws);

        //返款
        User user = userService.findById(cashDraws.getUserId());
        BigDecimal balance = user.getBalance();
        log.info("管理员拒绝打款前，用户账户余额:{}", balance);
        balance = balance.add(cashDraws.getMoney());
        log.info("管理员拒绝打款后，用户账户余额:{}", balance);

        //原表记录action状态修改为“管理员拒绝打款”
        MoneyDetailsVO detailsVO = new MoneyDetailsVO();
        detailsVO.setCashId(cashId);
        detailsVO.setAction(MoneyOperateTypeEnum.USER_DRAW_CASH.getType());
        List<MoneyDetails> moneyDetailsList = moneyDetailsDao.findByParameter(detailsVO);
        if(CollectionUtil.isEmpty(moneyDetailsList)) {
            return false;
        }
        MoneyDetails details = moneyDetailsList.get(0);
        details.setAction(MoneyOperateTypeEnum.ADMIN_REFUSE_REMIT.getType());
        mdService.update(details);


        //新增action状态为“金额退回”的表记录
        MoneyDetails moneyDetails = new MoneyDetails();
        moneyDetails.setOperatorId(admin.getId());
        moneyDetails.setAction(MoneyOperateTypeEnum.REFUND_DONE.getType());
        moneyDetails.setTargetId(cashDraws.getUserId());
        moneyDetails.setSum(balance);
        moneyDetails.setMoney(cashDraws.getMoney());
        moneyDetails.setCashId(cashId);
        moneyDetails.setRemark(comment);
        moneyDetails.setCreateTime(new Date());
        mdService.create(moneyDetails);

        user.setBalance(balance);
        user.setUpdateTime(new Date());
        userService.update(user);
        return true;
    }
}
