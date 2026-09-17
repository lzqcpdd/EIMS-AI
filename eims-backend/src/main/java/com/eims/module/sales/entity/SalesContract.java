package com.eims.module.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_contract")
public class SalesContract extends BaseEntity {
    private String contractCode;
    private Long customerId;
    private Long quotationId;
    private BigDecimal totalAmount;
    private Integer signStatus;
    private LocalDateTime signTime;
}