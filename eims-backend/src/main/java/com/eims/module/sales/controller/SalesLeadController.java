package com.eims.module.sales.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.sales.dto.SalesLeadSaveDTO;
import com.eims.module.sales.service.SalesService;
import com.eims.module.sales.vo.SalesLeadVO;
import com.eims.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/leads")
@RequiredArgsConstructor
public class SalesLeadController {

    private final SalesService salesService;

    @GetMapping
    public Result<PageResult<SalesLeadVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(salesService.pageLeads(current, size, status));
    }

    @GetMapping("/{id}")
    public Result<SalesLeadVO> get(@PathVariable Long id) {
        return Result.ok(salesService.getLeadDetail(id));
    }

    @PostMapping
    public Result<SalesLeadVO> create(@RequestBody SalesLeadSaveDTO dto) {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(salesService.saveLead(dto, user.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<SalesLeadVO> update(@PathVariable Long id, @RequestBody SalesLeadSaveDTO dto) {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        dto.setId(id);
        return Result.ok(salesService.saveLead(dto, user.getUserId()));
    }
}
