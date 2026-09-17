package com.eims.module.purchase.vo;

import com.eims.module.purchase.dto.PurchaseRequestItemDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderVO {
    private Long id;
    private String orderCode;
    private Long requestId;
    private String requestCode;
    private Long supplierId;
    private String supplierName;
    private String supplierContact;
    private String supplierPhone;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusLabel;
    private LocalDateTime expectedArrivalTime;
    private LocalDateTime createTime;
    private String applicantName;
    private String deptName;
    private List<PurchaseRequestItemDTO> items;
}
