package com.eims.module.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseQuoteVO {
    private Long id;
    private Long requestId;
    private String requestCode;
    private BigDecimal requestAmount;
    private Long supplierId;
    private String supplierName;
    private BigDecimal quoteAmount;
    private Integer deliveryDays;
    private BigDecimal qualityScore;
    private Integer isSelected;
    private String selectedLabel;
    private LocalDateTime createTime;
    /** 与申请预算差额（报价-申请金额） */
    private BigDecimal amountDiff;
    /** 是否最低价 */
    private Boolean lowestPrice;
    /** 是否最短交期 */
    private Boolean fastestDelivery;
}
