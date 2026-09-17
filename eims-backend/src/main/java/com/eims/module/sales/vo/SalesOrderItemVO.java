package com.eims.module.sales.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderItemVO {
    private Long id;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Integer deliveredQuantity;
}
