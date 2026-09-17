package com.eims.module.base.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.base.entity.Warehouse;
import com.eims.module.base.mapper.WarehouseMapper;
import com.eims.module.base.service.BaseDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseMapper mapper;
    private final BaseDataService baseDataService;

    @GetMapping
    public Result<PageResult<Warehouse>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(baseDataService.pageWarehouses(current, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Warehouse> get(@PathVariable Long id) {
        Warehouse entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("仓库不存在");
        return Result.ok(entity);
    }

    @PostMapping
    public Result<Warehouse> create(@RequestBody Warehouse entity) {
        return Result.ok(baseDataService.createWarehouse(entity));
    }

    @PutMapping("/{id}")
    public Result<Warehouse> update(@PathVariable Long id, @RequestBody Warehouse entity) {
        entity.setId(id);
        mapper.updateById(entity);
        return Result.ok(entity);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        baseDataService.deleteWarehouse(id);
        return Result.ok();
    }
}
