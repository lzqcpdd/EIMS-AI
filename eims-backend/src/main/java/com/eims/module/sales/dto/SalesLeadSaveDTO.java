package com.eims.module.sales.dto;

import lombok.Data;

@Data
public class SalesLeadSaveDTO {
    private Long id;
    private String customerName;
    private String source;
    private Integer level;
}
