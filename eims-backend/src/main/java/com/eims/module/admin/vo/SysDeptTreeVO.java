package com.eims.module.admin.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysDeptTreeVO {
    private Long id;
    private Long parentId;
    private String deptName;
    private String deptCode;
    private Integer sortNo;
    private Integer status;
    private List<SysDeptTreeVO> children = new ArrayList<>();
}
