package com.eims.module.admin.controller;

import com.eims.common.result.Result;
import com.eims.module.admin.entity.SysMenu;
import com.eims.module.admin.mapper.SysMenuMapper;
import com.eims.security.AuthService;
import com.eims.security.JwtUtils;
import com.eims.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final SysMenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM sys_user WHERE user_name=? AND is_deleted=0", Long.class, req.getUsername());
        LoginUser loginUser = authService.buildLoginUser(userId);
        String token = jwtUtils.generateToken(userId, req.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", loginUser);
        data.put("menus", buildMenuTree(loginUser.getPermissions()));
        return Result.ok(data);
    }

    @GetMapping("/user/current")
    public Result<LoginUser> currentUser() {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(user);
    }

    @GetMapping("/permissions")
    public Result<List<String>> permissions() {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(user.getPermissions());
    }

    private List<Map<String, Object>> buildMenuTree(List<String> permissions) {
        List<SysMenu> all = menuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getIsDeleted, 0)
                        .in(SysMenu::getMenuType, 1, 2)
                        .orderByAsc(SysMenu::getSortNo));
        List<SysMenu> filtered = all.stream()
                .filter(m -> permissions.contains(m.getMenuCode()) || m.getMenuType() == 1)
                .collect(Collectors.toList());

        Map<Long, Map<String, Object>> map = new LinkedHashMap<>();
        for (SysMenu m : filtered) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", m.getId());
            node.put("name", m.getMenuName());
            node.put("path", m.getPath());
            node.put("icon", m.getIcon());
            node.put("children", new ArrayList<>());
            map.put(m.getId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (SysMenu m : filtered) {
            Map<String, Object> node = map.get(m.getId());
            if (m.getParentId() == null || m.getParentId() == 0) {
                roots.add(node);
            } else if (map.containsKey(m.getParentId())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) map.get(m.getParentId()).get("children");
                children.add(node);
            }
        }
        roots.removeIf(r -> {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ch = (List<Map<String, Object>>) r.get("children");
            return ch.isEmpty() && !permissions.contains(findMenuCode(filtered, (Long) r.get("id")));
        });
        return roots;
    }

    private String findMenuCode(List<SysMenu> menus, Long id) {
        return menus.stream().filter(m -> m.getId().equals(id)).map(SysMenu::getMenuCode).findFirst().orElse("");
    }

    @Data
    static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }
}
