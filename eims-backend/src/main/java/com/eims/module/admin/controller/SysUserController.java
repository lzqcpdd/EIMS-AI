package com.eims.module.admin.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.admin.dto.AssignRoleRequest;
import com.eims.module.admin.entity.SysUser;
import com.eims.module.admin.mapper.SysUserMapper;
import com.eims.module.admin.service.AdminService;
import com.eims.module.admin.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserMapper mapper;
    private final AdminService adminService;

    @GetMapping
    public Result<PageResult<SysUserVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.pageUsers(current, size, keyword));
    }

    @GetMapping("/{id:\\d+}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        return Result.ok(adminService.getUserRoleIds(id));
    }

    @PutMapping("/{id:\\d+}/roles")
    public Result<Void> assignUserRoles(@PathVariable Long id, @RequestBody AssignRoleRequest request) {
        adminService.assignUserRoles(id, request);
        return Result.ok();
    }

    @GetMapping("/{id:\\d+}")
    public Result<SysUser> get(@PathVariable Long id) {
        SysUser entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("用户不存在");
        entity.setPassword(null);
        return Result.ok(entity);
    }

    @PostMapping
    public Result<SysUser> create(@RequestBody SysUser entity) {
        return Result.ok(adminService.createUser(entity));
    }

    @PutMapping("/{id:\\d+}")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUser entity) {
        return Result.ok(adminService.updateUser(id, entity));
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.ok();
    }
}
