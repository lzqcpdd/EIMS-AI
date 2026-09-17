package com.eims.module.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("material")
public class Material extends BaseEntity {
    private String materialCode;
    private String materialName;
    private String category;
    private String spec;
    private String unit;
    private Integer safetyStock;
    private Integer status;
}