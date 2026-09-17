package com.eims.module.wms.vo;

import lombok.Data;

@Data
public class StocktakeItemVO {
    private Long id;
    private Long stocktakeId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer diffQuantity;
}
