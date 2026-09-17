package com.eims.security;

import lombok.Data;

import java.util.List;

@Data
public class LoginUser {
    private Long userId;
    private String userName;
    private String nickName;
    private Long deptId;
    private List<String> roles;
    private List<String> permissions;
}
