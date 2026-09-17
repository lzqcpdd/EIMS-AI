package com.eims.module.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_lead")
public class SalesLead extends BaseEntity {
    private String leadCode;
    private String customerName;
    private String source;
    private Integer level;
    private Long ownerId;
    private Integer status;
}