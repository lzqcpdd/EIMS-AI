package com.eims.module.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_opportunity")
public class SalesOpportunity extends BaseEntity {
    private String opportunityCode;
    private Long leadId;
    private Long customerId;
    private Integer opportunityLevel;
    private BigDecimal expectedAmount;
    private Integer stage;
}