package com.eims.module.sales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesQuotationSaveDTO {
    private Long id;
    private Long opportunityId;
    private BigDecimal totalAmount;
    private LocalDateTime validUntil;
}
