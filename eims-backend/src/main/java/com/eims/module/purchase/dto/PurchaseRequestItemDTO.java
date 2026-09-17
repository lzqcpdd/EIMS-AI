package com.eims.module.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRequestItemDTO {
    private Long id;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private BigDecimal unitPrice;
    /** 本订单已入库数量 */
    private Integer receivedQuantity;
}
