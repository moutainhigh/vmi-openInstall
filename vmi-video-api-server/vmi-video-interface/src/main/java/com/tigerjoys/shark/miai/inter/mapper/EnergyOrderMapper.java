package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.EnergyOrderEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  充值订单表[t_energy_order]表 dao通用操作接口实现类
 * @author mouzhanpeng
 * @Date 2018-08-16 10:41:55
 *
 */
@Producer(entityType=EnergyOrderEntity.class,providerType=DefaultSqlProvider.class,increment=false)
@Mapper
public interface EnergyOrderMapper extends BaseMapper<EnergyOrderEntity> {
    
}