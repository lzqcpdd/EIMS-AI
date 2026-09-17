package com.eims.module.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier")
public class Supplier extends BaseEntity {
    private String supplierCode;
    private String supplierName;
    private String contactPerson;
    private String contactPhone;
    private String bankAccount;
    private Integer qualificationStatus;
    private BigDecimal score;
}