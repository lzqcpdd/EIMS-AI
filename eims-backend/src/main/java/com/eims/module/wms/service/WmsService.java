package com.eims.module.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.module.base.entity.Material;
import com.eims.module.base.entity.Warehouse;
import com.eims.module.base.mapper.MaterialMapper;
import com.eims.module.base.mapper.WarehouseMapper;
import com.eims.module.base.service.CodeRuleService;
import com.eims.module.wms.entity.Inventory;
import com.eims.module.wms.entity.StockRecord;
import com.eims.module.wms.entity.Stocktake;
import com.eims.module.wms.mapper.InventoryMapper;
import com.eims.module.wms.mapper.StockRecordMapper;
import com.eims.module.wms.mapper.StocktakeMapper;
import com.eims.module.wms.vo.InventoryVO;
import com.eims.module.wms.vo.StockRecordVO;
import com.eims.module.wms.dto.StocktakeItemRequest;
import com.eims.module.wms.entity.StocktakeItem;
import com.eims.module.wms.mapper.StocktakeItemMapper;
import com.eims.module.wms.vo.StocktakeDetailVO;
import com.eims.module.wms.vo.StocktakeItemVO;
import com.eims.module.wms.vo.StocktakeVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WmsService {

    private static final String[] BUSINESS_TYPE_LABELS = {"", "采购入库", "销售出库", "盘点调整", "退货入库", "其他"};
    private static final String[] STOCKTAKE_STATUS_LABELS = {"进行中", "已完成", "已确认"};

    private final InventoryMapper inventoryMapper;
    private final StockRecordMapper stockRecordMapper;
    private final StocktakeMapper stocktakeMapper;
    private final StocktakeItemMapper stocktakeItemMapper;
    private final MaterialMapper materialMapper;
    private final WarehouseMapper warehouseMapper;
    private final CodeRuleService codeRuleService;

    public Inventory getStock(Long materialId, Long warehouseId) {
        return inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getMaterialId, materialId)
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getIsDeleted, 0));
    }

    public Inventory getStockByCode(String materialCode, Long warehouseId) {
        Material material = materialMapper.selectOne(new LambdaQueryWrapper<Material>()
                .eq(Material::getMaterialCode, materialCode)
                .eq(Material::getIsDeleted, 0));
        if (material == null) return null;
        return getStock(material.getId(), warehouseId);
    }

    public PageResult<InventoryVO> pageInventory(long current, long size, String keyword) {
        Page<Inventory> page = new Page<>(current, size);
        LambdaQueryWrapper<Inventory> qw = new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getIsDeleted, 0)
                .orderByDesc(Inventory::getUpdateTime);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(Inventory::getMaterialCode, keyword)
                    .or().like(Inventory::getMaterialName, keyword));
        }
        inventoryMapper.selectPage(page, qw);
        List<InventoryVO> records = page.getRecords().stream().map(this::toInventoryVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public PageResult<StockRecordVO> pageStockRecords(long current, long size, String keyword) {
        Page<StockRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<StockRecord> qw = new LambdaQueryWrapper<StockRecord>()
                .eq(StockRecord::getIsDeleted, 0)
                .orderByDesc(StockRecord::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            List<Long> materialIds = materialMapper.selectList(new LambdaQueryWrapper<Material>()
                            .eq(Material::getIsDeleted, 0)
                            .and(w -> w.like(Material::getMaterialCode, keyword)
                                    .or().like(Material::getMaterialName, keyword)))
                    .stream().map(Material::getId).collect(Collectors.toList());
            qw.and(w -> {
                w.like(StockRecord::getRecordCode, keyword);
                if (!materialIds.isEmpty()) {
                    w.or().in(StockRecord::getMaterialId, materialIds);
                }
            });
        }
        stockRecordMapper.selectPage(page, qw);
        List<StockRecordVO> records = page.getRecords().stream().map(this::toStockRecordVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    public PageResult<StocktakeVO> pageStocktake(long current, long size, String keyword) {
        Page<Stocktake> page = new Page<>(current, size);
        LambdaQueryWrapper<Stocktake> qw = new LambdaQueryWrapper<Stocktake>()
                .eq(Stocktake::getIsDeleted, 0)
                .orderByDesc(Stocktake::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            qw.like(Stocktake::getStocktakeCode, keyword);
        }
        stocktakeMapper.selectPage(page, qw);
        List<StocktakeVO> records = page.getRecords().stream().map(this::toStocktakeVO).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), current, size);
    }

    @Transactional
    public Stocktake createStocktake(Stocktake entity) {
        ensureWarehouseExists(entity.getWarehouseId());
        if (!StringUtils.hasText(entity.getStocktakeCode())) {
            entity.setStocktakeCode(codeRuleService.generateCode("STK_TAKE"));
        }
        if (entity.getStatus() == null) entity.setStatus(0);
        stocktakeMapper.insert(entity);
        return entity;
    }

    public StocktakeDetailVO getStocktakeDetail(Long id) {
        Stocktake stocktake = stocktakeMapper.selectById(id);
        if (stocktake == null || (stocktake.getIsDeleted() != null && stocktake.getIsDeleted() == 1)) {
            throw new BusinessException("盘点单不存在");
        }
        StocktakeDetailVO detail = new StocktakeDetailVO();
        detail.setId(stocktake.getId());
        detail.setStocktakeCode(stocktake.getStocktakeCode());
        detail.setWarehouseId(stocktake.getWarehouseId());
        detail.setWarehouseName(queryWarehouseName(stocktake.getWarehouseId()));
        detail.setStatus(stocktake.getStatus());
        detail.setStatusLabel(label(STOCKTAKE_STATUS_LABELS, stocktake.getStatus()));
        List<StocktakeItem> items = stocktakeItemMapper.selectList(new LambdaQueryWrapper<StocktakeItem>()
                .eq(StocktakeItem::getStocktakeId, id)
                .eq(StocktakeItem::getIsDeleted, 0)
                .orderByAsc(StocktakeItem::getId));
        detail.setItems(items.stream().map(this::toStocktakeItemVO).collect(Collectors.toList()));
        return detail;
    }

    @Transactional
    public int initStocktakeItems(Long stocktakeId) {
        Stocktake stocktake = requireEditableStocktake(stocktakeId);
        List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, stocktake.getWarehouseId())
                .eq(Inventory::getIsDeleted, 0));
        int added = 0;
        for (Inventory inv : inventories) {
            Long exists = stocktakeItemMapper.selectCount(new LambdaQueryWrapper<StocktakeItem>()
                    .eq(StocktakeItem::getStocktakeId, stocktakeId)
                    .eq(StocktakeItem::getMaterialId, inv.getMaterialId())
                    .eq(StocktakeItem::getIsDeleted, 0));
            if (exists != null && exists > 0) continue;
            StocktakeItem item = new StocktakeItem();
            item.setStocktakeId(stocktakeId);
            item.setMaterialId(inv.getMaterialId());
            int systemQty = inv.getQuantity() != null ? inv.getQuantity() : 0;
            item.setSystemQuantity(systemQty);
            item.setActualQuantity(systemQty);
            item.setDiffQuantity(0);
            stocktakeItemMapper.insert(item);
            added++;
        }
        return added;
    }

    @Transactional
    public StocktakeItemVO saveStocktakeItem(Long stocktakeId, StocktakeItemRequest request) {
        Stocktake stocktake = requireEditableStocktake(stocktakeId);
        if (request.getMaterialId() == null) {
            throw new BusinessException("请选择物料");
        }
        Material material = materialMapper.selectById(request.getMaterialId());
        if (material == null || (material.getIsDeleted() != null && material.getIsDeleted() == 1)) {
            throw new BusinessException("物料不存在");
        }
        int systemQty = querySystemQuantity(stocktake.getWarehouseId(), request.getMaterialId());
        int actualQty = request.getActualQuantity() != null ? request.getActualQuantity() : systemQty;
        if (actualQty < 0) throw new BusinessException("实盘数量不能小于0");

        StocktakeItem item = stocktakeItemMapper.selectOne(new LambdaQueryWrapper<StocktakeItem>()
                .eq(StocktakeItem::getStocktakeId, stocktakeId)
                .eq(StocktakeItem::getMaterialId, request.getMaterialId())
                .eq(StocktakeItem::getIsDeleted, 0));
        if (item == null) {
            item = new StocktakeItem();
            item.setStocktakeId(stocktakeId);
            item.setMaterialId(request.getMaterialId());
            item.setSystemQuantity(systemQty);
            item.setActualQuantity(actualQty);
            item.setDiffQuantity(actualQty - systemQty);
            stocktakeItemMapper.insert(item);
        } else {
            item.setSystemQuantity(systemQty);
            item.setActualQuantity(actualQty);
            item.setDiffQuantity(actualQty - systemQty);
            stocktakeItemMapper.updateById(item);
        }
        return toStocktakeItemVO(item);
    }

    @Transactional
    public void deleteStocktakeItem(Long itemId) {
        StocktakeItem item = stocktakeItemMapper.selectById(itemId);
        if (item == null) throw new BusinessException("盘点明细不存在");
        requireEditableStocktake(item.getStocktakeId());
        stocktakeItemMapper.deleteById(itemId);
    }

    @Transactional
    public Stocktake completeStocktake(Long stocktakeId) {
        Stocktake stocktake = requireEditableStocktake(stocktakeId);
        Long itemCount = stocktakeItemMapper.selectCount(new LambdaQueryWrapper<StocktakeItem>()
                .eq(StocktakeItem::getStocktakeId, stocktakeId)
                .eq(StocktakeItem::getIsDeleted, 0));
        if (itemCount == null || itemCount == 0) {
            throw new BusinessException("请先添加盘点明细");
        }
        stocktake.setStatus(1);
        stocktakeMapper.updateById(stocktake);
        return stocktake;
    }

    @Transactional
    public Stocktake confirmStocktake(Long stocktakeId) {
        Stocktake stocktake = stocktakeMapper.selectById(stocktakeId);
        if (stocktake == null || (stocktake.getIsDeleted() != null && stocktake.getIsDeleted() == 1)) {
            throw new BusinessException("盘点单不存在");
        }
        if (stocktake.getStatus() == null || stocktake.getStatus() != 1) {
            throw new BusinessException("请先完成盘点后再确认调整");
        }
        List<StocktakeItem> items = stocktakeItemMapper.selectList(new LambdaQueryWrapper<StocktakeItem>()
                .eq(StocktakeItem::getStocktakeId, stocktakeId)
                .eq(StocktakeItem::getIsDeleted, 0));
        for (StocktakeItem item : items) {
            int diff = item.getDiffQuantity() != null ? item.getDiffQuantity() : 0;
            if (diff != 0) {
                adjustInventory(stocktake.getWarehouseId(), item.getMaterialId(), diff, stocktakeId,
                        "盘点调整：" + stocktake.getStocktakeCode());
            }
        }
        stocktake.setStatus(2);
        stocktakeMapper.updateById(stocktake);
        return stocktake;
    }

    @Transactional
    public StockRecord scanInOut(ScanRequest req) {
        Material material = materialMapper.selectOne(new LambdaQueryWrapper<Material>()
                .eq(Material::getMaterialCode, req.getMaterialCode())
                .eq(Material::getIsDeleted, 0));
        if (material == null) {
            throw new BusinessException("物料不存在: " + req.getMaterialCode());
        }
        if (material.getStatus() != null && material.getStatus() == 0) {
            throw new BusinessException("物料已停用");
        }
        ensureWarehouseExists(req.getWarehouseId());

        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, req.getWarehouseId())
                .eq(Inventory::getMaterialId, material.getId())
                .eq(Inventory::getIsDeleted, 0));

        int before = inv != null ? inv.getQuantity() : 0;
        int after;
        int businessType;

        if ("IN".equalsIgnoreCase(req.getType())) {
            after = before + req.getQuantity();
            businessType = req.getBusinessType() != null ? req.getBusinessType() : 1;
            if (inv == null) {
                inv = new Inventory();
                inv.setWarehouseId(req.getWarehouseId());
                inv.setMaterialId(material.getId());
                inv.setMaterialCode(material.getMaterialCode());
                inv.setMaterialName(material.getMaterialName());
                inv.setQuantity(after);
                inv.setLockedQuantity(0);
                inv.setSafetyStock(material.getSafetyStock());
                inventoryMapper.insert(inv);
            } else {
                inv.setMaterialCode(material.getMaterialCode());
                inv.setMaterialName(material.getMaterialName());
                inv.setQuantity(after);
                inventoryMapper.updateById(inv);
            }
        } else {
            if (before < req.getQuantity()) {
                throw new BusinessException("库存不足，当前库存: " + before);
            }
            after = before - req.getQuantity();
            businessType = req.getBusinessType() != null ? req.getBusinessType() : 2;
            inv.setQuantity(after);
            inventoryMapper.updateById(inv);
        }

        StockRecord record = new StockRecord();
        record.setRecordCode(codeRuleService.generateCode("STK_REC"));
        record.setWarehouseId(req.getWarehouseId());
        record.setMaterialId(material.getId());
        record.setBusinessType(businessType);
        record.setQuantity(req.getQuantity());
        record.setBeforeQuantity(before);
        record.setAfterQuantity(after);
        record.setBusinessId(req.getBusinessId());
        record.setRemark(req.getRemark());
        stockRecordMapper.insert(record);
        return record;
    }

    @Transactional
    public StockRecord adjustInventory(Long warehouseId, Long materialId, int diffQuantity, Long businessId, String remark) {
        if (diffQuantity == 0) return null;
        Material material = materialMapper.selectById(materialId);
        if (material == null || (material.getIsDeleted() != null && material.getIsDeleted() == 1)) {
            throw new BusinessException("物料不存在");
        }
        ScanRequest req = new ScanRequest();
        req.setMaterialCode(material.getMaterialCode());
        req.setWarehouseId(warehouseId);
        req.setQuantity(Math.abs(diffQuantity));
        req.setType(diffQuantity > 0 ? "IN" : "OUT");
        req.setBusinessType(3);
        req.setBusinessId(businessId);
        req.setRemark(remark);
        return scanInOut(req);
    }

    public void syncMaterialSnapshot(Long materialId, String materialCode, String materialName, Integer safetyStock) {
        Inventory patch = new Inventory();
        patch.setMaterialCode(materialCode);
        patch.setMaterialName(materialName);
        if (safetyStock != null) patch.setSafetyStock(safetyStock);
        inventoryMapper.update(patch, new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getMaterialId, materialId)
                .eq(Inventory::getIsDeleted, 0));
    }

    private void ensureWarehouseExists(Long warehouseId) {
        if (warehouseId == null) {
            throw new BusinessException("请选择仓库");
        }
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null || (warehouse.getIsDeleted() != null && warehouse.getIsDeleted() == 1)) {
            throw new BusinessException("仓库不存在");
        }
    }

    private InventoryVO toInventoryVO(Inventory inv) {
        InventoryVO vo = new InventoryVO();
        vo.setId(inv.getId());
        vo.setWarehouseId(inv.getWarehouseId());
        vo.setWarehouseName(queryWarehouseName(inv.getWarehouseId()));
        vo.setMaterialId(inv.getMaterialId());
        vo.setMaterialCode(inv.getMaterialCode());
        vo.setMaterialName(inv.getMaterialName());
        vo.setQuantity(inv.getQuantity());
        vo.setLockedQuantity(inv.getLockedQuantity());
        vo.setSafetyStock(inv.getSafetyStock());
        int qty = inv.getQuantity() != null ? inv.getQuantity() : 0;
        int safety = inv.getSafetyStock() != null ? inv.getSafetyStock() : 0;
        vo.setLowStock(qty <= safety);
        vo.setUpdateTime(inv.getUpdateTime());
        return vo;
    }

    private StockRecordVO toStockRecordVO(StockRecord record) {
        StockRecordVO vo = new StockRecordVO();
        vo.setId(record.getId());
        vo.setRecordCode(record.getRecordCode());
        vo.setWarehouseId(record.getWarehouseId());
        vo.setWarehouseName(queryWarehouseName(record.getWarehouseId()));
        vo.setMaterialId(record.getMaterialId());
        Material material = materialMapper.selectById(record.getMaterialId());
        if (material != null) {
            vo.setMaterialCode(material.getMaterialCode());
            vo.setMaterialName(material.getMaterialName());
        }
        vo.setBusinessType(record.getBusinessType());
        vo.setBusinessTypeLabel(label(BUSINESS_TYPE_LABELS, record.getBusinessType()));
        vo.setQuantity(record.getQuantity());
        vo.setBeforeQuantity(record.getBeforeQuantity());
        vo.setAfterQuantity(record.getAfterQuantity());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private StocktakeVO toStocktakeVO(Stocktake stocktake) {
        StocktakeVO vo = new StocktakeVO();
        vo.setId(stocktake.getId());
        vo.setStocktakeCode(stocktake.getStocktakeCode());
        vo.setWarehouseId(stocktake.getWarehouseId());
        vo.setWarehouseName(queryWarehouseName(stocktake.getWarehouseId()));
        vo.setStatus(stocktake.getStatus());
        vo.setStatusLabel(label(STOCKTAKE_STATUS_LABELS, stocktake.getStatus()));
        vo.setCreateTime(stocktake.getCreateTime());
        return vo;
    }

    private Stocktake requireEditableStocktake(Long stocktakeId) {
        Stocktake stocktake = stocktakeMapper.selectById(stocktakeId);
        if (stocktake == null || (stocktake.getIsDeleted() != null && stocktake.getIsDeleted() == 1)) {
            throw new BusinessException("盘点单不存在");
        }
        if (stocktake.getStatus() != null && stocktake.getStatus() >= 2) {
            throw new BusinessException("盘点单已确认，不能再修改");
        }
        return stocktake;
    }

    private int querySystemQuantity(Long warehouseId, Long materialId) {
        Inventory inv = getStock(materialId, warehouseId);
        return inv != null && inv.getQuantity() != null ? inv.getQuantity() : 0;
    }

    private StocktakeItemVO toStocktakeItemVO(StocktakeItem item) {
        StocktakeItemVO vo = new StocktakeItemVO();
        vo.setId(item.getId());
        vo.setStocktakeId(item.getStocktakeId());
        vo.setMaterialId(item.getMaterialId());
        Material material = materialMapper.selectById(item.getMaterialId());
        if (material != null) {
            vo.setMaterialCode(material.getMaterialCode());
            vo.setMaterialName(material.getMaterialName());
        }
        vo.setSystemQuantity(item.getSystemQuantity());
        vo.setActualQuantity(item.getActualQuantity());
        vo.setDiffQuantity(item.getDiffQuantity());
        return vo;
    }

    private String queryWarehouseName(Long warehouseId) {
        if (warehouseId == null) return null;
        Warehouse warehouse = warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getId, warehouseId)
                .eq(Warehouse::getIsDeleted, 0));
        if (warehouse != null) {
            return warehouse.getWarehouseName();
        }
        return "仓库#" + warehouseId;
    }

    private String label(String[] labels, Integer value) {
        if (value == null || value < 0 || value >= labels.length) return "未知";
        return labels[value];
    }

    @Data
    public static class ScanRequest {
        private String materialCode;
        private Long warehouseId;
        private Integer quantity;
        private String type;
        private Integer businessType;
        private Long businessId;
        private String remark;
    }
}
