package com.eims.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {
    private String orderCode;
    private Long requestId;
    private Long supplierId;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime expectedArrivalTime;
}