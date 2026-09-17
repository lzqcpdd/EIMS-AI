package com.eims.module.base.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.base.entity.Material;
import com.eims.module.base.mapper.MaterialMapper;
import com.eims.module.base.service.BaseDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialMapper mapper;
    private final BaseDataService baseDataService;

    @GetMapping
    public Result<PageResult<Material>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(baseDataService.pageMaterials(current, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Material> get(@PathVariable Long id) {
        Material entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("物料不存在");
        return Result.ok(entity);
    }

    @PostMapping
    public Result<Material> create(@RequestBody Material entity) {
        return Result.ok(baseDataService.createMaterial(entity));
    }

    @PutMapping("/{id}")
    public Result<Material> update(@PathVariable Long id, @RequestBody Material entity) {
        return Result.ok(baseDataService.updateMaterial(id, entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        baseDataService.deleteMaterial(id);
        return Result.ok();
    }
}
