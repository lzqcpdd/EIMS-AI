package com.eims.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.admin.entity.SysOperationLog;
import com.eims.module.admin.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final SysOperationLogMapper mapper;

    @GetMapping
    public Result<PageResult<SysOperationLog>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<SysOperationLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysOperationLog> qw = new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getOperationTime);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysOperationLog::getModuleName, keyword)
                    .or().like(SysOperationLog::getOperationType, keyword)
                    .or().like(SysOperationLog::getRequestUri, keyword));
        }
        mapper.selectPage(page, qw);
        return Result.ok(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/{id}")
    public Result<SysOperationLog> get(@PathVariable Long id) {
        SysOperationLog entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("操作日志不存在");
        return Result.ok(entity);
    }
}
