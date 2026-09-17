package com.eims.module.sales.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.sales.service.SalesService;
import com.eims.module.sales.vo.SalesAfterServiceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/after-services")
@RequiredArgsConstructor
public class SalesAfterServiceController {

    private final SalesService salesService;

    @GetMapping
    public Result<PageResult<SalesAfterServiceVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(salesService.pageAfterServices(current, size, status));
    }
}
