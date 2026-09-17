package com.eims.module.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_after_service")
public class SalesAfterService extends BaseEntity {
    private String serviceCode;
    private Long orderId;
    private Long customerId;
    private Integer serviceType;
    private Integer status;
    private String content;
}