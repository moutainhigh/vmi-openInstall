package com.tigerjoys.shark.miai.inter.mapper;

import org.apache.ibatis.annotations.Producer;
import com.tigerjoys.nbs.mybatis.core.provider.DefaultSqlProvider;
import com.tigerjoys.shark.miai.inter.entity.RechargePriceEntity;
import com.tigerjoys.nbs.mybatis.core.BaseMapper;
import com.tigerjoys.nbs.mybatis.core.annotation.Mapper;

/**
 * 数据库  充值价格列表[t_recharge_price]表 dao通用操作接口实现类
 * @author yangjunming
 * @Date 2019-07-26 09:30:54
 *
 */
@Producer(entityType=RechargePriceEntity.class,providerType=DefaultSqlProvider.class)
@Mapper
public interface RechargePriceMapper extends BaseMapper<RechargePriceEntity> {
    
}