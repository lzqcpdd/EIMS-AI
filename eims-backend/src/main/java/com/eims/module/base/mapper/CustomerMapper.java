package com.eims.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eims.module.base.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
