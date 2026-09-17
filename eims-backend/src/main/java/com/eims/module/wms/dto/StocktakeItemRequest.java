package com.eims.module.wms.dto;

import lombok.Data;

@Data
public class StocktakeItemRequest {
    private Long materialId;
    private Integer actualQuantity;
}
