package com.fulu.game.core.service;

import com.fulu.game.core.entity.SysUser;


/**
 * 
 * 
 * @author wangbin
 * @email ${email}
 * @date 2018-04-14 22:41:42
 */
public interface SysUserService extends ICommonService<SysUser,Integer>{

    public SysUser findByUsername(String userName);
}