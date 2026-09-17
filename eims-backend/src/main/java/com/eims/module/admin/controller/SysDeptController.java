package com.eims.module.admin.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.admin.entity.SysDept;
import com.eims.module.admin.mapper.SysDeptMapper;
import com.eims.module.admin.service.AdminService;
import com.eims.module.admin.vo.SysDeptTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptMapper mapper;
    private final AdminService adminService;

    @GetMapping
    public Result<PageResult<SysDept>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.pageDept(current, size, keyword));
    }

    @GetMapping("/tree")
    public Result<List<SysDeptTreeVO>> tree() {
        return Result.ok(adminService.deptTree());
    }

    @GetMapping("/{id:\\d+}")
    public Result<SysDept> get(@PathVariable Long id) {
        SysDept entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("部门不存在");
        return Result.ok(entity);
    }

    @PostMapping
    public Result<SysDept> create(@RequestBody SysDept entity) {
        return Result.ok(adminService.createDept(entity));
    }

    @PutMapping("/{id:\\d+}")
    public Result<SysDept> update(@PathVariable Long id, @RequestBody SysDept entity) {
        return Result.ok(adminService.updateDept(id, entity));
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        adminService.deleteDept(id);
        return Result.ok();
    }
}
