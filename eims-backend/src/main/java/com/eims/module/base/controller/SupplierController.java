package com.eims.module.base.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.base.entity.Supplier;
import com.eims.module.base.mapper.SupplierMapper;
import com.eims.module.base.service.BaseDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierMapper mapper;
    private final BaseDataService baseDataService;

    @GetMapping
    public Result<PageResult<Supplier>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(baseDataService.pageSuppliers(current, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Supplier> get(@PathVariable Long id) {
        Supplier entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("供应商不存在");
        return Result.ok(entity);
    }

    @PostMapping
    public Result<Supplier> create(@RequestBody Supplier entity) {
        return Result.ok(baseDataService.createSupplier(entity));
    }

    @PutMapping("/{id}")
    public Result<Supplier> update(@PathVariable Long id, @RequestBody Supplier entity) {
        entity.setId(id);
        mapper.updateById(entity);
        return Result.ok(entity);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        baseDataService.deleteSupplier(id);
        return Result.ok();
    }
}
