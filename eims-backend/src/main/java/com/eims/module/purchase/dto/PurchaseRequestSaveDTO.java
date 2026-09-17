package com.eims.module.purchase.dto;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseRequestSaveDTO {
    private Long id;
    private String remark;
    private List<PurchaseRequestItemDTO> items;
}
