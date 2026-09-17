package com.eims.module.purchase.controller;

import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.purchase.service.PurchaseService;
import com.eims.module.purchase.vo.PurchaseOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/purchase/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseService purchaseService;

    @GetMapping
    public Result<PageResult<PurchaseOrderVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(purchaseService.pageOrders(current, size));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrderVO> get(@PathVariable Long id) {
        return Result.ok(purchaseService.getOrderDetail(id));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        PurchaseOrderVO detail = purchaseService.getOrderDetail(id);
        byte[] body = purchaseService.exportOrderDetail(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(detail.getOrderCode() + "_订单详情.csv", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(body);
    }
}
