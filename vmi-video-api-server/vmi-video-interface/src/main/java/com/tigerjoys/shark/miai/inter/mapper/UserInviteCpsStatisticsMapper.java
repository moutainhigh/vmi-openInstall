package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.UserInviteCpsStatisticsEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  [t_user_invite_cps_statistics]表 dao通用操作接口实现类
 * @author yangjunming
 * @Date 2019-10-17 11:23:55
 *
 */
@Producer(entityType=UserInviteCpsStatisticsEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface UserInviteCpsStatisticsMapper extends BaseMapper<UserInviteCpsStatisticsEntity> {
    
}