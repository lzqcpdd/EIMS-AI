package com.eims.common.service;

import com.eims.common.exception.BusinessException;
import com.eims.common.mapper.ReferentialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReferentialGuardService {

    private final ReferentialMapper referentialMapper;

    public void ensureCustomerDeletable(Long id) {
        if (referentialMapper.countSalesByCustomer(id) > 0 || referentialMapper.countOrdersByCustomer(id) > 0) {
            throw new BusinessException("该客户已被销售业务引用，无法删除");
        }
    }

    public void ensureSupplierDeletable(Long id) {
        if (referentialMapper.countQuotesBySupplier(id) > 0 || referentialMapper.countOrdersBySupplier(id) > 0) {
            throw new BusinessException("该供应商已被采购业务引用，无法删除");
        }
    }

    public void ensureMaterialDeletable(Long id) {
        if (referentialMapper.countInventoryByMaterial(id) > 0
                || referentialMapper.countPurchaseItemsByMaterial(id) > 0
                || referentialMapper.countSalesItemsByMaterial(id) > 0) {
            throw new BusinessException("该物料已被库存或业务单据引用，无法删除");
        }
    }

    public void ensureWarehouseDeletable(Long id) {
        if (referentialMapper.countInventoryByWarehouse(id) > 0
                || referentialMapper.countStocktakeByWarehouse(id) > 0
                || referentialMapper.countDeliveryByWarehouse(id) > 0) {
            throw new BusinessException("该仓库已被库存或出入库业务引用，无法删除");
        }
    }

    public void ensureDeptDeletable(Long id) {
        if (referentialMapper.countChildDept(id) > 0) {
            throw new BusinessException("请先删除或迁移下级部门");
        }
        if (referentialMapper.countUsersByDept(id) > 0) {
            throw new BusinessException("该部门下仍有用户，无法删除");
        }
    }

    public void ensureUserDeletable(Long id) {
        if (referentialMapper.countLeadsByUser(id) > 0
                || referentialMapper.countRequestsByUser(id) > 0
                || referentialMapper.countWarehousesByManager(id) > 0) {
            throw new BusinessException("该用户仍被业务数据引用，无法删除");
        }
    }

    public void ensureRoleDeletable(Long id) {
        if (referentialMapper.countUsersByRole(id) > 0) {
            throw new BusinessException("该角色仍有关联用户，无法删除");
        }
    }

    public void ensureCodeRuleDeletable(String ruleCode) {
        if (ruleCode == null) return;
        if (ruleCode.startsWith("SAL_") || ruleCode.startsWith("PUR_")
                || "STK_REC".equals(ruleCode) || "STK_TAKE".equals(ruleCode) || "DELIVERY".equals(ruleCode)) {
            throw new BusinessException("系统内置编码规则不可删除");
        }
    }
}
