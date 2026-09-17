package com.eims.module.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseQuoteSaveDTO {
    private Long id;
    private Long requestId;
    private Long supplierId;
    private BigDecimal quoteAmount;
    private Integer deliveryDays;
    private BigDecimal qualityScore;
}
