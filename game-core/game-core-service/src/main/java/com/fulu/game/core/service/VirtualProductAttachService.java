package com.fulu.game.core.service;

import com.fulu.game.core.entity.VirtualProductAttach;
import com.fulu.game.core.entity.vo.VirtualProductAttachVO;

import java.util.List;


/**
 * 虚拟商品附件表
 *
 * @author Gong Zechun
 * @email ${email}
 * @date 2018-08-30 15:05:30
 */
public interface VirtualProductAttachService extends ICommonService<VirtualProductAttach, Integer> {

    List<VirtualProductAttach> findByParameter(VirtualProductAttachVO virtualProductAttachVO);


    List<VirtualProductAttachVO> findByOrderProIdUserId(Integer userId , Integer productId);

    
    int deleteByVirtualProductId(int virtualProductId);

    
    List<VirtualProductAttachVO> findDetailByVo(VirtualProductAttachVO virtualProductAttachVO);


    /**
     * 根据商品ID获取附件信息
     * @param virtualProductId
     * @return
     */
    List<VirtualProductAttach> findByProductId(Integer virtualProductId);
}
