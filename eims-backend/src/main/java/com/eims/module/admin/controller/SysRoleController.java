package com.eims.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.admin.dto.AssignMenuRequest;
import com.eims.module.admin.entity.SysRole;
import com.eims.module.admin.mapper.SysRoleMapper;
import com.eims.module.admin.service.AdminService;
import com.eims.module.admin.vo.SysMenuTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleMapper mapper;
    private final AdminService adminService;

    @GetMapping
    public Result<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getIsDeleted, 0)
                .orderByDesc(SysRole::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysRole::getRoleName, keyword)
                    .or().like(SysRole::getRoleCode, keyword));
        }
        mapper.selectPage(page, qw);
        return Result.ok(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/menus/tree")
    public Result<List<SysMenuTreeVO>> menuTree() {
        return Result.ok(adminService.menuTree());
    }

    @GetMapping("/{id:\\d+}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long id) {
        return Result.ok(adminService.getRoleMenuIds(id));
    }

    @PutMapping("/{id:\\d+}/menus")
    public Result<Void> assignRoleMenus(@PathVariable Long id, @RequestBody AssignMenuRequest request) {
        adminService.assignRoleMenus(id, request);
        return Result.ok();
    }

    @GetMapping("/{id:\\d+}")
    public Result<SysRole> get(@PathVariable Long id) {
        SysRole entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("角色不存在");
        return Result.ok(entity);
    }

    @PostMapping
    public Result<SysRole> create(@RequestBody SysRole entity) {
        if (entity.getStatus() == null) entity.setStatus(1);
        mapper.insert(entity);
        return Result.ok(entity);
    }

    @PutMapping("/{id:\\d+}")
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole entity) {
        entity.setId(id);
        mapper.updateById(entity);
        return Result.ok(entity);
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        adminService.deleteRole(id);
        return Result.ok();
    }
}
