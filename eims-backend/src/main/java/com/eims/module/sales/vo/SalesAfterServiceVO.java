package com.eims.module.sales.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalesAfterServiceVO {
    private Long id;
    private String serviceCode;
    private Long orderId;
    private String orderCode;
    private Long customerId;
    private String customerName;
    private Integer serviceType;
    private String serviceTypeLabel;
    private Integer status;
    private String statusLabel;
    private String content;
    private LocalDateTime createTime;
}
