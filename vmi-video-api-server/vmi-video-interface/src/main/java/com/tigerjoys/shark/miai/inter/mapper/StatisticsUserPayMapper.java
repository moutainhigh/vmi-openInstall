package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.StatisticsUserPayEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  充值用户每日数据统计[t_statistics_user_pay]表 dao通用操作接口实现类
 * @author lipeng
 * @Date 2019-06-24 20:42:39
 *
 */
@Producer(entityType=StatisticsUserPayEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface StatisticsUserPayMapper extends BaseMapper<StatisticsUserPayEntity> {
    
}