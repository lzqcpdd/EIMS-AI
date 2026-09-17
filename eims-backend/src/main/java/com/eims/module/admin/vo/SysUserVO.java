package com.eims.module.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysUserVO {
    private Long id;
    private Long deptId;
    private String deptName;
    private String userName;
    private String nickName;
    private String phone;
    private String email;
    private Integer status;
    private List<Long> roleIds;
    private String roleNames;
    private LocalDateTime createTime;
}
