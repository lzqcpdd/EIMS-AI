package com.eims.module.sales.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.sales.service.SalesService;
import com.eims.module.sales.vo.SalesDeliveryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/deliveries")
@RequiredArgsConstructor
public class SalesDeliveryController {

    private final SalesService salesService;

    @GetMapping
    public Result<PageResult<SalesDeliveryVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long orderId) {
        return Result.ok(salesService.pageDeliveries(current, size, orderId));
    }
}
