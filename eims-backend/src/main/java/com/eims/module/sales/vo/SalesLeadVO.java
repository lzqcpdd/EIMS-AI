package com.eims.module.sales.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalesLeadVO {
    private Long id;
    private String leadCode;
    private String customerName;
    private String source;
    private Integer level;
    private Long ownerId;
    private String ownerName;
    private Integer status;
    private String statusLabel;
    private String stageLabel;
    private Long opportunityId;
    private LocalDateTime createTime;
}
