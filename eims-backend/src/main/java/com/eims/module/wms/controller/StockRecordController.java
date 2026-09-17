package com.eims.module.wms.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.wms.entity.StockRecord;
import com.eims.module.wms.service.WmsService;
import com.eims.module.wms.vo.StockRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wms/stock-records")
@RequiredArgsConstructor
public class StockRecordController {

    private final WmsService wmsService;

    @GetMapping
    public Result<PageResult<StockRecordVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(wmsService.pageStockRecords(current, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<StockRecord> get(@PathVariable Long id) {
        throw new BusinessException("库存流水为只读审计数据");
    }

    @PostMapping
    public Result<StockRecord> create(@RequestBody StockRecord entity) {
        throw new BusinessException("库存流水由系统自动生成");
    }

    @PutMapping("/{id}")
    public Result<StockRecord> update(@PathVariable Long id, @RequestBody StockRecord entity) {
        throw new BusinessException("库存流水为只读审计数据");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        throw new BusinessException("库存流水不可删除");
    }
}
