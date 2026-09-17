package com.eims.module.purchase.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.purchase.dto.PurchaseRequestSaveDTO;
import com.eims.module.purchase.service.PurchaseService;
import com.eims.module.purchase.vo.PurchaseRequestVO;
import com.eims.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase/requests")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final PurchaseService purchaseService;

    @GetMapping
    public Result<PageResult<PurchaseRequestVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(purchaseService.pageRequests(current, size, status));
    }

    @GetMapping("/{id}")
    public Result<PurchaseRequestVO> get(@PathVariable Long id) {
        return Result.ok(purchaseService.getDetail(id));
    }

    @PostMapping
    public Result<PurchaseRequestVO> create(@RequestBody PurchaseRequestSaveDTO dto) {
        LoginUser user = currentUser();
        return Result.ok(purchaseService.saveRequest(dto, user.getUserId(), user.getDeptId()));
    }

    @PutMapping("/{id}")
    public Result<PurchaseRequestVO> update(@PathVariable Long id, @RequestBody PurchaseRequestSaveDTO dto) {
        dto.setId(id);
        LoginUser user = currentUser();
        return Result.ok(purchaseService.saveRequest(dto, user.getUserId(), user.getDeptId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseService.deleteRequest(id);
        return Result.ok();
    }

    private LoginUser currentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
