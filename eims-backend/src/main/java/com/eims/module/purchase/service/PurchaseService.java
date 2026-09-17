package com.eims.module.purchase.service;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.eims.common.exception.BusinessException;

import com.eims.common.result.PageResult;

import com.eims.module.admin.entity.SysDept;
import com.eims.module.admin.entity.SysUser;
import com.eims.module.admin.mapper.SysDeptMapper;
import com.eims.module.admin.mapper.SysUserMapper;
import com.eims.module.base.entity.Material;
import com.eims.module.base.entity.Supplier;
import com.eims.module.base.mapper.MaterialMapper;
import com.eims.module.base.mapper.SupplierMapper;
import com.eims.module.base.service.CodeRuleService;

import com.eims.module.purchase.dto.PurchaseQuoteSaveDTO;
import com.eims.module.purchase.dto.PurchaseRequestItemDTO;
import com.eims.module.purchase.dto.PurchaseRequestSaveDTO;
import com.eims.module.purchase.entity.*;
import com.eims.module.purchase.mapper.*;
import com.eims.module.purchase.vo.PurchaseApprovalVO;
import com.eims.module.purchase.vo.PurchaseOrderVO;
import com.eims.module.purchase.vo.PurchaseQuoteVO;
import com.eims.module.purchase.vo.PurchaseRequestVO;

import com.eims.module.wms.service.WmsService;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;



import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;



@Service

@RequiredArgsConstructor

public class PurchaseService {



    private static final String[] STATUS_LABELS = {"草稿", "审批中", "审批通过", "已驳回", "已生成订单"};

    private static final String[] LEVEL_LABELS = {"", "部门负责人", "分管副总", "总经理"};



    private final PurchaseRequestMapper requestMapper;

    private final PurchaseRequestItemMapper itemMapper;

    private final PurchaseQuoteMapper quoteMapper;

    private final PurchaseOrderMapper orderMapper;

    private final PurchaseApprovalMapper approvalMapper;

    private final MaterialMapper materialMapper;

    private final SupplierMapper supplierMapper;

    private final SysUserMapper userMapper;

    private final SysDeptMapper deptMapper;

    private final CodeRuleService codeRuleService;

    private final WmsService wmsService;

    private final JdbcTemplate jdbcTemplate;



    public PageResult<PurchaseRequestVO> pageRequests(long current, long size, Integer status) {
        Page<PurchaseRequest> page = new Page<>(current, size);
        LambdaQueryWrapper<PurchaseRequest> qw = new LambdaQueryWrapper<PurchaseRequest>()
                .eq(PurchaseRequest::getIsDeleted, 0)
                .eq(status != null, PurchaseRequest::getStatus, status)
                .orderByDesc(PurchaseRequest::getCreateTime);
        requestMapper.selectPage(page, qw);
        List<PurchaseRequestVO> records = page.getRecords().stream()
                .map(this::toSummaryVO)
                .collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }



    public PurchaseRequestVO getDetail(Long id) {

        PurchaseRequest req = requestMapper.selectById(id);

        if (req == null || req.getIsDeleted() == 1) {

            throw new BusinessException("采购申请不存在");

        }

        PurchaseRequestVO vo = toSummaryVO(req);

        vo.setItems(loadItemDTOs(id));

        vo.setApprovals(loadApprovals(id));

        return vo;

    }



    @Transactional

    public PurchaseRequestVO saveRequest(PurchaseRequestSaveDTO dto, Long userId, Long deptId) {

        validateItems(dto.getItems());

        BigDecimal totalAmount = calcTotal(dto.getItems());



        PurchaseRequest entity;

        if (dto.getId() == null) {

            entity = new PurchaseRequest();

            entity.setRequestCode(codeRuleService.generateCode("PUR_REQ"));

            entity.setDeptId(deptId);

            entity.setApplicantId(userId);

            entity.setStatus(0);

            entity.setTotalAmount(totalAmount);

            entity.setRemark(dto.getRemark());

            requestMapper.insert(entity);

        } else {

            entity = requestMapper.selectById(dto.getId());

            if (entity == null) throw new BusinessException("采购申请不存在");

            if (entity.getStatus() != 0) throw new BusinessException("只有草稿状态可编辑");

            entity.setTotalAmount(totalAmount);

            entity.setRemark(dto.getRemark());

            requestMapper.updateById(entity);

            softDeleteItems(entity.getId());

        }



        saveItems(entity.getId(), dto.getItems(), userId);

        return getDetail(entity.getId());

    }



    @Transactional

    public void deleteRequest(Long id) {

        PurchaseRequest req = requestMapper.selectById(id);

        if (req == null) throw new BusinessException("采购申请不存在");

        if (req.getStatus() != 0) throw new BusinessException("只有草稿状态可删除");

        requestMapper.deleteById(id);

    }



    @Transactional

    public PurchaseRequestVO submitRequest(Long id) {

        PurchaseRequest req = requestMapper.selectById(id);

        if (req == null) throw new BusinessException("采购申请不存在");

        if (req.getStatus() != 0) throw new BusinessException("只有草稿状态可提交");



        List<PurchaseRequestItem> items = itemMapper.selectList(new LambdaQueryWrapper<PurchaseRequestItem>()

                .eq(PurchaseRequestItem::getRequestId, id)

                .eq(PurchaseRequestItem::getIsDeleted, 0));

        if (items.isEmpty()) throw new BusinessException("请至少添加一条采购物料明细");

        if (req.getTotalAmount() == null || req.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException("采购总金额必须大于0");

        }



        req.setStatus(1);

        req.setSubmitTime(LocalDateTime.now());

        requestMapper.updateById(req);

        return getDetail(id);

    }



    @Transactional

    public PurchaseApproval approve(Long requestId, Long approverId, Integer result, String comment) {

        PurchaseRequest req = requestMapper.selectById(requestId);

        if (req == null) throw new BusinessException("采购申请不存在");

        if (req.getStatus() != 1) throw new BusinessException("申请不在审批中");



        int maxRequired = getMaxRequiredLevel(req.getTotalAmount());

        int nextLevel = getNextApprovalLevel(requestId);

        if (nextLevel > maxRequired) throw new BusinessException("该申请已完成全部审批层级");



        PurchaseApproval approval = new PurchaseApproval();

        approval.setRequestId(requestId);

        approval.setApproverId(approverId);

        approval.setApprovalLevel(nextLevel);

        approval.setResult(result);

        approval.setComment(StringUtils.hasText(comment) ? comment : (result == 1 ? "同意" : "驳回"));

        approval.setApprovalTime(LocalDateTime.now());

        approvalMapper.insert(approval);



        if (result == 2) {

            req.setStatus(3);

        } else if (nextLevel < maxRequired) {

            req.setStatus(1);

        } else {

            req.setStatus(2);

        }

        requestMapper.updateById(req);

        return approval;

    }



    public PurchaseOrder createOrderFromQuoteId(Long quoteId) {
        if (quoteId == null) throw new BusinessException("请选择报价");
        PurchaseQuote quote = quoteMapper.selectById(quoteId);
        if (quote == null) throw new BusinessException("报价不存在");
        return createOrderFromQuote(quote.getRequestId(), quoteId);
    }

    @Transactional
    public PurchaseOrder createOrderFromQuote(Long requestId, Long quoteId) {
        if (requestId == null) throw new BusinessException("采购申请不能为空");
        if (quoteId == null) throw new BusinessException("请选择报价");
        PurchaseRequest req = requestMapper.selectById(requestId);
        if (req == null) throw new BusinessException("采购申请不存在");
        if (req.getStatus() != 2) throw new BusinessException("采购申请未审批通过，无法生成订单");

        Long orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM purchase_order WHERE request_id=? AND is_deleted=0", Long.class, requestId);
        if (orderCount != null && orderCount > 0) throw new BusinessException("该申请已生成采购订单");

        PurchaseQuote quote = quoteMapper.selectById(quoteId);
        if (quote == null) throw new BusinessException("报价不存在");
        if (!Objects.equals(quote.getRequestId(), requestId)) throw new BusinessException("报价与采购申请不匹配");

        jdbcTemplate.update("UPDATE purchase_quote SET is_selected=0 WHERE request_id=? AND is_deleted=0", requestId);
        quote.setIsSelected(1);
        quoteMapper.updateById(quote);

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderCode(codeRuleService.generateCode("PUR_ORD"));
        order.setRequestId(requestId);
        order.setSupplierId(quote.getSupplierId());
        order.setTotalAmount(quote.getQuoteAmount());
        order.setStatus(0);
        if (quote.getDeliveryDays() != null) {
            order.setExpectedArrivalTime(LocalDateTime.now().plusDays(quote.getDeliveryDays()));
        }
        orderMapper.insert(order);

        req.setStatus(4);
        requestMapper.updateById(req);
        return order;
    }

    public PageResult<PurchaseQuoteVO> pageQuotes(long current, long size, Long requestId) {
        Page<PurchaseQuote> page = new Page<>(current, size);
        LambdaQueryWrapper<PurchaseQuote> qw = new LambdaQueryWrapper<PurchaseQuote>()
                .eq(PurchaseQuote::getIsDeleted, 0)
                .eq(requestId != null, PurchaseQuote::getRequestId, requestId)
                .orderByDesc(PurchaseQuote::getCreateTime);
        quoteMapper.selectPage(page, qw);
        List<PurchaseQuoteVO> records = enrichQuotes(page.getRecords());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public List<PurchaseQuoteVO> listQuotesByRequest(Long requestId) {
        List<PurchaseQuote> quotes = quoteMapper.selectList(new LambdaQueryWrapper<PurchaseQuote>()
                .eq(PurchaseQuote::getRequestId, requestId)
                .eq(PurchaseQuote::getIsDeleted, 0)
                .orderByAsc(PurchaseQuote::getQuoteAmount));
        return enrichQuotes(quotes);
    }

    @Transactional
    public PurchaseQuoteVO saveQuote(PurchaseQuoteSaveDTO dto, Long userId) {
        if (dto.getRequestId() == null) throw new BusinessException("请选择采购申请");
        if (dto.getSupplierId() == null) throw new BusinessException("请选择供应商");
        if (dto.getQuoteAmount() == null || dto.getQuoteAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("报价金额必须大于0");
        }
        if (dto.getDeliveryDays() == null || dto.getDeliveryDays() <= 0) {
            throw new BusinessException("交期必须大于0天");
        }

        PurchaseRequest req = requestMapper.selectById(dto.getRequestId());
        if (req == null) throw new BusinessException("采购申请不存在");
        if (req.getStatus() != 2) throw new BusinessException("仅「审批通过」状态的申请可录入报价");

        Long dup = quoteMapper.selectCount(new LambdaQueryWrapper<PurchaseQuote>()
                .eq(PurchaseQuote::getRequestId, dto.getRequestId())
                .eq(PurchaseQuote::getSupplierId, dto.getSupplierId())
                .eq(PurchaseQuote::getIsDeleted, 0)
                .ne(dto.getId() != null, PurchaseQuote::getId, dto.getId()));
        if (dup != null && dup > 0) throw new BusinessException("该供应商已对此申请报价，请勿重复录入");

        PurchaseQuote entity;
        if (dto.getId() == null) {
            entity = new PurchaseQuote();
            entity.setRequestId(dto.getRequestId());
            entity.setSupplierId(dto.getSupplierId());
            entity.setIsSelected(0);
            entity.setCreateBy(userId);
        } else {
            entity = quoteMapper.selectById(dto.getId());
            if (entity == null) throw new BusinessException("报价记录不存在");
            if (entity.getIsSelected() != null && entity.getIsSelected() == 1) {
                throw new BusinessException("已选中的报价不可修改");
            }
        }
        entity.setQuoteAmount(dto.getQuoteAmount());
        entity.setDeliveryDays(dto.getDeliveryDays());
        entity.setQualityScore(dto.getQualityScore() != null ? dto.getQualityScore() : BigDecimal.ZERO);

        if (dto.getId() == null) quoteMapper.insert(entity);
        else quoteMapper.updateById(entity);

        return enrichQuotes(Collections.singletonList(entity)).get(0);
    }

    @Transactional
    public void deleteQuote(Long id) {
        PurchaseQuote quote = quoteMapper.selectById(id);
        if (quote == null) throw new BusinessException("报价记录不存在");
        if (quote.getIsSelected() != null && quote.getIsSelected() == 1) {
            throw new BusinessException("已选中的报价不可删除");
        }
        quoteMapper.deleteById(id);
    }

    public PageResult<PurchaseOrderVO> pageOrders(long current, long size) {
        Page<PurchaseOrder> page = new Page<>(current, size);
        orderMapper.selectPage(page, new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getIsDeleted, 0)
                .orderByDesc(PurchaseOrder::getCreateTime));
        List<PurchaseOrderVO> records = page.getRecords().stream()
                .map(o -> toOrderVO(o, false))
                .collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public PurchaseOrderVO getOrderDetail(Long id) {
        PurchaseOrder order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1) throw new BusinessException("采购订单不存在");
        return toOrderVO(order, true);
    }

    public byte[] exportOrderDetail(Long id) {
        PurchaseOrderVO vo = getOrderDetail(id);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff');
        sb.append("采购订单详情\n");
        sb.append("订单号,").append(vo.getOrderCode()).append('\n');
        sb.append("供应商,").append(nullToEmpty(vo.getSupplierName())).append('\n');
        sb.append("联系人,").append(nullToEmpty(vo.getSupplierContact())).append('\n');
        sb.append("联系电话,").append(nullToEmpty(vo.getSupplierPhone())).append('\n');
        sb.append("关联申请,").append(nullToEmpty(vo.getRequestCode())).append('\n');
        sb.append("申请人,").append(nullToEmpty(vo.getApplicantName())).append('\n');
        sb.append("申请部门,").append(nullToEmpty(vo.getDeptName())).append('\n');
        sb.append("订单金额,").append(vo.getTotalAmount()).append('\n');
        sb.append("订单状态,").append(vo.getStatusLabel()).append('\n');
        sb.append("预计到货,").append(vo.getExpectedArrivalTime() != null ? vo.getExpectedArrivalTime().format(dtf) : "").append('\n');
        sb.append("创建时间,").append(vo.getCreateTime() != null ? vo.getCreateTime().format(dtf) : "").append('\n');
        sb.append('\n');
        sb.append("物料编码,物料名称,数量,单价,小计\n");
        BigDecimal lineTotal = BigDecimal.ZERO;
        if (vo.getItems() != null) {
            for (PurchaseRequestItemDTO item : vo.getItems()) {
                BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                BigDecimal sub = unitPrice.multiply(BigDecimal.valueOf(qty));
                lineTotal = lineTotal.add(sub);
                sb.append(item.getMaterialCode()).append(',')
                        .append(item.getMaterialName()).append(',')
                        .append(item.getQuantity()).append(',')
                        .append(item.getUnitPrice()).append(',')
                        .append(sub).append('\n');
            }
        }
        sb.append("合计,,,,").append(lineTotal).append('\n');
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }



    @Transactional
    public void receiveGoods(Long orderId, Long warehouseId, Integer quantity, String materialCode) {
        PurchaseOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1) throw new BusinessException("采购订单不存在");
        if (order.getStatus() != null && (order.getStatus() == 3 || order.getStatus() == 4)) {
            throw new BusinessException("当前订单状态不可入库");
        }
        if (warehouseId == null) throw new BusinessException("请选择仓库");
        if (quantity == null || quantity <= 0) throw new BusinessException("入库数量必须大于0");
        if (!StringUtils.hasText(materialCode)) throw new BusinessException("请选择物料");

        Long warehouseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warehouse WHERE id=? AND is_deleted=0", Long.class, warehouseId);
        if (warehouseCount == null || warehouseCount == 0) throw new BusinessException("仓库不存在");

        List<PurchaseRequestItemDTO> items = loadItemDTOs(order.getRequestId());
        enrichItemsWithReceived(orderId, items);
        PurchaseRequestItemDTO matched = items.stream()
                .filter(i -> materialCode.equals(i.getMaterialCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("该物料不在订单明细中"));

        int received = matched.getReceivedQuantity() != null ? matched.getReceivedQuantity() : 0;
        int remaining = matched.getQuantity() - received;
        if (remaining <= 0) throw new BusinessException("该物料已全部入库");
        if (quantity > remaining) throw new BusinessException("入库数量不能超过待入库数量：" + remaining);

        WmsService.ScanRequest scan = new WmsService.ScanRequest();
        scan.setMaterialCode(materialCode);
        scan.setWarehouseId(warehouseId);
        scan.setQuantity(quantity);
        scan.setType("IN");
        scan.setBusinessType(1);
        scan.setBusinessId(orderId);
        scan.setRemark("采购订单入库 " + order.getOrderCode());
        wmsService.scanInOut(scan);

        enrichItemsWithReceived(orderId, items);
        boolean allReceived = items.stream().allMatch(i -> {
            int r = i.getReceivedQuantity() != null ? i.getReceivedQuantity() : 0;
            return r >= i.getQuantity();
        });
        order.setStatus(allReceived ? 2 : 1);
        orderMapper.updateById(order);
    }



    public List<PurchaseRequestItem> getItems(Long requestId) {

        return itemMapper.selectList(new LambdaQueryWrapper<PurchaseRequestItem>()

                .eq(PurchaseRequestItem::getRequestId, requestId)

                .eq(PurchaseRequestItem::getIsDeleted, 0));

    }



    public List<PurchaseApprovalVO> loadApprovals(Long requestId) {

        return jdbcTemplate.query(

                "SELECT pa.id, pa.approver_id AS approverId, COALESCE(u.nick_name, u.user_name) AS approverName, " +

                        "pa.approval_level AS approvalLevel, pa.result, pa.comment, pa.approval_time AS approvalTime " +

                        "FROM purchase_approval pa LEFT JOIN sys_user u ON pa.approver_id=u.id " +

                        "WHERE pa.request_id=? AND pa.is_deleted=0 ORDER BY pa.approval_time ASC",

                (rs, rowNum) -> {

                    PurchaseApprovalVO vo = new PurchaseApprovalVO();

                    vo.setId(rs.getLong("id"));

                    vo.setApproverId(rs.getLong("approverId"));

                    vo.setApproverName(rs.getString("approverName"));

                    int level = rs.getInt("approvalLevel");

                    vo.setApprovalLevel(level);

                    vo.setApprovalLevelLabel(levelLabel(level));

                    int result = rs.getInt("result");

                    vo.setResult(result);

                    vo.setResultLabel(result == 1 ? "通过" : "驳回");

                    vo.setComment(rs.getString("comment"));

                    vo.setApprovalTime(rs.getTimestamp("approvalTime").toLocalDateTime());

                    return vo;

                }, requestId);

    }



    private PurchaseRequestVO toSummaryVO(PurchaseRequest req) {

        PurchaseRequestVO vo = new PurchaseRequestVO();

        vo.setId(req.getId());

        vo.setRequestCode(req.getRequestCode());

        vo.setDeptId(req.getDeptId());

        vo.setApplicantId(req.getApplicantId());

        vo.setTotalAmount(req.getTotalAmount());

        vo.setStatus(req.getStatus());

        vo.setStatusLabel(statusLabel(req.getStatus()));

        vo.setSubmitTime(req.getSubmitTime());

        vo.setCreateTime(req.getCreateTime());

        vo.setRemark(req.getRemark());



        try {
            vo.setApplicantName(jdbcTemplate.queryForObject(
                    "SELECT COALESCE(u.nick_name, u.user_name) FROM sys_user u WHERE u.id=?",
                    String.class, req.getApplicantId()));
        } catch (Exception ignored) {
        }



        if (req.getDeptId() != null) {

            try {

                vo.setDeptName(jdbcTemplate.queryForObject(

                        "SELECT dept_name FROM sys_dept WHERE id=?", String.class, req.getDeptId()));

            } catch (Exception ignored) {

            }

        }



        int maxRequired = getMaxRequiredLevel(req.getTotalAmount());

        vo.setRequiredApprovalLevel(maxRequired);

        int nextLevel = req.getStatus() == 1 ? getNextApprovalLevel(req.getId()) : 0;

        vo.setCurrentApprovalLevel(nextLevel);

        vo.setCurrentApprovalLevelLabel(nextLevel > 0 ? levelLabel(nextLevel) : "");

        vo.setStageLabel(buildStageLabel(req, maxRequired, nextLevel));

        return vo;

    }



    private String buildStageLabel(PurchaseRequest req, int maxRequired, int nextLevel) {

        switch (req.getStatus()) {

            case 0: return "草稿，待提交审批";

            case 1:

                return "等待" + levelLabel(nextLevel) + "审批（第" + nextLevel + "/" + maxRequired + "级）";

            case 2: return "审批已全部通过，可进行询价比价";

            case 3: return "审批已驳回，需修改后重新提交";

            case 4: return "已生成采购订单";

            default: return statusLabel(req.getStatus());

        }

    }



    private List<PurchaseRequestItemDTO> loadItemDTOs(Long requestId) {

        return getItems(requestId).stream().map(item -> {

            PurchaseRequestItemDTO dto = new PurchaseRequestItemDTO();

            dto.setId(item.getId());

            dto.setMaterialId(item.getMaterialId());

            dto.setMaterialCode(item.getMaterialCode());

            dto.setMaterialName(item.getMaterialName());

            dto.setQuantity(item.getQuantity());

            dto.setUnitPrice(item.getUnitPrice());

            return dto;

        }).collect(Collectors.toList());

    }



    private void validateItems(List<PurchaseRequestItemDTO> items) {

        if (items == null || items.isEmpty()) {

            throw new BusinessException("请至少添加一条采购物料明细");

        }

        for (int i = 0; i < items.size(); i++) {

            PurchaseRequestItemDTO item = items.get(i);

            int row = i + 1;

            if (item.getMaterialId() == null) throw new BusinessException("第" + row + "行：请选择物料");

            if (!StringUtils.hasText(item.getMaterialCode())) throw new BusinessException("第" + row + "行：物料编码不能为空");

            if (!StringUtils.hasText(item.getMaterialName())) throw new BusinessException("第" + row + "行：物料名称不能为空");

            if (item.getQuantity() == null || item.getQuantity() <= 0) throw new BusinessException("第" + row + "行：数量必须大于0");

            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {

                throw new BusinessException("第" + row + "行：单价必须大于0");

            }

        }

    }



    private BigDecimal calcTotal(List<PurchaseRequestItemDTO> items) {

        return items.stream()

                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))

                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }



    private void saveItems(Long requestId, List<PurchaseRequestItemDTO> items, Long userId) {

        for (PurchaseRequestItemDTO dto : items) {

            Material material = materialMapper.selectById(dto.getMaterialId());

            if (material == null) throw new BusinessException("物料不存在：" + dto.getMaterialCode());



            PurchaseRequestItem item = new PurchaseRequestItem();

            item.setRequestId(requestId);

            item.setMaterialId(material.getId());

            item.setMaterialCode(material.getMaterialCode());

            item.setMaterialName(material.getMaterialName());

            item.setQuantity(dto.getQuantity());

            item.setUnitPrice(dto.getUnitPrice());

            item.setCreateBy(userId);

            itemMapper.insert(item);

        }

    }



    private void softDeleteItems(Long requestId) {

        jdbcTemplate.update("UPDATE purchase_request_item SET is_deleted=1 WHERE request_id=?", requestId);

    }



    private int getMaxRequiredLevel(BigDecimal amount) {

        if (amount == null) return 1;

        if (amount.compareTo(new BigDecimal("500000")) >= 0) return 3;

        if (amount.compareTo(new BigDecimal("50000")) >= 0) return 2;

        return 1;

    }



    private int getNextApprovalLevel(Long requestId) {

        Integer maxPassed = jdbcTemplate.queryForObject(

                "SELECT COALESCE(MAX(approval_level), 0) FROM purchase_approval WHERE request_id=? AND result=1 AND is_deleted=0",

                Integer.class, requestId);

        return maxPassed + 1;

    }



    private String statusLabel(Integer status) {

        if (status == null || status < 0 || status >= STATUS_LABELS.length) return "未知";

        return STATUS_LABELS[status];

    }



    private String levelLabel(int level) {

        if (level <= 0 || level >= LEVEL_LABELS.length) return "未知";

        return LEVEL_LABELS[level];

    }

    private static final String[] ORDER_STATUS_LABELS = {"待入库", "部分入库", "全部入库", "已完成", "已取消"};

    private List<PurchaseQuoteVO> enrichQuotes(List<PurchaseQuote> quotes) {
        if (quotes == null || quotes.isEmpty()) return Collections.emptyList();
        BigDecimal minPrice = quotes.stream()
                .map(PurchaseQuote::getQuoteAmount)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        Integer minDays = quotes.stream()
                .map(PurchaseQuote::getDeliveryDays)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        return quotes.stream().map(q -> {
            PurchaseQuoteVO vo = new PurchaseQuoteVO();
            vo.setId(q.getId());
            vo.setRequestId(q.getRequestId());
            vo.setSupplierId(q.getSupplierId());
            vo.setQuoteAmount(q.getQuoteAmount());
            vo.setDeliveryDays(q.getDeliveryDays());
            vo.setQualityScore(q.getQualityScore());
            vo.setIsSelected(q.getIsSelected());
            vo.setSelectedLabel(q.getIsSelected() != null && q.getIsSelected() == 1 ? "已选中" : "—");
            vo.setCreateTime(q.getCreateTime());

            try {
                PurchaseRequest req = requestMapper.selectById(q.getRequestId());
                if (req != null) {
                    vo.setRequestCode(req.getRequestCode());
                    vo.setRequestAmount(req.getTotalAmount());
                    if (req.getTotalAmount() != null && q.getQuoteAmount() != null) {
                        vo.setAmountDiff(q.getQuoteAmount().subtract(req.getTotalAmount()));
                    }
                }
            } catch (Exception ignored) {
            }

            if (q.getSupplierId() != null) {
                Supplier supplier = supplierMapper.selectById(q.getSupplierId());
                if (supplier != null) {
                    vo.setSupplierName(supplier.getSupplierName());
                }
            }

            vo.setLowestPrice(minPrice != null && q.getQuoteAmount() != null
                    && q.getQuoteAmount().compareTo(minPrice) == 0);
            vo.setFastestDelivery(minDays != null && minDays.equals(q.getDeliveryDays()));
            return vo;
        }).collect(Collectors.toList());
    }

    private PurchaseOrderVO toOrderVO(PurchaseOrder order, boolean withItems) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setRequestId(order.getRequestId());
        vo.setSupplierId(order.getSupplierId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusLabel(orderStatusLabel(order.getStatus()));
        vo.setExpectedArrivalTime(order.getExpectedArrivalTime());
        vo.setCreateTime(order.getCreateTime());

        if (order.getSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(order.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getSupplierName());
                vo.setSupplierContact(supplier.getContactPerson());
                vo.setSupplierPhone(supplier.getContactPhone());
            }
        }

        PurchaseRequest req = requestMapper.selectById(order.getRequestId());
        if (req != null) {
            vo.setRequestCode(req.getRequestCode());
            if (req.getApplicantId() != null) {
                SysUser applicant = userMapper.selectById(req.getApplicantId());
                if (applicant != null) {
                    vo.setApplicantName(StringUtils.hasText(applicant.getNickName())
                            ? applicant.getNickName() : applicant.getUserName());
                }
            }
            if (req.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(req.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
        }

        if (withItems) {
            List<PurchaseRequestItemDTO> items = loadItemDTOs(order.getRequestId());
            enrichItemsWithReceived(order.getId(), items);
            vo.setItems(items);
        }
        return vo;
    }

    private void enrichItemsWithReceived(Long orderId, List<PurchaseRequestItemDTO> items) {
        if (items == null || items.isEmpty()) return;
        Map<Long, Integer> receivedByMaterial = jdbcTemplate.query(
                "SELECT material_id, COALESCE(SUM(quantity), 0) AS qty FROM stock_record " +
                        "WHERE business_id=? AND business_type=1 AND is_deleted=0 GROUP BY material_id",
                rs -> {
                    Map<Long, Integer> map = new java.util.HashMap<>();
                    while (rs.next()) {
                        map.put(rs.getLong("material_id"), rs.getInt("qty"));
                    }
                    return map;
                },
                orderId);
        for (PurchaseRequestItemDTO item : items) {
            int received = receivedByMaterial.getOrDefault(item.getMaterialId(), 0);
            item.setReceivedQuantity(received);
        }
    }

    private String orderStatusLabel(Integer status) {
        if (status == null || status < 0 || status >= ORDER_STATUS_LABELS.length) return "未知";
        return ORDER_STATUS_LABELS[status];
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

}


