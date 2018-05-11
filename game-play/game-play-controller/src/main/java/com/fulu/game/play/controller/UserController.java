package com.fulu.game.play.controller;

import com.fulu.game.common.Constant;
import com.fulu.game.common.Result;
import com.fulu.game.common.ResultStatus;
import com.fulu.game.common.enums.RedisKeyEnum;
import com.fulu.game.common.enums.exception.UserExceptionEnums;
import com.fulu.game.common.exception.UserException;
import com.fulu.game.common.utils.SMSUtil;
import com.fulu.game.common.utils.SubjectUtil;
import com.fulu.game.core.entity.User;
import com.fulu.game.core.entity.UserTechAuth;
import com.fulu.game.core.entity.vo.UserInfoVO;
import com.fulu.game.core.entity.vo.UserVO;
import com.fulu.game.core.entity.vo.WxUserInfo;
import com.fulu.game.core.service.UserInfoAuthService;
import com.fulu.game.core.service.UserService;
import com.fulu.game.core.service.UserTechAuthService;
import com.fulu.game.core.service.impl.RedisOpenServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/user")
public class UserController extends BaseController {

    @Autowired
    private UserTechAuthService userTechAuthService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserInfoAuthService userInfoAuthService;
    @Autowired
    private RedisOpenServiceImpl redisOpenService;

    @RequestMapping("tech/list")
    public Result userTechList() {
        User user = userService.getCurrentUser();
        //查询所有用户认证的技能
        List<UserTechAuth> techAuthList = userTechAuthService.findByUserId(user.getId(), true);
        return Result.success().data(techAuthList);
    }

    /**
     * 用户-查询余额
     * 账户金额不能从缓存取，因为存在管理员给用户加零钱缓存并未更新
     *
     * @return
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
    public Result get(@RequestParam(name = "mobile", required = false, defaultValue = "false") Boolean mobile,
                      @RequestParam(name = "idcard", required = false, defaultValue = "false") Boolean idcard,
                      @RequestParam(name = "gender", required = false, defaultValue = "false") Boolean gender,
                      @RequestParam(name = "realname", required = false, defaultValue = "false") Boolean realname,
                      @RequestParam(name = "age", required = false, defaultValue = "false") Boolean age) {
        User user = userService.findById(userService.getCurrentUser().getId());
        if (null != idcard && !idcard){
            user.setIdcard(null);
        }
        if (!realname){
            user.setRealname(null);
        }
        if (!gender){
            user.setGender(null);
        }
        if (!mobile){
            user.setMobile(null);
        }
        if (!age){
            user.setAge(null);
        }
        return Result.success().data(user).msg("查询信息成功！");
    }

    /**
     * 用户-更新个人信息
     *
     * @param userVO
     * @return
     */
    @RequestMapping("/update")
    public Result update(@ModelAttribute UserVO userVO) {
        User user = userService.findById(userService.getCurrentUser().getId());
        user.setAge(userVO.getAge());
        user.setGender(userVO.getGender());
        user.setCity(userVO.getCity());
        user.setProvince(userVO.getProvince());
        user.setCountry(userVO.getCountry());
        user.setBirth(userVO.getBirth());
        user.setConstellation(userVO.getConstellation());
        user.setNickname(userVO.getNickname());
        user.setHeadPortraitsUrl(userVO.getHeadPortraitsUrl());
        userService.update(user);
        userService.updateRedisUser(user);

        user.setBalance(null);
        user.setOpenId(null);
        user.setPassword(null);
        user.setSalt(null);
        user.setIdcard(null);
        user.setRealname(null);
        return Result.success().data(user).msg("个人信息设置成功！");
    }

    /**
     * 点击发送验证码接口
     *
     * @param mobile
     * @return
     */
    @PostMapping("/mobile/sms")
    public Result sms(@RequestParam("mobile") String mobile) {
        String token = SubjectUtil.getToken();
        //缓存中查找该手机是否有验证码
        if (redisOpenService.hasKey(RedisKeyEnum.SMS.generateKey(mobile))) {
            String times = redisOpenService.get(RedisKeyEnum.SMS.generateKey(mobile));
            if (Integer.parseInt(times) > Constant.MOBILE_CODE_SEND_TIMES_DEV) {
                return Result.error().msg("半小时内发送次数不能超过" + Constant.MOBILE_CODE_SEND_TIMES_DEV + "次，请等待！");
            } else {
                String verifyCode = SMSUtil.sendVerificationCode(mobile);
                log.info("手机号 {} 发送验证码为 {}", mobile, verifyCode);
                redisOpenService.hset(RedisKeyEnum.SMS.generateKey(token), mobile, verifyCode, Constant.VERIFYCODE_CACHE_TIME_DEV);
                times = String.valueOf(Integer.parseInt(times) + 1);
                redisOpenService.set(RedisKeyEnum.SMS.generateKey(mobile), times, Constant.MOBILE_CACHE_TIME_DEV);
                return Result.success().msg("验证码发送成功！");
            }
        } else {
            String verifyCode = SMSUtil.sendVerificationCode(mobile);
            log.info("重新计数，手机号 {} 发送验证码为 {}", mobile, verifyCode);
            redisOpenService.hset(RedisKeyEnum.SMS.generateKey(token), mobile, verifyCode, Constant.VERIFYCODE_CACHE_TIME_DEV);
            redisOpenService.set(RedisKeyEnum.SMS.generateKey(mobile), "1", Constant.MOBILE_CACHE_TIME_DEV);
            return Result.success().msg("验证码发送成功！");
        }
    }

    @PostMapping("/mobile/bind")
    public Result bind(@ModelAttribute WxUserInfo wxUserInfo) {

        String token = SubjectUtil.getToken();
        //验证手机号的验证码
        String redisVerifyCode = redisOpenService.hget(RedisKeyEnum.SMS.generateKey(token), wxUserInfo.getMobile());
        if (null == redisVerifyCode) {
            return Result.error().msg("验证码失效");
        } else {
            String verifyCode = wxUserInfo.getVerifyCode();
            if (verifyCode != null && !verifyCode.equals(redisVerifyCode)) {
                return Result.error().msg("验证码提交错误");
            } else {//绑定手机号
                User user = userService.getCurrentUser();
                String openId = user.getOpenId();
                if (user == null) {
                    return Result.error().msg("微信用户绑定失败！");
                }
                User newUser = null;
                User openIdUser = userService.findByOpenId(openId);
                //如果openId已经绑定手机号且手机号和绑定的手机号不一致,则返回错误
                if (openIdUser != null && openIdUser.getMobile() != null) {
                    if (!wxUserInfo.getMobile().equals(openIdUser.getMobile())) {
                        return Result.error().msg("已经绑定过手机号！");
                    }
                }
                User mobileUser = userService.findByMobile(wxUserInfo.getMobile());
                if (mobileUser != null) {
                    if (!mobileUser.getId().equals(openIdUser.getId())) {
                        mobileUser.setOpenId(openId);
                        mobileUser.setGender(wxUserInfo.getGender() != null ? Integer.parseInt(wxUserInfo.getGender()) : 0);
                        mobileUser.setNickname(wxUserInfo.getNickName());
                        mobileUser.setHeadPortraitsUrl(wxUserInfo.getAvatarUrl());
                        mobileUser.setCity(wxUserInfo.getCity());
                        mobileUser.setProvince(wxUserInfo.getProvince());
                        mobileUser.setCountry(wxUserInfo.getCountry());
                        mobileUser.setUpdateTime(new Date());
                        userService.update(mobileUser);
                        userService.deleteById(openIdUser.getId());
                    }
                    newUser = mobileUser;
                } else {
                    openIdUser.setMobile(wxUserInfo.getMobile());
                    openIdUser.setGender(wxUserInfo.getGender() != null ? Integer.parseInt(wxUserInfo.getGender()) : 0);
                    openIdUser.setNickname(wxUserInfo.getNickName());
                    openIdUser.setHeadPortraitsUrl(wxUserInfo.getAvatarUrl());
                    openIdUser.setCity(wxUserInfo.getCity());
                    openIdUser.setProvince(wxUserInfo.getProvince());
                    openIdUser.setCountry(wxUserInfo.getCountry());
                    openIdUser.setUpdateTime(new Date());
                    userService.update(openIdUser);
                    newUser = openIdUser;
                }
                userService.updateRedisUser(newUser);
                newUser.setOpenId(null);
                newUser.setBalance(null);
                return Result.success().data(newUser).msg("手机号绑定成功！");
            }
        }
    }

    @PostMapping("/im/save")
    public Result imSave(@RequestParam("status") Integer status,
                         @RequestParam("imId") String imId,
                         @RequestParam("imPsw") String imPsw) {
        log.info("IM注册请求开始,请求参数 status=={},imId=={}", status, imId);
        User user = userService.findById(userService.getCurrentUser().getId());
        if (status == 200) {
            user.setImId(imId);
            user.setImPsw(imPsw);
            userService.update(user);
            userService.updateRedisUser(user);
            log.info("用户{}绑定IM信息成功", user.getId());
        } else if (status == 500) {
            log.error("用户{}绑定IM失败", user.getId());
            return Result.error(ResultStatus.IM_REGIST_FAIL).msg("IM用户注册失败！");
        }
        return Result.success().msg("IM用户信息保存成功！");
    }

    /**
     * 用户-根据imId查询聊天对象User
     *
     * @return
     */
    @PostMapping("/im/get")
    public Result getImUser(@RequestParam("imId") String imId) {
        if(StringUtils.isEmpty(imId)){
            throw new UserException(UserExceptionEnums.IllEGAL_IMID_EXCEPTION);
        }
        User user = userService.findByImId(imId);
        if (null == user){
            return Result.error().msg("未查询到该用户或尚未注册IM");
        }
        return Result.success().data(user).msg("查询IM用户成功");
    }

    /**
     * 用户-根据imIds批量查询聊天对象UserList
     *
     * @return
     */
    @PostMapping("/im/list")
    public Result getImUserList(@RequestParam("imIds") String imIds) {
        List<User> userList = userService.findByImIds(imIds);
        return Result.success().data(userList).msg("查询IM用户成功！");
    }

    @PostMapping("chatwith/get")
    public Result chatWithGet(@RequestParam("id") Integer id) {
        UserInfoVO userInfoVO = userInfoAuthService.findUserCardByUserId(id, false, true, true, true);
        return Result.success().data(userInfoVO).msg("查询聊天对象信息成功！");
    }


}
