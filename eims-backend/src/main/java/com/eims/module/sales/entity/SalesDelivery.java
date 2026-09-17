package com.eims.module.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_delivery")
public class SalesDelivery extends BaseEntity {
    private String deliveryCode;
    private Long orderId;
    private Long warehouseId;
    private Integer status;
    private String logisticsNo;
}