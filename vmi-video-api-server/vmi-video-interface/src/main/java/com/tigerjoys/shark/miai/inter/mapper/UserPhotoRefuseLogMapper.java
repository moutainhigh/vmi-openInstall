package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.UserPhotoRefuseLogEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  用户头像审核拒绝记录表[t_user_photo_refuse_log]表 dao通用操作接口实现类
 * @author lipeng
 * @Date 2019-12-14 16:36:00
 *
 */
@Producer(entityType=UserPhotoRefuseLogEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface UserPhotoRefuseLogMapper extends BaseMapper<UserPhotoRefuseLogEntity> {
    
}