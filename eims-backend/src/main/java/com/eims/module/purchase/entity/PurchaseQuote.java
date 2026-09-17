package com.eims.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_quote")
public class PurchaseQuote extends BaseEntity {
    private Long requestId;
    private Long supplierId;
    private BigDecimal quoteAmount;
    private Integer deliveryDays;
    private BigDecimal qualityScore;
    private Integer isSelected;
}