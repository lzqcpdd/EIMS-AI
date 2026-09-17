package com.eims.module.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_record")
public class StockRecord extends BaseEntity {
    private String recordCode;
    private Long warehouseId;
    private Long materialId;
    private Integer businessType;
    private Integer quantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private Long businessId;
    private String remark;
}