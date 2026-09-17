package com.eims.module.sales.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.module.base.entity.Customer;
import com.eims.module.base.mapper.CustomerMapper;
import com.eims.module.base.service.CodeRuleService;
import com.eims.module.sales.dto.SalesLeadSaveDTO;
import com.eims.module.sales.dto.SalesQuotationSaveDTO;
import com.eims.module.sales.entity.*;
import com.eims.module.sales.mapper.*;
import com.eims.module.sales.vo.*;
import com.eims.module.wms.entity.Inventory;
import com.eims.module.wms.mapper.InventoryMapper;
import com.eims.module.wms.service.WmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesService {

    private static final String[] LEAD_STATUS_LABELS = {"待跟进", "跟进中", "已转商机", "无效线索"};
    private static final String[] OPP_STAGE_LABELS = {"", "初步接触", "报价阶段", "合同阶段", "已成交", "已失败"};
    private static final String[] QUOTE_STATUS_LABELS = {"草稿", "已提交", "已确认", "已失效"};
    private static final String[] CONTRACT_SIGN_LABELS = {"待签章", "已签章", "已作废"};
    private static final String[] ORDER_STATUS_LABELS = {"待确认", "待发货", "部分发货", "已完成", "已取消"};
    private static final String[] PAYMENT_STATUS_LABELS = {"未回款", "部分回款", "已回款"};
    private static final String[] DELIVERY_STATUS_LABELS = {"待出库", "已出库", "已签收"};
    private static final String[] SERVICE_TYPE_LABELS = {"", "客户回访", "退货处理", "投诉处理"};
    private static final String[] SERVICE_STATUS_LABELS = {"处理中", "已完成"};
    private static final String[] WORKFLOW_STEP_NAMES = {
            "线索跟进", "商机立项", "销售报价", "合同签章", "销售订单", "发货出库", "售后服务"
    };

    private final SalesLeadMapper leadMapper;
    private final SalesOpportunityMapper opportunityMapper;
    private final SalesQuotationMapper quotationMapper;
    private final SalesContractMapper contractMapper;
    private final SalesOrderMapper orderMapper;
    private final SalesOrderItemMapper orderItemMapper;
    private final SalesDeliveryMapper deliveryMapper;
    private final SalesAfterServiceMapper afterServiceMapper;
    private final CustomerMapper customerMapper;
    private final InventoryMapper inventoryMapper;
    private final CodeRuleService codeRuleService;
    private final WmsService wmsService;
    private final JdbcTemplate jdbcTemplate;

    // ========== Lead ==========

    public PageResult<SalesLeadVO> pageLeads(long current, long size, Integer status) {
        Page<SalesLead> page = new Page<>(current, size);
        leadMapper.selectPage(page, new LambdaQueryWrapper<SalesLead>()
                .eq(SalesLead::getIsDeleted, 0)
                .eq(status != null, SalesLead::getStatus, status)
                .orderByDesc(SalesLead::getCreateTime));
        List<SalesLeadVO> records = page.getRecords().stream().map(this::toLeadVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public SalesLeadVO getLeadDetail(Long id) {
        SalesLead lead = leadMapper.selectById(id);
        if (lead == null || lead.getIsDeleted() == 1) throw new BusinessException("线索不存在");
        return toLeadVO(lead);
    }

    @Transactional
    public SalesLeadVO saveLead(SalesLeadSaveDTO dto, Long userId) {
        if (!StringUtils.hasText(dto.getCustomerName())) throw new BusinessException("客户名称不能为空");
        SalesLead entity;
        if (dto.getId() == null) {
            entity = new SalesLead();
            entity.setLeadCode(codeRuleService.generateCode("SAL_LEAD"));
            entity.setStatus(0);
            entity.setOwnerId(userId);
            entity.setCreateBy(userId);
        } else {
            entity = leadMapper.selectById(dto.getId());
            if (entity == null) throw new BusinessException("线索不存在");
            if (entity.getStatus() != null && entity.getStatus() >= 2) {
                throw new BusinessException("已转商机的线索不可修改");
            }
        }
        entity.setCustomerName(dto.getCustomerName());
        entity.setSource(dto.getSource());
        entity.setLevel(dto.getLevel() != null ? dto.getLevel() : 1);
        if (dto.getId() == null) leadMapper.insert(entity);
        else leadMapper.updateById(entity);
        return toLeadVO(entity);
    }

    @Transactional
    public void followLead(Long id) {
        SalesLead lead = leadMapper.selectById(id);
        if (lead == null) throw new BusinessException("线索不存在");
        if (lead.getStatus() != null && lead.getStatus() >= 2) {
            throw new BusinessException("该线索已转商机");
        }
        lead.setStatus(1);
        leadMapper.updateById(lead);
    }

    @Transactional
    public void invalidateLead(Long id) {
        SalesLead lead = leadMapper.selectById(id);
        if (lead == null) throw new BusinessException("线索不存在");
        lead.setStatus(3);
        leadMapper.updateById(lead);
    }

    @Transactional
    public SalesOpportunityVO convertLead(Long leadId, Long customerId) {
        SalesLead lead = leadMapper.selectById(leadId);
        if (lead == null) throw new BusinessException("线索不存在");
        if (lead.getStatus() != null && lead.getStatus() == 2) throw new BusinessException("线索已转商机");
        if (customerId == null) throw new BusinessException("请选择客户");
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) throw new BusinessException("客户不存在");

        SalesOpportunity opp = new SalesOpportunity();
        opp.setOpportunityCode(codeRuleService.generateCode("SAL_OPP"));
        opp.setLeadId(leadId);
        opp.setCustomerId(customerId);
        opp.setStage(1);
        opp.setExpectedAmount(BigDecimal.ZERO);
        opp.setOpportunityLevel(lead.getLevel() != null ? lead.getLevel() : 1);
        opportunityMapper.insert(opp);

        lead.setStatus(2);
        leadMapper.updateById(lead);
        return getOpportunityDetail(opp.getId());
    }

    // ========== Opportunity ==========

    public PageResult<SalesOpportunityVO> pageOpportunities(long current, long size, Integer stage) {
        Page<SalesOpportunity> page = new Page<>(current, size);
        opportunityMapper.selectPage(page, new LambdaQueryWrapper<SalesOpportunity>()
                .eq(SalesOpportunity::getIsDeleted, 0)
                .eq(stage != null, SalesOpportunity::getStage, stage)
                .orderByDesc(SalesOpportunity::getCreateTime));
        List<SalesOpportunityVO> records = page.getRecords().stream()
                .map(o -> toOpportunitySummary(o))
                .collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public SalesOpportunityVO getOpportunityDetail(Long id) {
        SalesOpportunity opp = opportunityMapper.selectById(id);
        if (opp == null || opp.getIsDeleted() == 1) throw new BusinessException("商机不存在");
        SalesOpportunityVO vo = toOpportunitySummary(opp);
        vo.setQuotations(listQuotationsByOpportunity(id));
        vo.setContracts(listContractsByOpportunity(id));
        vo.setOrders(listOrdersByOpportunity(id));
        vo.setDeliveries(listDeliveriesByOpportunity(id));
        vo.setAfterServices(listAfterServicesByOpportunity(id));
        vo.setCurrentStep(computeCurrentStep(vo));
        vo.setCurrentStepName(stepName(vo.getCurrentStep()));
        vo.setStageLabel("当前位于【" + vo.getCurrentStepName() + "】— " + buildOppStageLabel(opp.getStage()));
        vo.setTimeline(buildWorkflowTimeline(opp, vo));
        return vo;
    }

    // ========== Quotation ==========

    public PageResult<SalesQuotationVO> pageQuotations(long current, long size, Long opportunityId) {
        Page<SalesQuotation> page = new Page<>(current, size);
        quotationMapper.selectPage(page, new LambdaQueryWrapper<SalesQuotation>()
                .eq(SalesQuotation::getIsDeleted, 0)
                .eq(opportunityId != null, SalesQuotation::getOpportunityId, opportunityId)
                .orderByDesc(SalesQuotation::getCreateTime));
        List<SalesQuotationVO> records = page.getRecords().stream().map(this::toQuotationVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public List<SalesQuotationVO> listQuotationsByOpportunity(Long opportunityId) {
        List<SalesQuotation> list = quotationMapper.selectList(new LambdaQueryWrapper<SalesQuotation>()
                .eq(SalesQuotation::getOpportunityId, opportunityId)
                .eq(SalesQuotation::getIsDeleted, 0)
                .orderByDesc(SalesQuotation::getCreateTime));
        return list.stream().map(this::toQuotationVO).collect(Collectors.toList());
    }

    @Transactional
    public SalesQuotationVO saveQuotation(SalesQuotationSaveDTO dto) {
        if (dto.getOpportunityId() == null) throw new BusinessException("请选择商机");
        if (dto.getTotalAmount() == null || dto.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("报价金额必须大于0");
        }
        SalesOpportunity opp = opportunityMapper.selectById(dto.getOpportunityId());
        if (opp == null) throw new BusinessException("商机不存在");
        if (opp.getStage() != null && opp.getStage() >= 4) {
            throw new BusinessException("已成交商机不可新增报价");
        }

        SalesQuotation entity;
        if (dto.getId() == null) {
            entity = new SalesQuotation();
            entity.setQuotationCode(codeRuleService.generateCode("SAL_QUO"));
            entity.setOpportunityId(dto.getOpportunityId());
            entity.setCustomerId(opp.getCustomerId());
            entity.setStatus(0);
        } else {
            entity = quotationMapper.selectById(dto.getId());
            if (entity == null) throw new BusinessException("报价单不存在");
            if (entity.getStatus() != null && entity.getStatus() >= 2) {
                throw new BusinessException("已确认的报价不可修改");
            }
        }
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setValidUntil(dto.getValidUntil() != null ? dto.getValidUntil() : LocalDateTime.now().plusDays(30));
        if (dto.getId() == null) quotationMapper.insert(entity);
        else quotationMapper.updateById(entity);

        if (opp.getStage() != null && opp.getStage() < 2) {
            opp.setStage(2);
            if (opp.getExpectedAmount() == null || opp.getExpectedAmount().compareTo(BigDecimal.ZERO) == 0) {
                opp.setExpectedAmount(dto.getTotalAmount());
            }
            opportunityMapper.updateById(opp);
        }
        return toQuotationVO(entity);
    }

    @Transactional
    public void submitQuotation(Long id) {
        SalesQuotation quote = quotationMapper.selectById(id);
        if (quote == null) throw new BusinessException("报价单不存在");
        quote.setStatus(1);
        quotationMapper.updateById(quote);
    }

    @Transactional
    public SalesContractVO confirmQuotation(Long id) {
        SalesQuotation quote = quotationMapper.selectById(id);
        if (quote == null) throw new BusinessException("报价单不存在");
        if (quote.getStatus() != null && quote.getStatus() == 2) {
            throw new BusinessException("报价已确认");
        }
        quote.setStatus(2);
        quotationMapper.updateById(quote);

        SalesOpportunity opp = opportunityMapper.selectById(quote.getOpportunityId());
        if (opp != null && opp.getStage() != null && opp.getStage() < 3) {
            opp.setStage(3);
            opp.setExpectedAmount(quote.getTotalAmount());
            opportunityMapper.updateById(opp);
        }

        Long contractCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sales_contract WHERE quotation_id=? AND is_deleted=0",
                Long.class, id);
        if (contractCount != null && contractCount > 0) {
            SalesContract existing = contractMapper.selectOne(new LambdaQueryWrapper<SalesContract>()
                    .eq(SalesContract::getQuotationId, id)
                    .eq(SalesContract::getIsDeleted, 0)
                    .last("LIMIT 1"));
            return toContractVO(existing);
        }

        SalesContract contract = new SalesContract();
        contract.setContractCode(codeRuleService.generateCode("SAL_CON"));
        contract.setCustomerId(quote.getCustomerId());
        contract.setQuotationId(id);
        contract.setTotalAmount(quote.getTotalAmount());
        contract.setSignStatus(0);
        contractMapper.insert(contract);
        return toContractVO(contract);
    }

    // ========== Contract ==========

    public PageResult<SalesContractVO> pageContracts(long current, long size, Integer signStatus) {
        Page<SalesContract> page = new Page<>(current, size);
        contractMapper.selectPage(page, new LambdaQueryWrapper<SalesContract>()
                .eq(SalesContract::getIsDeleted, 0)
                .eq(signStatus != null, SalesContract::getSignStatus, signStatus)
                .orderByDesc(SalesContract::getCreateTime));
        List<SalesContractVO> records = page.getRecords().stream().map(this::toContractVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public List<SalesContractVO> listContractsByOpportunity(Long opportunityId) {
        List<SalesQuotation> quotes = quotationMapper.selectList(new LambdaQueryWrapper<SalesQuotation>()
                .eq(SalesQuotation::getOpportunityId, opportunityId)
                .eq(SalesQuotation::getIsDeleted, 0));
        if (quotes.isEmpty()) return Collections.emptyList();
        List<Long> quoteIds = quotes.stream().map(SalesQuotation::getId).collect(Collectors.toList());
        return contractMapper.selectList(new LambdaQueryWrapper<SalesContract>()
                .in(SalesContract::getQuotationId, quoteIds)
                .eq(SalesContract::getIsDeleted, 0)
                .orderByDesc(SalesContract::getCreateTime))
                .stream().map(this::toContractVO).collect(Collectors.toList());
    }

    @Transactional
    public SalesOrderVO signContract(Long contractId) {
        SalesContract contract = contractMapper.selectById(contractId);
        if (contract == null) throw new BusinessException("合同不存在");
        if (contract.getSignStatus() != null && contract.getSignStatus() == 1) {
            throw new BusinessException("合同已签章");
        }
        contract.setSignStatus(1);
        contract.setSignTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        Long orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sales_order WHERE contract_id=? AND is_deleted=0",
                Long.class, contractId);
        if (orderCount != null && orderCount > 0) {
            throw new BusinessException("该合同已生成销售订单");
        }

        SalesOrder order = new SalesOrder();
        order.setOrderCode(codeRuleService.generateCode("SAL_ORD"));
        order.setContractId(contractId);
        order.setCustomerId(contract.getCustomerId());
        order.setTotalAmount(contract.getTotalAmount());
        order.setStatus(1);
        order.setPaymentStatus(0);
        orderMapper.insert(order);

        if (contract.getQuotationId() != null) {
            SalesQuotation quote = quotationMapper.selectById(contract.getQuotationId());
            if (quote != null) {
                SalesOpportunity opp = opportunityMapper.selectById(quote.getOpportunityId());
                if (opp != null) {
                    opp.setStage(4);
                    opportunityMapper.updateById(opp);
                }
            }
        }
        return getOrderDetail(order.getId());
    }

    // ========== Order ==========

    public PageResult<SalesOrderVO> pageOrders(long current, long size, Integer status) {
        Page<SalesOrder> page = new Page<>(current, size);
        orderMapper.selectPage(page, new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getIsDeleted, 0)
                .eq(status != null, SalesOrder::getStatus, status)
                .orderByDesc(SalesOrder::getCreateTime));
        List<SalesOrderVO> records = page.getRecords().stream()
                .map(o -> toOrderVO(o, false))
                .collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public SalesOrderVO getOrderDetail(Long id) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1) throw new BusinessException("销售订单不存在");
        return toOrderVO(order, true);
    }

    public List<SalesOrderVO> listOrdersByOpportunity(Long opportunityId) {
        List<SalesContractVO> contracts = listContractsByOpportunity(opportunityId);
        if (contracts.isEmpty()) return Collections.emptyList();
        List<Long> contractIds = contracts.stream().map(SalesContractVO::getId).collect(Collectors.toList());
        return orderMapper.selectList(new LambdaQueryWrapper<SalesOrder>()
                .in(SalesOrder::getContractId, contractIds)
                .eq(SalesOrder::getIsDeleted, 0)
                .orderByDesc(SalesOrder::getCreateTime))
                .stream().map(o -> toOrderVO(o, true))
                .collect(Collectors.toList());
    }

    public List<SalesDeliveryVO> listDeliveriesByOpportunity(Long opportunityId) {
        List<SalesOrderVO> orders = listOrdersByOpportunity(opportunityId);
        if (orders.isEmpty()) return Collections.emptyList();
        List<Long> orderIds = orders.stream().map(SalesOrderVO::getId).collect(Collectors.toList());
        return deliveryMapper.selectList(new LambdaQueryWrapper<SalesDelivery>()
                .in(SalesDelivery::getOrderId, orderIds)
                .eq(SalesDelivery::getIsDeleted, 0)
                .orderByDesc(SalesDelivery::getCreateTime))
                .stream().map(this::toDeliveryVO).collect(Collectors.toList());
    }

    public List<SalesAfterServiceVO> listAfterServicesByOpportunity(Long opportunityId) {
        List<SalesOrderVO> orders = listOrdersByOpportunity(opportunityId);
        if (orders.isEmpty()) return Collections.emptyList();
        List<Long> orderIds = orders.stream().map(SalesOrderVO::getId).collect(Collectors.toList());
        return afterServiceMapper.selectList(new LambdaQueryWrapper<SalesAfterService>()
                .in(SalesAfterService::getOrderId, orderIds)
                .eq(SalesAfterService::getIsDeleted, 0)
                .orderByDesc(SalesAfterService::getCreateTime))
                .stream().map(this::toAfterServiceVO).collect(Collectors.toList());
    }

    public Long resolveOpportunityIdByOrder(Long orderId) {
        SalesOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getContractId() == null) return null;
        SalesContract contract = contractMapper.selectById(order.getContractId());
        if (contract == null || contract.getQuotationId() == null) return null;
        SalesQuotation quote = quotationMapper.selectById(contract.getQuotationId());
        return quote != null ? quote.getOpportunityId() : null;
    }

    @Transactional
    public SalesDeliveryVO deliver(Long orderId, Long warehouseId, String materialCode, Integer quantity, String logisticsNo) {
        SalesOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) throw new BusinessException("销售订单不存在");
        if (order.getStatus() != null && (order.getStatus() == 3 || order.getStatus() == 4)) {
            throw new BusinessException("当前订单状态不可发货");
        }
        if (warehouseId == null) throw new BusinessException("请选择仓库");
        if (quantity == null || quantity <= 0) throw new BusinessException("发货数量必须大于0");
        if (!StringUtils.hasText(materialCode)) throw new BusinessException("请选择物料");

        SalesOrderItem item = orderItemMapper.selectOne(new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, orderId)
                .eq(SalesOrderItem::getMaterialCode, materialCode)
                .eq(SalesOrderItem::getIsDeleted, 0));
        if (item == null) throw new BusinessException("该物料不在订单明细中");

        int delivered = item.getDeliveredQuantity() != null ? item.getDeliveredQuantity() : 0;
        int remaining = item.getQuantity() - delivered;
        if (remaining <= 0) throw new BusinessException("该物料已全部发货");
        if (quantity > remaining) throw new BusinessException("发货数量不能超过待发货数量：" + remaining);

        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getMaterialId, item.getMaterialId())
                .eq(Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getIsDeleted, 0));
        if (inv == null || inv.getQuantity() < quantity) {
            throw new BusinessException("库存不足，无法发货");
        }

        WmsService.ScanRequest scan = new WmsService.ScanRequest();
        scan.setMaterialCode(materialCode);
        scan.setWarehouseId(warehouseId);
        scan.setQuantity(quantity);
        scan.setType("OUT");
        scan.setBusinessType(2);
        scan.setBusinessId(orderId);
        scan.setRemark("销售订单发货 " + order.getOrderCode());
        wmsService.scanInOut(scan);

        item.setDeliveredQuantity(delivered + quantity);
        orderItemMapper.updateById(item);

        SalesDelivery delivery = new SalesDelivery();
        delivery.setDeliveryCode(codeRuleService.generateCode("DELIVERY"));
        delivery.setOrderId(orderId);
        delivery.setWarehouseId(warehouseId);
        delivery.setStatus(1);
        delivery.setLogisticsNo(logisticsNo);
        deliveryMapper.insert(delivery);

        List<SalesOrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getOrderId, orderId)
                .eq(SalesOrderItem::getIsDeleted, 0));
        boolean allDelivered = items.stream().allMatch(i -> {
            int d = i.getDeliveredQuantity() != null ? i.getDeliveredQuantity() : 0;
            return d >= i.getQuantity();
        });
        order.setStatus(allDelivered ? 3 : 2);
        orderMapper.updateById(order);
        return toDeliveryVO(delivery);
    }

    // ========== Delivery ==========

    public PageResult<SalesDeliveryVO> pageDeliveries(long current, long size, Long orderId) {
        Page<SalesDelivery> page = new Page<>(current, size);
        deliveryMapper.selectPage(page, new LambdaQueryWrapper<SalesDelivery>()
                .eq(SalesDelivery::getIsDeleted, 0)
                .eq(orderId != null, SalesDelivery::getOrderId, orderId)
                .orderByDesc(SalesDelivery::getCreateTime));
        List<SalesDeliveryVO> records = page.getRecords().stream().map(this::toDeliveryVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    @Transactional
    public void signDelivery(Long id, String logisticsNo) {
        SalesDelivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) throw new BusinessException("发货单不存在");
        delivery.setStatus(2);
        if (StringUtils.hasText(logisticsNo)) delivery.setLogisticsNo(logisticsNo);
        deliveryMapper.updateById(delivery);
    }

    // ========== After Service ==========

    public PageResult<SalesAfterServiceVO> pageAfterServices(long current, long size, Integer status) {
        Page<SalesAfterService> page = new Page<>(current, size);
        afterServiceMapper.selectPage(page, new LambdaQueryWrapper<SalesAfterService>()
                .eq(SalesAfterService::getIsDeleted, 0)
                .eq(status != null, SalesAfterService::getStatus, status)
                .orderByDesc(SalesAfterService::getCreateTime));
        List<SalesAfterServiceVO> records = page.getRecords().stream().map(this::toAfterServiceVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    @Transactional
    public SalesAfterServiceVO createAfterService(Long orderId, Integer serviceType, String content) {
        SalesOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("销售订单不存在");
        SalesAfterService entity = new SalesAfterService();
        entity.setServiceCode(codeRuleService.generateCode("SAL_AS"));
        entity.setOrderId(orderId);
        entity.setCustomerId(order.getCustomerId());
        entity.setServiceType(serviceType != null ? serviceType : 1);
        entity.setStatus(0);
        entity.setContent(content);
        afterServiceMapper.insert(entity);
        return toAfterServiceVO(entity);
    }

    @Transactional
    public void completeAfterService(Long id) {
        SalesAfterService entity = afterServiceMapper.selectById(id);
        if (entity == null) throw new BusinessException("售后单不存在");
        entity.setStatus(1);
        afterServiceMapper.updateById(entity);
    }

    // ========== VO mappers ==========

    private SalesLeadVO toLeadVO(SalesLead lead) {
        SalesLeadVO vo = new SalesLeadVO();
        vo.setId(lead.getId());
        vo.setLeadCode(lead.getLeadCode());
        vo.setCustomerName(lead.getCustomerName());
        vo.setSource(lead.getSource());
        vo.setLevel(lead.getLevel());
        vo.setOwnerId(lead.getOwnerId());
        vo.setStatus(lead.getStatus());
        vo.setStatusLabel(label(LEAD_STATUS_LABELS, lead.getStatus()));
        vo.setStageLabel(buildLeadStageLabel(lead.getStatus()));
        vo.setCreateTime(lead.getCreateTime());
        vo.setOwnerName(queryUserName(lead.getOwnerId()));
        if (lead.getStatus() != null && lead.getStatus() == 2) {
            SalesOpportunity opp = opportunityMapper.selectOne(new LambdaQueryWrapper<SalesOpportunity>()
                    .eq(SalesOpportunity::getLeadId, lead.getId())
                    .eq(SalesOpportunity::getIsDeleted, 0)
                    .last("LIMIT 1"));
            if (opp != null) vo.setOpportunityId(opp.getId());
        }
        return vo;
    }

    private String buildLeadStageLabel(Integer status) {
        if (status == null) return "待跟进";
        switch (status) {
            case 0: return "待跟进，可标记跟进中或转商机";
            case 1: return "跟进中，确认意向后可转商机";
            case 2: return "已转商机，请前往商机管理继续";
            case 3: return "无效线索";
            default: return label(LEAD_STATUS_LABELS, status);
        }
    }

    private SalesOpportunityVO toOpportunitySummary(SalesOpportunity opp) {
        SalesOpportunityVO vo = new SalesOpportunityVO();
        vo.setId(opp.getId());
        vo.setOpportunityCode(opp.getOpportunityCode());
        vo.setLeadId(opp.getLeadId());
        vo.setCustomerId(opp.getCustomerId());
        vo.setOpportunityLevel(opp.getOpportunityLevel());
        vo.setExpectedAmount(opp.getExpectedAmount());
        vo.setStage(opp.getStage());
        vo.setStageLabel(buildOppStageLabel(opp.getStage()));
        vo.setCreateTime(opp.getCreateTime());
        vo.setCustomerName(queryCustomerName(opp.getCustomerId()));
        if (opp.getLeadId() != null) {
            SalesLead lead = leadMapper.selectById(opp.getLeadId());
            if (lead != null) {
                vo.setLeadCode(lead.getLeadCode());
                vo.setLeadCustomerName(lead.getCustomerName());
            }
        }
        return vo;
    }

    private String buildOppStageLabel(Integer stage) {
        if (stage == null || stage <= 0 || stage >= OPP_STAGE_LABELS.length) return "未知";
        String base = OPP_STAGE_LABELS[stage];
        switch (stage) {
            case 1: return base + "，可创建销售报价";
            case 2: return base + "，确认报价后生成合同";
            case 3: return base + "，签章后生成销售订单";
            case 4: return base + "，可进行发货与售后";
            case 5: return base;
            default: return base;
        }
    }

    private SalesQuotationVO toQuotationVO(SalesQuotation q) {
        SalesQuotationVO vo = new SalesQuotationVO();
        vo.setId(q.getId());
        vo.setQuotationCode(q.getQuotationCode());
        vo.setOpportunityId(q.getOpportunityId());
        vo.setCustomerId(q.getCustomerId());
        vo.setTotalAmount(q.getTotalAmount());
        vo.setValidUntil(q.getValidUntil());
        vo.setStatus(q.getStatus());
        vo.setStatusLabel(label(QUOTE_STATUS_LABELS, q.getStatus()));
        vo.setCreateTime(q.getCreateTime());
        vo.setCustomerName(queryCustomerName(q.getCustomerId()));
        if (q.getOpportunityId() != null) {
            SalesOpportunity opp = opportunityMapper.selectById(q.getOpportunityId());
            if (opp != null) vo.setOpportunityCode(opp.getOpportunityCode());
        }
        return vo;
    }

    private SalesContractVO toContractVO(SalesContract c) {
        SalesContractVO vo = new SalesContractVO();
        vo.setId(c.getId());
        vo.setContractCode(c.getContractCode());
        vo.setCustomerId(c.getCustomerId());
        vo.setQuotationId(c.getQuotationId());
        vo.setTotalAmount(c.getTotalAmount());
        vo.setSignStatus(c.getSignStatus());
        vo.setSignStatusLabel(label(CONTRACT_SIGN_LABELS, c.getSignStatus()));
        vo.setSignTime(c.getSignTime());
        vo.setCreateTime(c.getCreateTime());
        vo.setCustomerName(queryCustomerName(c.getCustomerId()));
        if (c.getQuotationId() != null) {
            SalesQuotation q = quotationMapper.selectById(c.getQuotationId());
            if (q != null) {
                vo.setQuotationCode(q.getQuotationCode());
                vo.setOpportunityId(q.getOpportunityId());
            }
        }
        return vo;
    }

    private SalesOrderVO toOrderVO(SalesOrder order, boolean withItems) {
        SalesOrderVO vo = new SalesOrderVO();
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setContractId(order.getContractId());
        vo.setCustomerId(order.getCustomerId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusLabel(label(ORDER_STATUS_LABELS, order.getStatus()));
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setPaymentStatusLabel(label(PAYMENT_STATUS_LABELS, order.getPaymentStatus()));
        vo.setCreateTime(order.getCreateTime());
        vo.setCustomerName(queryCustomerName(order.getCustomerId()));
        if (order.getContractId() != null) {
            SalesContract contract = contractMapper.selectById(order.getContractId());
            if (contract != null) {
                vo.setContractCode(contract.getContractCode());
                if (contract.getQuotationId() != null) {
                    SalesQuotation quote = quotationMapper.selectById(contract.getQuotationId());
                    if (quote != null) vo.setOpportunityId(quote.getOpportunityId());
                }
            }
        }
        if (withItems) {
            vo.setItems(orderItemMapper.selectList(new LambdaQueryWrapper<SalesOrderItem>()
                    .eq(SalesOrderItem::getOrderId, order.getId())
                    .eq(SalesOrderItem::getIsDeleted, 0))
                    .stream().map(this::toOrderItemVO).collect(Collectors.toList()));
        }
        return vo;
    }

    private SalesOrderItemVO toOrderItemVO(SalesOrderItem item) {
        SalesOrderItemVO vo = new SalesOrderItemVO();
        vo.setId(item.getId());
        vo.setMaterialId(item.getMaterialId());
        vo.setMaterialCode(item.getMaterialCode());
        vo.setMaterialName(item.getMaterialName());
        vo.setQuantity(item.getQuantity());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setDeliveredQuantity(item.getDeliveredQuantity() != null ? item.getDeliveredQuantity() : 0);
        return vo;
    }

    private SalesDeliveryVO toDeliveryVO(SalesDelivery d) {
        SalesDeliveryVO vo = new SalesDeliveryVO();
        vo.setId(d.getId());
        vo.setDeliveryCode(d.getDeliveryCode());
        vo.setOrderId(d.getOrderId());
        vo.setWarehouseId(d.getWarehouseId());
        vo.setStatus(d.getStatus());
        vo.setStatusLabel(label(DELIVERY_STATUS_LABELS, d.getStatus()));
        vo.setLogisticsNo(d.getLogisticsNo());
        vo.setCreateTime(d.getCreateTime());
        if (d.getOrderId() != null) {
            SalesOrder order = orderMapper.selectById(d.getOrderId());
            if (order != null) vo.setOrderCode(order.getOrderCode());
        }
        if (d.getWarehouseId() != null) {
            try {
                vo.setWarehouseName(jdbcTemplate.queryForObject(
                        "SELECT warehouse_name FROM warehouse WHERE id=?", String.class, d.getWarehouseId()));
            } catch (Exception ignored) {
            }
        }
        return vo;
    }

    private SalesAfterServiceVO toAfterServiceVO(SalesAfterService s) {
        SalesAfterServiceVO vo = new SalesAfterServiceVO();
        vo.setId(s.getId());
        vo.setServiceCode(s.getServiceCode());
        vo.setOrderId(s.getOrderId());
        vo.setCustomerId(s.getCustomerId());
        vo.setServiceType(s.getServiceType());
        vo.setServiceTypeLabel(label(SERVICE_TYPE_LABELS, s.getServiceType()));
        vo.setStatus(s.getStatus());
        vo.setStatusLabel(label(SERVICE_STATUS_LABELS, s.getStatus()));
        vo.setContent(s.getContent());
        vo.setCreateTime(s.getCreateTime());
        vo.setCustomerName(queryCustomerName(s.getCustomerId()));
        if (s.getOrderId() != null) {
            SalesOrder order = orderMapper.selectById(s.getOrderId());
            if (order != null) vo.setOrderCode(order.getOrderCode());
        }
        return vo;
    }

    private String queryCustomerName(Long customerId) {
        if (customerId == null) return null;
        Customer c = customerMapper.selectById(customerId);
        return c != null ? c.getCustomerName() : null;
    }

    private String queryUserName(Long userId) {
        if (userId == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COALESCE(nick_name, user_name) FROM sys_user WHERE id=?",
                    String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    private String label(String[] labels, Integer index) {
        if (index == null || index < 0 || index >= labels.length) return "未知";
        return labels[index];
    }

    private String stepName(Integer step) {
        if (step == null || step < 0 || step >= WORKFLOW_STEP_NAMES.length) return "未知";
        return WORKFLOW_STEP_NAMES[step];
    }

    private int computeCurrentStep(SalesOpportunityVO vo) {
        if (vo.getStage() != null && vo.getStage() == 5) return 0;

        if (vo.getAfterServices() != null) {
            boolean openService = vo.getAfterServices().stream()
                    .anyMatch(s -> s.getStatus() != null && s.getStatus() == 0);
            if (openService) return 6;
        }

        if (vo.getOrders() != null && !vo.getOrders().isEmpty()) {
            boolean allDone = vo.getOrders().stream()
                    .allMatch(o -> o.getStatus() != null && o.getStatus() == 3);
            boolean needDeliver = vo.getOrders().stream()
                    .anyMatch(o -> o.getStatus() != null && (o.getStatus() == 1 || o.getStatus() == 2));
            if (needDeliver) return 5;
            if (allDone) {
                if (vo.getAfterServices() != null && !vo.getAfterServices().isEmpty()) return 6;
                return 5;
            }
            return 4;
        }

        if (vo.getContracts() != null && !vo.getContracts().isEmpty()) {
            boolean unsigned = vo.getContracts().stream()
                    .anyMatch(c -> c.getSignStatus() != null && c.getSignStatus() == 0);
            return unsigned ? 3 : 4;
        }

        if (vo.getQuotations() != null && !vo.getQuotations().isEmpty()) return 2;
        if (vo.getLeadId() != null || (vo.getStage() != null && vo.getStage() >= 1)) return 1;
        return 0;
    }

    private List<SalesWorkflowEventVO> buildWorkflowTimeline(SalesOpportunity opp, SalesOpportunityVO vo) {
        List<SalesWorkflowEventVO> events = new ArrayList<>();

        if (opp.getLeadId() != null) {
            SalesLead lead = leadMapper.selectById(opp.getLeadId());
            if (lead != null) {
                events.add(event("线索跟进", "线索创建", lead.getLeadCode() + " — " + lead.getCustomerName(),
                        "info", lead.getCreateTime()));
                if (lead.getStatus() != null && lead.getStatus() >= 1) {
                    events.add(event("线索跟进", "开始跟进", "销售跟进中", "warning", lead.getUpdateTime()));
                }
            }
        }

        events.add(event("商机立项", "商机创建", vo.getOpportunityCode() + " — " + vo.getCustomerName(),
                "success", opp.getCreateTime()));

        if (vo.getQuotations() != null) {
            for (SalesQuotationVO q : vo.getQuotations()) {
                String type = q.getStatus() != null && q.getStatus() >= 2 ? "success" : "info";
                events.add(event("销售报价", "报价单 " + q.getQuotationCode(),
                        "金额 ¥" + q.getTotalAmount() + "，" + q.getStatusLabel(), type, q.getCreateTime()));
            }
        }

        if (vo.getContracts() != null) {
            for (SalesContractVO c : vo.getContracts()) {
                String type = c.getSignStatus() != null && c.getSignStatus() == 1 ? "success" : "warning";
                LocalDateTime time = c.getSignStatus() != null && c.getSignStatus() == 1
                        ? c.getSignTime() : c.getCreateTime();
                events.add(event("合同签章", "合同 " + c.getContractCode(),
                        c.getSignStatusLabel() + "，金额 ¥" + c.getTotalAmount(), type, time));
            }
        }

        if (vo.getOrders() != null) {
            for (SalesOrderVO o : vo.getOrders()) {
                events.add(event("销售订单", "订单 " + o.getOrderCode(),
                        o.getStatusLabel() + "，金额 ¥" + o.getTotalAmount(), "success", o.getCreateTime()));
            }
        }

        if (vo.getDeliveries() != null) {
            for (SalesDeliveryVO d : vo.getDeliveries()) {
                String type = d.getStatus() != null && d.getStatus() == 2 ? "success" : "warning";
                events.add(event("发货出库", "发货单 " + d.getDeliveryCode(),
                        d.getStatusLabel() + (d.getLogisticsNo() != null ? "，物流 " + d.getLogisticsNo() : ""),
                        type, d.getCreateTime()));
            }
        }

        if (vo.getAfterServices() != null) {
            for (SalesAfterServiceVO s : vo.getAfterServices()) {
                String type = s.getStatus() != null && s.getStatus() == 1 ? "success" : "warning";
                events.add(event("售后服务", s.getServiceCode(),
                        s.getServiceTypeLabel() + "：" + s.getContent(), type, s.getCreateTime()));
            }
        }

        events.sort(Comparator.comparing(SalesWorkflowEventVO::getEventTime,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return events;
    }

    private SalesWorkflowEventVO event(String step, String title, String desc, String type, LocalDateTime time) {
        SalesWorkflowEventVO e = new SalesWorkflowEventVO();
        e.setStepName(step);
        e.setTitle(title);
        e.setDescription(desc);
        e.setType(type);
        e.setEventTime(time);
        return e;
    }
}
