package com.eims.module.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stocktake_item")
public class StocktakeItem extends BaseEntity {
    private Long stocktakeId;
    private Long materialId;
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer diffQuantity;
}