package com.eims.module.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.common.service.ReferentialGuardService;
import com.eims.module.base.entity.CodeRule;
import com.eims.module.base.mapper.CodeRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/code-rules")
@RequiredArgsConstructor
public class CodeRuleController {

    private final CodeRuleMapper mapper;
    private final ReferentialGuardService referentialGuardService;

    @GetMapping
    public Result<PageResult<CodeRule>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<CodeRule> page = new Page<>(current, size);
        LambdaQueryWrapper<CodeRule> qw = new LambdaQueryWrapper<CodeRule>()
                .eq(CodeRule::getIsDeleted, 0)
                .orderByDesc(CodeRule::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(CodeRule::getRuleCode, keyword)
                    .or().like(CodeRule::getRuleName, keyword));
        }
        mapper.selectPage(page, qw);
        return Result.ok(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/{id}")
    public Result<CodeRule> get(@PathVariable Long id) {
        CodeRule entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("编码规则不存在");
        return Result.ok(entity);
    }

    @PostMapping
    public Result<CodeRule> create(@RequestBody CodeRule entity) {
        if (!StringUtils.hasText(entity.getRuleCode())) {
            throw new BusinessException("规则码不能为空");
        }
        Long exists = mapper.selectCount(new LambdaQueryWrapper<CodeRule>()
                .eq(CodeRule::getRuleCode, entity.getRuleCode()));
        if (exists != null && exists > 0) {
            throw new BusinessException("规则码已存在");
        }
        if (entity.getDatePattern() == null) entity.setDatePattern("yyyyMMdd");
        if (entity.getSeqLength() == null) entity.setSeqLength(4);
        if (entity.getCurrentSeq() == null) entity.setCurrentSeq(0);
        if (entity.getStatus() == null) entity.setStatus(1);
        mapper.insert(entity);
        return Result.ok(entity);
    }

    @PutMapping("/{id}")
    public Result<CodeRule> update(@PathVariable Long id, @RequestBody CodeRule entity) {
        entity.setId(id);
        if (entity.getDatePattern() == null) entity.setDatePattern("yyyyMMdd");
        if (entity.getSeqLength() == null) entity.setSeqLength(4);
        if (entity.getStatus() == null) entity.setStatus(1);
        mapper.updateById(entity);
        return Result.ok(entity);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        CodeRule entity = mapper.selectById(id);
        if (entity != null) {
            referentialGuardService.ensureCodeRuleDeletable(entity.getRuleCode());
        }
        mapper.deleteById(id);
        return Result.ok();
    }
}
