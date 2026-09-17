package com.eims.module.purchase.controller;



import com.eims.common.result.Result;

import com.eims.module.purchase.entity.*;

import com.eims.module.purchase.service.PurchaseService;

import com.eims.module.purchase.vo.PurchaseApprovalVO;

import com.eims.module.purchase.vo.PurchaseRequestVO;

import com.eims.security.LoginUser;

import lombok.Data;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;



import java.util.List;

import java.util.Map;



@RestController

@RequestMapping("/purchase/workflow")

@RequiredArgsConstructor

public class PurchaseWorkflowController {



    private final PurchaseService purchaseService;



    @GetMapping("/detail/{id}")

    public Result<PurchaseRequestVO> detail(@PathVariable Long id) {

        return Result.ok(purchaseService.getDetail(id));

    }



    @GetMapping("/approvals/{requestId}")

    public Result<List<PurchaseApprovalVO>> approvals(@PathVariable Long requestId) {

        return Result.ok(purchaseService.loadApprovals(requestId));

    }



    @PostMapping("/submit/{id}")

    public Result<PurchaseRequestVO> submit(@PathVariable Long id) {

        return Result.ok(purchaseService.submitRequest(id));

    }



    @PostMapping("/approve")

    public Result<Void> approve(@RequestBody ApproveRequest req) {

        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        purchaseService.approve(req.getRequestId(), user.getUserId(), req.getResult(), req.getComment());

        return Result.ok();

    }



    @PostMapping("/create-order")

    public Result<PurchaseOrder> createOrder(@RequestBody Map<String, Long> body) {
        Long quoteId = body.get("quoteId");
        if (quoteId != null) {
            return Result.ok(purchaseService.createOrderFromQuoteId(quoteId));
        }
        return Result.ok(purchaseService.createOrderFromQuote(body.get("requestId"), body.get("quoteId")));
    }



    @PostMapping("/receive")

    public Result<Void> receive(@RequestBody ReceiveRequest req) {

        purchaseService.receiveGoods(req.getOrderId(), req.getWarehouseId(), req.getQuantity(), req.getMaterialCode());

        return Result.ok();

    }



    @GetMapping("/items/{requestId}")

    public Result<List<PurchaseRequestItem>> items(@PathVariable Long requestId) {

        return Result.ok(purchaseService.getItems(requestId));

    }



    @Data

    static class ApproveRequest {

        private Long requestId;

        private Integer result;

        private String comment;

    }



    @Data

    static class ReceiveRequest {

        private Long orderId;

        private Long warehouseId;

        private Integer quantity;

        private String materialCode;

    }

}


