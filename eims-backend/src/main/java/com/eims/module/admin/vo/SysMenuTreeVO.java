package com.eims.module.admin.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysMenuTreeVO {
    private Long id;
    private Long parentId;
    private String menuName;
    private String menuCode;
    private String path;
    private List<SysMenuTreeVO> children = new ArrayList<>();
}
