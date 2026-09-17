package com.eims.module.wms.controller;

import com.eims.common.result.Result;
import com.eims.module.wms.entity.Inventory;
import com.eims.module.wms.entity.StockRecord;
import com.eims.module.wms.service.WmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wms")
@RequiredArgsConstructor
public class WmsController {

    private final WmsService wmsService;

    /** 必须在 /stock/{materialId} 之前，避免 by-code 被当成 materialId */
    @GetMapping("/stock/by-code")
    public Result<Inventory> getStockByCode(@RequestParam String materialCode,
                                            @RequestParam Long warehouseId) {
        return Result.ok(wmsService.getStockByCode(materialCode, warehouseId));
    }

    @GetMapping("/stock/{materialId:\\d+}")
    public Result<Inventory> getStock(@PathVariable Long materialId,
                                      @RequestParam(required = false) Long warehouseId) {
        return Result.ok(wmsService.getStock(materialId, warehouseId));
    }

    @PostMapping("/stock/increase")
    public Result<StockRecord> increase(@RequestBody WmsService.ScanRequest req) {
        req.setType("IN");
        return Result.ok(wmsService.scanInOut(req));
    }

    @PostMapping("/stock/deduct")
    public Result<StockRecord> deduct(@RequestBody WmsService.ScanRequest req) {
        req.setType("OUT");
        return Result.ok(wmsService.scanInOut(req));
    }

    @PostMapping("/scan")
    public Result<StockRecord> scan(@RequestBody WmsService.ScanRequest req) {
        return Result.ok(wmsService.scanInOut(req));
    }
}
