package com.eims.module.wms.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StocktakeVO {
    private Long id;
    private String stocktakeCode;
    private Long warehouseId;
    private String warehouseName;
    private Integer status;
    private String statusLabel;
    private LocalDateTime createTime;
}
