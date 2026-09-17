package com.eims.module.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {
    private String customerCode;
    private String customerName;
    private String region;
    private Integer level;
    private String contactPerson;
    private String contactPhone;
}