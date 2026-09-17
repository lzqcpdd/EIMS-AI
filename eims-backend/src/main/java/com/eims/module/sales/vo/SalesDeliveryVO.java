package com.eims.module.sales.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalesDeliveryVO {
    private Long id;
    private String deliveryCode;
    private Long orderId;
    private String orderCode;
    private Long warehouseId;
    private String warehouseName;
    private Integer status;
    private String statusLabel;
    private String logisticsNo;
    private LocalDateTime createTime;
}
