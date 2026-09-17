package com.eims.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_approval")
public class PurchaseApproval extends BaseEntity {
    private Long requestId;
    private Long approverId;
    private Integer approvalLevel;
    private Integer result;
    private String comment;
    private LocalDateTime approvalTime;
}