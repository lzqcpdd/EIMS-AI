package com.eims.module.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
public class Inventory extends BaseEntity {
    private Long warehouseId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private Integer lockedQuantity;
    private Integer safetyStock;
}