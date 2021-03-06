package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.AMPageEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  [t_a_m_page]表 dao通用操作接口实现类
 * @author shiming
 * @Date 2018-11-14 16:14:24
 *
 */
@Producer(entityType=AMPageEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface AMPageMapper extends BaseMapper<AMPageEntity> {
    
}