package com.eims.module.purchase.vo;

import com.eims.module.purchase.dto.PurchaseRequestItemDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseRequestVO {
    private Long id;
    private String requestCode;
    private Long deptId;
    private String deptName;
    private Long applicantId;
    private String applicantName;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusLabel;
    private String stageLabel;
    private Integer requiredApprovalLevel;
    private Integer currentApprovalLevel;
    private String currentApprovalLevelLabel;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
    private String remark;
    private List<PurchaseRequestItemDTO> items;
    private List<PurchaseApprovalVO> approvals;
}
