package com.eims.module.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.eims.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private Long deptId;
    private String userName;
    private String nickName;
    private String password;
    private String phone;
    private String email;
    private Integer status;
}