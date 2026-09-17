package com.eims.module.admin.controller;

import com.eims.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 基础 KPI
        data.put("supplierCount", count("SELECT COUNT(*) FROM supplier WHERE is_deleted=0"));
        data.put("customerCount", count("SELECT COUNT(*) FROM customer WHERE is_deleted=0"));
        data.put("materialCount", count("SELECT COUNT(*) FROM material WHERE is_deleted=0"));
        data.put("inventoryTotal", count("SELECT COALESCE(SUM(quantity),0) FROM inventory WHERE is_deleted=0"));

        // 业务 KPI
        data.put("purchasePending", count("SELECT COUNT(*) FROM purchase_request WHERE status=1 AND is_deleted=0"));
        data.put("purchaseOrderCount", count("SELECT COUNT(*) FROM purchase_order WHERE is_deleted=0"));
        data.put("salesOrderCount", count("SELECT COUNT(*) FROM sales_order WHERE is_deleted=0"));
        data.put("lowStockCount", count("SELECT COUNT(*) FROM inventory WHERE quantity <= safety_stock AND is_deleted=0"));
        data.put("deliveryPending", count("SELECT COUNT(*) FROM sales_delivery WHERE status=0 AND is_deleted=0"));
        data.put("opportunityCount", count("SELECT COUNT(*) FROM sales_opportunity WHERE stage NOT IN (4,5) AND is_deleted=0"));
        data.put("leadCount", count("SELECT COUNT(*) FROM sales_lead WHERE status IN (0,1) AND is_deleted=0"));
        data.put("stocktakeActive", count("SELECT COUNT(*) FROM stocktake WHERE status=0 AND is_deleted=0"));

        data.put("monthlySalesAmount", sum("SELECT COALESCE(SUM(total_amount),0) FROM sales_order " +
                "WHERE is_deleted=0 AND create_time >= DATE_FORMAT(NOW(),'%Y-%m-01')"));
        data.put("monthlyPurchaseAmount", sum("SELECT COALESCE(SUM(total_amount),0) FROM purchase_order " +
                "WHERE is_deleted=0 AND create_time >= DATE_FORMAT(NOW(),'%Y-%m-01')"));

        // 近7日库存变动
        data.put("stockTrend", jdbcTemplate.queryForList(
                "SELECT DATE_FORMAT(DATE(create_time), '%Y-%m-%d') as date, " +
                        "CAST(SUM(CASE WHEN business_type IN (1,4) THEN quantity ELSE -quantity END) AS SIGNED) as change_qty " +
                        "FROM stock_record WHERE is_deleted=0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                        "GROUP BY DATE_FORMAT(DATE(create_time), '%Y-%m-%d') ORDER BY date"));

        // 库存分类占比
        data.put("categoryStock", jdbcTemplate.queryForList(
                "SELECT m.category, CAST(SUM(i.quantity) AS SIGNED) as total FROM inventory i JOIN material m ON i.material_id=m.id " +
                        "WHERE i.is_deleted=0 GROUP BY m.category"));

        // 各仓库库存占比
        data.put("warehouseStock", jdbcTemplate.queryForList(
                "SELECT w.warehouse_name as name, CAST(SUM(i.quantity) AS SIGNED) as total " +
                        "FROM inventory i JOIN warehouse w ON i.warehouse_id=w.id " +
                        "WHERE i.is_deleted=0 GROUP BY w.id, w.warehouse_name"));

        // 近30日销售/采购金额趋势
        data.put("amountTrend", jdbcTemplate.queryForList(
                "SELECT DATE_FORMAT(d.date, '%Y-%m-%d') as date, " +
                        "COALESCE(s.sales_amount, 0) as sales_amount, " +
                        "COALESCE(p.purchase_amount, 0) as purchase_amount " +
                        "FROM (SELECT DATE_SUB(CURDATE(), INTERVAL seq DAY) as date " +
                        "      FROM (SELECT 0 seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 " +
                        "            UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 " +
                        "            UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 " +
                        "            UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 " +
                        "            UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 " +
                        "            UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29) t) d " +
                        "LEFT JOIN (SELECT DATE(create_time) as dt, SUM(total_amount) as sales_amount " +
                        "           FROM sales_order WHERE is_deleted=0 GROUP BY DATE(create_time)) s ON d.date=s.dt " +
                        "LEFT JOIN (SELECT DATE(create_time) as dt, SUM(total_amount) as purchase_amount " +
                        "           FROM purchase_order WHERE is_deleted=0 GROUP BY DATE(create_time)) p ON d.date=p.dt " +
                        "ORDER BY d.date"));

        // 销售漏斗
        data.put("salesFunnel", Arrays.asList(
                map("stage", "销售线索", "count", count("SELECT COUNT(*) FROM sales_lead WHERE is_deleted=0")),
                map("stage", "商机", "count", count("SELECT COUNT(*) FROM sales_opportunity WHERE is_deleted=0")),
                map("stage", "报价", "count", count("SELECT COUNT(*) FROM sales_quotation WHERE is_deleted=0")),
                map("stage", "合同", "count", count("SELECT COUNT(*) FROM sales_contract WHERE is_deleted=0")),
                map("stage", "订单", "count", count("SELECT COUNT(*) FROM sales_order WHERE is_deleted=0"))
        ));

        // 客户区域分布
        data.put("customerRegion", jdbcTemplate.queryForList(
                "SELECT COALESCE(region, '其他') as region, CAST(COUNT(*) AS SIGNED) as total " +
                        "FROM customer WHERE is_deleted=0 GROUP BY COALESCE(region, '其他')"));

        // 采购订单状态分布
        data.put("purchaseOrderStatus", jdbcTemplate.queryForList(
                "SELECT status, COUNT(*) as total FROM purchase_order WHERE is_deleted=0 GROUP BY status"));

        // 销售订单状态分布
        data.put("salesOrderStatus", jdbcTemplate.queryForList(
                "SELECT status, COUNT(*) as total FROM sales_order WHERE is_deleted=0 GROUP BY status"));

        // 低库存物料 TOP10
        data.put("lowStockItems", jdbcTemplate.queryForList(
                "SELECT material_name, quantity, safety_stock " +
                        "FROM inventory WHERE quantity <= safety_stock AND is_deleted=0 " +
                        "ORDER BY (safety_stock - quantity) DESC LIMIT 10"));

        // 最近操作日志
        data.put("recentLogs", jdbcTemplate.queryForList(
                "SELECT l.module_name, l.operation_type, u.nick_name as operator, l.operation_time " +
                        "FROM sys_operation_log l LEFT JOIN sys_user u ON l.user_id=u.id " +
                        "ORDER BY l.operation_time DESC LIMIT 8"));

        // 待处理事项
        data.put("pendingTasks", jdbcTemplate.queryForList(
                "SELECT '采购审批' as type, request_code as code, total_amount as amount, submit_time as time " +
                        "FROM purchase_request WHERE status=1 AND is_deleted=0 " +
                        "UNION ALL " +
                        "SELECT '待发货', order_code, total_amount, create_time " +
                        "FROM sales_order WHERE status IN (1,2) AND is_deleted=0 " +
                        "ORDER BY time DESC LIMIT 8"));

        return Result.ok(data);
    }

    private long count(String sql) {
        Long c = jdbcTemplate.queryForObject(sql, Long.class);
        return c != null ? c : 0;
    }

    private double sum(String sql) {
        Double s = jdbcTemplate.queryForObject(sql, Double.class);
        return s != null ? s : 0;
    }

    private Map<String, Object> map(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }
}
