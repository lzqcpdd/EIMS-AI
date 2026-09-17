package com.eims.module.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_order_item")
public class SalesOrderItem extends BaseEntity {
    private Long orderId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Integer deliveredQuantity;
}