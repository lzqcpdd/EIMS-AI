package com.eims.module.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("code_rule")
public class CodeRule extends BaseEntity {
    private String ruleCode;
    private String ruleName;
    private String prefix;
    private String datePattern;
    private Integer seqLength;
    private Integer currentSeq;
    private Integer status;
}