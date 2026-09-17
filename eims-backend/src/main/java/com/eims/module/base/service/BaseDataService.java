package com.eims.module.base.service;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.eims.common.result.PageResult;

import com.eims.common.service.ReferentialGuardService;

import com.eims.module.base.entity.Customer;

import com.eims.module.base.entity.Material;

import com.eims.module.base.entity.Supplier;

import com.eims.module.base.entity.Warehouse;

import com.eims.module.base.mapper.CustomerMapper;

import com.eims.module.base.mapper.MaterialMapper;

import com.eims.module.base.mapper.SupplierMapper;

import com.eims.module.base.mapper.WarehouseMapper;

import com.eims.module.wms.service.WmsService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;



@Service

@RequiredArgsConstructor

public class BaseDataService {



    private final CustomerMapper customerMapper;

    private final SupplierMapper supplierMapper;

    private final MaterialMapper materialMapper;

    private final WarehouseMapper warehouseMapper;

    private final ReferentialGuardService referentialGuardService;

    private final WmsService wmsService;

    private final CodeRuleService codeRuleService;



    public PageResult<Customer> pageCustomers(long current, long size, String keyword) {

        Page<Customer> page = new Page<>(current, size);

        LambdaQueryWrapper<Customer> qw = new LambdaQueryWrapper<Customer>()

                .eq(Customer::getIsDeleted, 0)

                .orderByDesc(Customer::getCreateTime);

        if (StringUtils.hasText(keyword)) {

            qw.and(w -> w.like(Customer::getCustomerCode, keyword)

                    .or().like(Customer::getCustomerName, keyword)

                    .or().like(Customer::getContactPhone, keyword));

        }

        customerMapper.selectPage(page, qw);

        return new PageResult<>(page.getRecords(), page.getTotal(), current, size);

    }



    public PageResult<Supplier> pageSuppliers(long current, long size, String keyword) {

        Page<Supplier> page = new Page<>(current, size);

        LambdaQueryWrapper<Supplier> qw = new LambdaQueryWrapper<Supplier>()

                .eq(Supplier::getIsDeleted, 0)

                .orderByDesc(Supplier::getCreateTime);

        if (StringUtils.hasText(keyword)) {

            qw.and(w -> w.like(Supplier::getSupplierCode, keyword)

                    .or().like(Supplier::getSupplierName, keyword)

                    .or().like(Supplier::getContactPhone, keyword));

        }

        supplierMapper.selectPage(page, qw);

        return new PageResult<>(page.getRecords(), page.getTotal(), current, size);

    }



    public PageResult<Material> pageMaterials(long current, long size, String keyword) {

        Page<Material> page = new Page<>(current, size);

        LambdaQueryWrapper<Material> qw = new LambdaQueryWrapper<Material>()

                .eq(Material::getIsDeleted, 0)

                .orderByDesc(Material::getCreateTime);

        if (StringUtils.hasText(keyword)) {

            qw.and(w -> w.like(Material::getMaterialCode, keyword)

                    .or().like(Material::getMaterialName, keyword)

                    .or().like(Material::getCategory, keyword));

        }

        materialMapper.selectPage(page, qw);

        return new PageResult<>(page.getRecords(), page.getTotal(), current, size);

    }



    public PageResult<Warehouse> pageWarehouses(long current, long size, String keyword) {

        Page<Warehouse> page = new Page<>(current, size);

        LambdaQueryWrapper<Warehouse> qw = new LambdaQueryWrapper<Warehouse>()

                .eq(Warehouse::getIsDeleted, 0)

                .orderByDesc(Warehouse::getCreateTime);

        if (StringUtils.hasText(keyword)) {

            qw.and(w -> w.like(Warehouse::getWarehouseCode, keyword)

                    .or().like(Warehouse::getWarehouseName, keyword)

                    .or().like(Warehouse::getLocation, keyword));

        }

        warehouseMapper.selectPage(page, qw);

        return new PageResult<>(page.getRecords(), page.getTotal(), current, size);

    }



    public Customer createCustomer(Customer entity) {

        if (!StringUtils.hasText(entity.getCustomerCode())) {

            entity.setCustomerCode(codeRuleService.generateCode("CUSTOMER"));

        }

        if (entity.getLevel() == null) entity.setLevel(1);

        customerMapper.insert(entity);

        return entity;

    }



    public Supplier createSupplier(Supplier entity) {

        if (!StringUtils.hasText(entity.getSupplierCode())) {

            entity.setSupplierCode(codeRuleService.generateCode("SUPPLIER"));

        }

        supplierMapper.insert(entity);

        return entity;

    }



    public Material createMaterial(Material entity) {

        if (!StringUtils.hasText(entity.getMaterialCode())) {

            entity.setMaterialCode(codeRuleService.generateCode("MATERIAL"));

        }

        if (entity.getStatus() == null) entity.setStatus(1);

        materialMapper.insert(entity);

        return entity;

    }



    public Warehouse createWarehouse(Warehouse entity) {

        if (!StringUtils.hasText(entity.getWarehouseCode())) {

            entity.setWarehouseCode(codeRuleService.generateCode("WAREHOUSE"));

        }

        warehouseMapper.insert(entity);

        return entity;

    }



    public Material updateMaterial(Long id, Material entity) {

        entity.setId(id);

        materialMapper.updateById(entity);

        wmsService.syncMaterialSnapshot(id, entity.getMaterialCode(), entity.getMaterialName(), entity.getSafetyStock());

        return entity;

    }



    public void deleteCustomer(Long id) {

        referentialGuardService.ensureCustomerDeletable(id);

        customerMapper.deleteById(id);

    }



    public void deleteSupplier(Long id) {

        referentialGuardService.ensureSupplierDeletable(id);

        supplierMapper.deleteById(id);

    }



    public void deleteMaterial(Long id) {

        referentialGuardService.ensureMaterialDeletable(id);

        materialMapper.deleteById(id);

    }



    public void deleteWarehouse(Long id) {

        referentialGuardService.ensureWarehouseDeletable(id);

        warehouseMapper.deleteById(id);

    }

}

