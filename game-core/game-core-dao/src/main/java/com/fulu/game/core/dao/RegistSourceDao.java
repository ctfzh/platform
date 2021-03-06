package com.fulu.game.core.dao;

import com.fulu.game.core.entity.RegistSource;
import com.fulu.game.core.entity.vo.RegistSourceVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yanbiao
 * @date 2018-06-13 15:33:21
 */
@Mapper
public interface RegistSourceDao extends ICommonDao<RegistSource, Integer> {

    List<RegistSource> findByParameter(RegistSourceVO registSourceVO);

    List<RegistSourceVO> listWithCount();

    /**
     * 查询CJ的注册来源详情
     *
     * @return 注册来源Bean
     */
    RegistSource findCjRegistSource();

    /**
     * 根据名称查询注册来源
     *
     * @return 注册名称
     */
    RegistSource findByName(String name);
}
