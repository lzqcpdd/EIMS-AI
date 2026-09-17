package com.eims.module.wms.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockRecordVO {
    private Long id;
    private String recordCode;
    private Long warehouseId;
    private String warehouseName;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer businessType;
    private String businessTypeLabel;
    private Integer quantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private String remark;
    private LocalDateTime createTime;
}
