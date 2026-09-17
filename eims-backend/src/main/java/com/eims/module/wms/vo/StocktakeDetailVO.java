package com.eims.module.wms.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StocktakeDetailVO {
    private Long id;
    private String stocktakeCode;
    private Long warehouseId;
    private String warehouseName;
    private Integer status;
    private String statusLabel;
    private List<StocktakeItemVO> items = new ArrayList<>();
}
