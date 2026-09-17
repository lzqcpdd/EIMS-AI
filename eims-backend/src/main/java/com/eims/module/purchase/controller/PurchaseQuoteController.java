package com.eims.module.purchase.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.purchase.dto.PurchaseQuoteSaveDTO;
import com.eims.module.purchase.service.PurchaseService;
import com.eims.module.purchase.vo.PurchaseQuoteVO;
import com.eims.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase/quotes")
@RequiredArgsConstructor
public class PurchaseQuoteController {

    private final PurchaseService purchaseService;

    @GetMapping
    public Result<PageResult<PurchaseQuoteVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long requestId) {
        return Result.ok(purchaseService.pageQuotes(current, size, requestId));
    }

    @GetMapping("/by-request/{requestId}")
    public Result<List<PurchaseQuoteVO>> byRequest(@PathVariable Long requestId) {
        return Result.ok(purchaseService.listQuotesByRequest(requestId));
    }

    @PostMapping
    public Result<PurchaseQuoteVO> create(@RequestBody PurchaseQuoteSaveDTO dto) {
        LoginUser user = currentUser();
        return Result.ok(purchaseService.saveQuote(dto, user.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<PurchaseQuoteVO> update(@PathVariable Long id, @RequestBody PurchaseQuoteSaveDTO dto) {
        dto.setId(id);
        LoginUser user = currentUser();
        return Result.ok(purchaseService.saveQuote(dto, user.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseService.deleteQuote(id);
        return Result.ok();
    }

    private LoginUser currentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
