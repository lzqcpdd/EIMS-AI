package com.eims.module.sales.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.sales.service.SalesService;
import com.eims.module.sales.vo.SalesOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesService salesService;

    @GetMapping
    public Result<PageResult<SalesOrderVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(salesService.pageOrders(current, size, status));
    }

    @GetMapping("/{id}")
    public Result<SalesOrderVO> get(@PathVariable Long id) {
        return Result.ok(salesService.getOrderDetail(id));
    }
}
