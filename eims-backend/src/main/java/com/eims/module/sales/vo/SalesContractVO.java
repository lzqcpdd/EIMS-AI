package com.eims.module.sales.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesContractVO {
    private Long id;
    private String contractCode;
    private Long customerId;
    private String customerName;
    private Long quotationId;
    private String quotationCode;
    private Long opportunityId;
    private BigDecimal totalAmount;
    private Integer signStatus;
    private String signStatusLabel;
    private LocalDateTime signTime;
    private LocalDateTime createTime;
}
