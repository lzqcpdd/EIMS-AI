package com.eims.module.sales.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.sales.dto.SalesQuotationSaveDTO;
import com.eims.module.sales.service.SalesService;
import com.eims.module.sales.vo.SalesQuotationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/quotations")
@RequiredArgsConstructor
public class SalesQuotationController {

    private final SalesService salesService;

    @GetMapping
    public Result<PageResult<SalesQuotationVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long opportunityId) {
        return Result.ok(salesService.pageQuotations(current, size, opportunityId));
    }

    @PostMapping
    public Result<SalesQuotationVO> create(@RequestBody SalesQuotationSaveDTO dto) {
        return Result.ok(salesService.saveQuotation(dto));
    }

    @PutMapping("/{id}")
    public Result<SalesQuotationVO> update(@PathVariable Long id, @RequestBody SalesQuotationSaveDTO dto) {
        dto.setId(id);
        return Result.ok(salesService.saveQuotation(dto));
    }
}
