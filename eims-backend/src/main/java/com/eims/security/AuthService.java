package com.eims.security;

import com.eims.module.admin.entity.SysUser;
import com.eims.module.admin.mapper.SysMenuMapper;
import com.eims.module.admin.mapper.SysRoleMapper;
import com.eims.module.admin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final JdbcTemplate jdbcTemplate;

    public LoginUser buildLoginUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUserName(user.getUserName());
        loginUser.setNickName(user.getNickName());
        loginUser.setDeptId(user.getDeptId());

        List<String> roles = jdbcTemplate.queryForList(
                "SELECT r.role_code FROM sys_role r JOIN sys_user_role ur ON r.id=ur.role_id WHERE ur.user_id=? AND r.is_deleted=0",
                String.class, userId);
        loginUser.setRoles(roles);

        List<String> permissions = jdbcTemplate.queryForList(
                "SELECT DISTINCT m.menu_code FROM sys_menu m JOIN sys_role_menu rm ON m.id=rm.menu_id " +
                        "JOIN sys_user_role ur ON rm.role_id=ur.role_id WHERE ur.user_id=? AND m.is_deleted=0",
                String.class, userId);
        loginUser.setPermissions(permissions);
        return loginUser;
    }
}
