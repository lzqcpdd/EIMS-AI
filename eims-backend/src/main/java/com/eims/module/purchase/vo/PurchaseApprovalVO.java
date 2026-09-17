package com.eims.module.purchase.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PurchaseApprovalVO {
    private Long id;
    private Long approverId;
    private String approverName;
    private Integer approvalLevel;
    private String approvalLevelLabel;
    private Integer result;
    private String resultLabel;
    private String comment;
    private LocalDateTime approvalTime;
}
