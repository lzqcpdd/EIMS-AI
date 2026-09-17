package com.eims.module.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_quotation")
public class SalesQuotation extends BaseEntity {
    private String quotationCode;
    private Long opportunityId;
    private Long customerId;
    private BigDecimal totalAmount;
    private LocalDateTime validUntil;
    private Integer status;
}