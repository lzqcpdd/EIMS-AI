package com.eims.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_request")
public class PurchaseRequest extends BaseEntity {
    private String requestCode;
    private Long deptId;
    private Long applicantId;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime submitTime;
}