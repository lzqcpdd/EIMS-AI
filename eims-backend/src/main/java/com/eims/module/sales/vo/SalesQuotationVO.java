package com.eims.module.sales.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesQuotationVO {
    private Long id;
    private String quotationCode;
    private Long opportunityId;
    private String opportunityCode;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private LocalDateTime validUntil;
    private Integer status;
    private String statusLabel;
    private LocalDateTime createTime;
}
