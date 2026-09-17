package com.eims.module.wms.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryVO {
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private Integer lockedQuantity;
    private Integer safetyStock;
    private Boolean lowStock;
    private LocalDateTime updateTime;
}
