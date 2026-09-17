package com.eims.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReferentialMapper {

    @Select("SELECT COUNT(*) FROM sales_opportunity WHERE customer_id = #{id} AND is_deleted = 0")
    long countSalesByCustomer(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sales_order WHERE customer_id = #{id} AND is_deleted = 0")
    long countOrdersByCustomer(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM purchase_quote WHERE supplier_id = #{id} AND is_deleted = 0")
    long countQuotesBySupplier(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM purchase_order WHERE supplier_id = #{id} AND is_deleted = 0")
    long countOrdersBySupplier(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM inventory WHERE material_id = #{id} AND is_deleted = 0")
    long countInventoryByMaterial(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM purchase_request_item WHERE material_id = #{id} AND is_deleted = 0")
    long countPurchaseItemsByMaterial(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sales_order_item WHERE material_id = #{id} AND is_deleted = 0")
    long countSalesItemsByMaterial(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM inventory WHERE warehouse_id = #{id} AND is_deleted = 0")
    long countInventoryByWarehouse(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM stocktake WHERE warehouse_id = #{id} AND is_deleted = 0")
    long countStocktakeByWarehouse(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sales_delivery WHERE warehouse_id = #{id} AND is_deleted = 0")
    long countDeliveryByWarehouse(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_dept WHERE parent_id = #{id} AND is_deleted = 0")
    long countChildDept(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_user WHERE dept_id = #{id} AND is_deleted = 0")
    long countUsersByDept(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sales_lead WHERE owner_id = #{id} AND is_deleted = 0")
    long countLeadsByUser(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM purchase_request WHERE applicant_id = #{id} AND is_deleted = 0")
    long countRequestsByUser(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM warehouse WHERE manager_id = #{id} AND is_deleted = 0")
    long countWarehousesByManager(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_user_role WHERE user_id = #{id}")
    long countRolesByUser(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_user_role WHERE role_id = #{id}")
    long countUsersByRole(@Param("id") Long id);
}
