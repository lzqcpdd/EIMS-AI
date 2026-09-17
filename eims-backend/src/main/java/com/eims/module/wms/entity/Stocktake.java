package com.eims.module.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stocktake")
public class Stocktake extends BaseEntity {
    private String stocktakeCode;
    private Long warehouseId;
    private Integer status;
}