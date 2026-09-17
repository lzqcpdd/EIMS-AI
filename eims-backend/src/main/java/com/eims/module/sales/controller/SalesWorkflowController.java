package com.eims.module.sales.controller;

import com.eims.common.result.PageResult;
import com.eims.common.exception.BusinessException;
import com.eims.common.result.Result;
import com.eims.module.sales.dto.SalesLeadSaveDTO;
import com.eims.module.sales.dto.SalesQuotationSaveDTO;
import com.eims.module.sales.service.SalesService;
import com.eims.module.sales.vo.*;
import com.eims.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sales/workflow")
@RequiredArgsConstructor
public class SalesWorkflowController {

    private final SalesService salesService;

    @GetMapping("/opportunity/{id}")
    public Result<SalesOpportunityVO> opportunityDetail(@PathVariable Long id) {
        return Result.ok(salesService.getOpportunityDetail(id));
    }

    @GetMapping("/opportunity-by-order/{orderId}")
    public Result<SalesOpportunityVO> opportunityByOrder(@PathVariable Long orderId) {
        Long oppId = salesService.resolveOpportunityIdByOrder(orderId);
        if (oppId == null) throw new BusinessException("未找到关联商机");
        return Result.ok(salesService.getOpportunityDetail(oppId));
    }

    @PostMapping("/follow-lead/{id}")
    public Result<Void> followLead(@PathVariable Long id) {
        salesService.followLead(id);
        return Result.ok();
    }

    @PostMapping("/invalidate-lead/{id}")
    public Result<Void> invalidateLead(@PathVariable Long id) {
        salesService.invalidateLead(id);
        return Result.ok();
    }

    @PostMapping("/convert-lead")
    public Result<SalesOpportunityVO> convertLead(@RequestBody Map<String, Long> body) {
        return Result.ok(salesService.convertLead(body.get("leadId"), body.get("customerId")));
    }

    @PostMapping("/submit-quotation/{id}")
    public Result<Void> submitQuotation(@PathVariable Long id) {
        salesService.submitQuotation(id);
        return Result.ok();
    }

    @PostMapping("/confirm-quotation/{id}")
    public Result<SalesContractVO> confirmQuotation(@PathVariable Long id) {
        return Result.ok(salesService.confirmQuotation(id));
    }

    @PostMapping("/sign-contract/{id}")
    public Result<SalesOrderVO> signContract(@PathVariable Long id) {
        return Result.ok(salesService.signContract(id));
    }

    @PostMapping("/deliver")
    public Result<SalesDeliveryVO> deliver(@RequestBody DeliverRequest req) {
        return Result.ok(salesService.deliver(
                req.getOrderId(), req.getWarehouseId(), req.getMaterialCode(),
                req.getQuantity(), req.getLogisticsNo()));
    }

    @PostMapping("/sign-delivery/{id}")
    public Result<Void> signDelivery(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String logisticsNo = body != null ? body.get("logisticsNo") : null;
        salesService.signDelivery(id, logisticsNo);
        return Result.ok();
    }

    @PostMapping("/after-service")
    public Result<SalesAfterServiceVO> createAfterService(@RequestBody AfterServiceRequest req) {
        return Result.ok(salesService.createAfterService(req.getOrderId(), req.getServiceType(), req.getContent()));
    }

    @PostMapping("/complete-service/{id}")
    public Result<Void> completeAfterService(@PathVariable Long id) {
        salesService.completeAfterService(id);
        return Result.ok();
    }

    @Data
    static class DeliverRequest {
        private Long orderId;
        private Long warehouseId;
        private String materialCode;
        private Integer quantity;
        private String logisticsNo;
    }

    @Data
    static class AfterServiceRequest {
        private Long orderId;
        private Integer serviceType;
        private String content;
    }
}
