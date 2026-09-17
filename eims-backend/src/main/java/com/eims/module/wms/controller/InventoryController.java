package com.eims.module.wms.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.wms.entity.Inventory;
import com.eims.module.wms.service.WmsService;
import com.eims.module.wms.vo.InventoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wms/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final WmsService wmsService;

    @GetMapping
    public Result<PageResult<InventoryVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(wmsService.pageInventory(current, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Inventory> get(@PathVariable Long id) {
        throw new BusinessException("请通过扫码出入库或业务单据变更库存");
    }

    @PostMapping
    public Result<Inventory> create(@RequestBody Inventory entity) {
        throw new BusinessException("库存仅可通过扫码出入库或业务单据变更");
    }

    @PutMapping("/{id}")
    public Result<Inventory> update(@PathVariable Long id, @RequestBody Inventory entity) {
        throw new BusinessException("库存仅可通过扫码出入库或业务单据变更");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        throw new BusinessException("库存记录不可删除");
    }
}
