package com.eims.module.sales.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalesOrderVO {
    private Long id;
    private String orderCode;
    private Long contractId;
    private String contractCode;
    private Long opportunityId;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusLabel;
    private Integer paymentStatus;
    private String paymentStatusLabel;
    private LocalDateTime createTime;
    private List<SalesOrderItemVO> items;
}
