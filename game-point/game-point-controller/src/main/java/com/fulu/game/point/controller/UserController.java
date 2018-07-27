package com.fulu.game.point.controller;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.fulu.game.common.Result;
import com.fulu.game.common.config.WxMaServiceSupply;
import com.fulu.game.common.enums.RedisKeyEnum;
import com.fulu.game.common.enums.WechatEcoEnum;
import com.fulu.game.common.exception.UserException;
import com.fulu.game.common.utils.SubjectUtil;
import com.fulu.game.core.entity.User;
import com.fulu.game.core.entity.vo.WxUserInfo;
import com.fulu.game.core.service.UserService;
import com.fulu.game.core.service.impl.RedisOpenServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 用户个人中心Controller
 *
 * @author Gong ZeChun
 * @date 2018/7/27 11:51
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/user")
public class UserController extends BaseController {

    private final UserService userService;
    @Autowired
    private WxMaServiceSupply wxMaServiceSupply;
    @Autowired
    private RedisOpenServiceImpl redisOpenService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户-我的钱包-查询余额
     * 账户金额不能从缓存取，因为存在管理员给用户加零钱缓存并未更新
     *
     * @return 封装结果集
     */
    @PostMapping("/balance/get")
    public Result getBalance() {
        User user = userService.findById(userService.getCurrentUser().getId());
        return Result.success().data(user.getBalance()).msg("查询成功！");
    }



    /**
     * 用户-进入我的页面
     *
     * @return
     */
    @PostMapping("/get")
    public Result get() {
        User user = userService.findById(userService.getCurrentUser().getId());
        return Result.success().data(user).msg("查询信息成功！");
    }

    /**
     * 获取用户微信手机号
     *
     * @param encryptedData
     * @param iv
     * @return
     */
    @PostMapping("/wxinfo/mobile")
    public Result getWinfoMobile(String encryptedData,
                                 String iv) {

        WxMaPhoneNumberInfo phoneNoInfo = null;
        String sessionKey = redisOpenService.get(RedisKeyEnum.WX_SESSION_KEY.generateKey(SubjectUtil.getToken()));
        try {
            phoneNoInfo = wxMaServiceSupply.gameWxMaService().getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
        } catch (Exception e) {
            log.error("获取用户微信异常:encryptedData:{};iv:{};sessionKey:{};{}", encryptedData, iv, sessionKey, e.getMessage());
            throw new UserException(UserException.ExceptionCode.SESSION_KEY_DISABLE_EXCEPTION);
        }
        if (phoneNoInfo == null && phoneNoInfo.getPurePhoneNumber() == null) {
            log.error("未获取用户微信手机号:phoneNoInfo:{};encryptedData:{};iv:{};sessionKey:{};", phoneNoInfo, encryptedData, iv, sessionKey);
            throw new UserException(UserException.ExceptionCode.WX_PHONE_NOT_EXIST_EXCEPTION);
        }
        User user = userService.findById(userService.getCurrentUser().getId());
        log.info("获取用户微信手机号,原用户信息:{}", user);
        user.setMobile(phoneNoInfo.getPurePhoneNumber());
        user.setUpdateTime(new Date());
        userService.update(user);
        userService.updateRedisUser(user);
        log.info("获取用户微信手机号,更新后用户信息:{};手机号:{};", user, user.getMobile());
        return Result.success().data(user);

    }



    /**
     * 保存微信信息
     * @param wxUserInfo
     * @return
     */
    @PostMapping("/wxinfo/save")
    public Result saveWxUserInfo(WxUserInfo wxUserInfo) {
        User user = userService.getCurrentUser();
        WxMaUserInfo wxMaUserInfo = null;
        try {
            String sessionKey = redisOpenService.get(RedisKeyEnum.WX_SESSION_KEY.generateKey(SubjectUtil.getToken()));
            wxMaUserInfo =wxMaServiceSupply.pointWxMaService().getUserService().getUserInfo(sessionKey, wxUserInfo.getEncryptedData(), wxUserInfo.getIv());
        } catch (Exception e) {
            log.error("获取用户微信异常:wxUserInfo:{};{}", wxUserInfo, e.getMessage());
            throw new UserException(UserException.ExceptionCode.SESSION_KEY_DISABLE_EXCEPTION);
        }
        user.setUnionId(wxMaUserInfo.getUnionId());
        if (user.getGender() == null) {
            user.setGender(wxUserInfo.getGender());
        }
        if (user.getNickname() == null) {
            user.setNickname(wxUserInfo.getNickName());
        }
        if (user.getHeadPortraitsUrl() == null) {
            user.setHeadPortraitsUrl(wxUserInfo.getAvatarUrl());
        }
        if (user.getCity() == null) {
            user.setCity(wxUserInfo.getCity());
        }
        if (user.getProvince() == null) {
            user.setProvince(wxUserInfo.getProvince());
        }
        if (user.getCountry() == null) {
            user.setCountry(wxUserInfo.getCountry());
        }
        user.setUpdateTime(new Date());
        userService.updateUnionUser(user, WechatEcoEnum.POINT);
        return Result.success().data(user);
    }



}