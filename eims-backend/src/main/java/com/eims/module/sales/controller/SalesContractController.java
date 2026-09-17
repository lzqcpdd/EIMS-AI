package com.eims.module.sales.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.sales.service.SalesService;
import com.eims.module.sales.vo.SalesContractVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/contracts")
@RequiredArgsConstructor
public class SalesContractController {

    private final SalesService salesService;

    @GetMapping
    public Result<PageResult<SalesContractVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer signStatus) {
        return Result.ok(salesService.pageContracts(current, size, signStatus));
    }
}
