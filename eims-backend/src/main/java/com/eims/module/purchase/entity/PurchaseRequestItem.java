package com.eims.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_request_item")
public class PurchaseRequestItem extends BaseEntity {
    private Long requestId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime expectedDate;
}