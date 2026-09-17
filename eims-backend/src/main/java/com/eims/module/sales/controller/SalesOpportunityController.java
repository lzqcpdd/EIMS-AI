package com.eims.module.sales.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.sales.service.SalesService;
import com.eims.module.sales.vo.SalesOpportunityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/opportunities")
@RequiredArgsConstructor
public class SalesOpportunityController {

    private final SalesService salesService;

    @GetMapping
    public Result<PageResult<SalesOpportunityVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer stage) {
        return Result.ok(salesService.pageOpportunities(current, size, stage));
    }

    @GetMapping("/{id}")
    public Result<SalesOpportunityVO> get(@PathVariable Long id) {
        return Result.ok(salesService.getOpportunityDetail(id));
    }
}
