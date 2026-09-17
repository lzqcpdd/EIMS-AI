package com.eims.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(String... args) {
        initPasswords();
        migrateCodeRuleTable();
        migrateMissingBaseEntityColumns();
        initMenuLabels();
        initCodeRuleLabels();
        initUserLabels();
        initBaseDataLabels();
        initRoleMenus();
        initDemoData();
    }

    private void migrateCodeRuleTable() {
        try {
            jdbcTemplate.execute("ALTER TABLE code_rule ADD COLUMN create_by BIGINT UNSIGNED NULL AFTER status");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE code_rule ADD COLUMN update_by BIGINT UNSIGNED NULL AFTER create_time");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE code_rule ADD COLUMN remark VARCHAR(500) NULL AFTER is_deleted");
        } catch (Exception ignored) {
        }
    }

    /** 补齐部分明细表缺失的 BaseEntity 字段 */
    private void migrateMissingBaseEntityColumns() {
        addColumn("purchase_approval", "create_by BIGINT UNSIGNED NULL", "approval_time");
        addColumn("purchase_approval", "create_time DATETIME DEFAULT CURRENT_TIMESTAMP", "create_by");
        addColumn("purchase_approval", "update_by BIGINT UNSIGNED NULL", "create_time");
        addColumn("purchase_approval", "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "update_by");
        addColumn("purchase_approval", "is_deleted TINYINT UNSIGNED DEFAULT 0", "update_time");
        addColumn("purchase_approval", "remark VARCHAR(500) NULL", "is_deleted");

        addColumn("stocktake_item", "create_by BIGINT UNSIGNED NULL", "diff_quantity");
        addColumn("stocktake_item", "create_time DATETIME DEFAULT CURRENT_TIMESTAMP", "create_by");
        addColumn("stocktake_item", "update_by BIGINT UNSIGNED NULL", "create_time");
        addColumn("stocktake_item", "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "update_by");
        addColumn("stocktake_item", "is_deleted TINYINT UNSIGNED DEFAULT 0", "update_time");
        addColumn("stocktake_item", "remark VARCHAR(500) NULL", "is_deleted");

        addColumn("purchase_request_item", "create_by BIGINT UNSIGNED NULL", "expected_date");
        addColumn("purchase_request_item", "remark VARCHAR(500) NULL", "is_deleted");

        addColumn("sales_order_item", "create_by BIGINT UNSIGNED NULL", "delivered_quantity");
        addColumn("sales_order_item", "create_time DATETIME DEFAULT CURRENT_TIMESTAMP", "create_by");
        addColumn("sales_order_item", "update_by BIGINT UNSIGNED NULL", "create_time");
        addColumn("sales_order_item", "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "update_by");
        addColumn("sales_order_item", "is_deleted TINYINT UNSIGNED DEFAULT 0", "update_time");
        addColumn("sales_order_item", "remark VARCHAR(500) NULL", "is_deleted");
    }

    private void addColumn(String table, String columnDef, String afterColumn) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + columnDef + " AFTER " + afterColumn);
        } catch (Exception ignored) {
        }
    }

    private void initPasswords() {
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        if (!profiles.contains("dev") && !profiles.contains("prod")) {
            return;
        }
        String hash = passwordEncoder.encode("Admin@123456");
        jdbcTemplate.update(
                "UPDATE sys_user SET password=? WHERE user_name IN ('admin','purchaser','sales','warehouse')", hash);
    }

    private void initMenuLabels() {
        Object[][] menus = {
                {1L, "工作台", "Odometer"},
                {10L, "基础信息", "Collection"},
                {11L, "供应商管理", "User"},
                {12L, "客户管理", "Avatar"},
                {13L, "物料管理", "Box"},
                {14L, "仓库管理", "House"},
                {15L, "编码规则", "Key"},
                {20L, "仓储管理", "Goods"},
                {21L, "库存查询", "Search"},
                {22L, "扫码出入库", "Camera"},
                {23L, "库存流水", "List"},
                {24L, "盘点管理", "DocumentChecked"},
                {30L, "采购管理", "ShoppingCart"},
                {31L, "采购申请", "Edit"},
                {32L, "采购审批", "Checked"},
                {33L, "询价比价", "TrendCharts"},
                {34L, "采购订单", "Tickets"},
                {40L, "销售管理", "ShoppingCartFull"},
                {41L, "销售线索", "Link"},
                {42L, "商机管理", "TrendCharts"},
                {43L, "销售报价", "PriceTag"},
                {44L, "销售合同", "Document"},
                {45L, "销售订单", "ShoppingBag"},
                {46L, "发货跟踪", "Van"},
                {47L, "售后服务", "Headset"},
                {50L, "系统管理", "Setting"},
                {51L, "用户管理", "UserFilled"},
                {52L, "角色管理", "Stamp"},
                {53L, "部门管理", "OfficeBuilding"},
                {54L, "操作日志", "Notebook"}
        };
        for (Object[] row : menus) {
            jdbcTemplate.update("UPDATE sys_menu SET menu_name=?, icon=? WHERE id=?", row[1], row[2], row[0]);
        }
    }

    private void initCodeRuleLabels() {
        Object[][] rules = {
                {"SUPPLIER", "供应商编码"}, {"CUSTOMER", "客户编码"}, {"MATERIAL", "物料编码"},
                {"WAREHOUSE", "仓库编码"}, {"PUR_REQ", "采购申请"}, {"PUR_ORD", "采购订单"},
                {"SAL_LEAD", "销售线索"}, {"SAL_OPP", "商机编码"}, {"SAL_QUO", "报价单"},
                {"SAL_CON", "销售合同"}, {"SAL_ORD", "销售订单"}, {"SAL_AS", "售后服务"},
                {"STK_REC", "库存流水"},
                {"STK_TAKE", "盘点单"}, {"DELIVERY", "发货单"}
        };
        for (Object[] row : rules) {
            jdbcTemplate.update("UPDATE code_rule SET rule_name=? WHERE rule_code=?", row[1], row[0]);
        }
        jdbcTemplate.update(
                "INSERT INTO code_rule (rule_code, rule_name, prefix, date_pattern, seq_length) " +
                "SELECT 'STK_TAKE', '盘点单', 'ST', 'yyyyMMdd', 4 " +
                "WHERE NOT EXISTS (SELECT 1 FROM code_rule WHERE rule_code='STK_TAKE')");
        jdbcTemplate.update(
                "INSERT INTO code_rule (rule_code, rule_name, prefix, date_pattern, seq_length) " +
                "SELECT 'SAL_AS', '售后服务', 'AS', 'yyyyMMdd', 4 " +
                "WHERE NOT EXISTS (SELECT 1 FROM code_rule WHERE rule_code='SAL_AS')");
    }

    private void initUserLabels() {
        Object[][] users = {
                {"admin", "系统管理员"}, {"purchaser", "张采购"},
                {"sales", "李销售"}, {"warehouse", "王仓储"}
        };
        for (Object[] row : users) {
            jdbcTemplate.update("UPDATE sys_user SET nick_name=? WHERE user_name=?", row[1], row[0]);
        }
        jdbcTemplate.update("UPDATE sys_user SET email=CONCAT(user_name, '@eims.com') WHERE email IS NULL OR email=''");
        initSampleLogs();
    }

    /** 修复基础信息、仓储、系统管理中文乱码（Windows 导入 SQL 后显示问号） */
    private void initBaseDataLabels() {
        Object[][] depts = {
                {1L, "弗洛科技有限公司"}, {2L, "采购部"}, {3L, "销售部"},
                {4L, "仓储部"}, {5L, "信息技术部"}
        };
        for (Object[] row : depts) {
            jdbcTemplate.update("UPDATE sys_dept SET dept_name=? WHERE id=?", row[1], row[0]);
        }

        Object[][] roles = {
                {1L, "系统管理员"}, {2L, "采购专员"}, {3L, "销售专员"},
                {4L, "仓储专员"}, {5L, "部门负责人"}
        };
        for (Object[] row : roles) {
            jdbcTemplate.update("UPDATE sys_role SET role_name=? WHERE id=?", row[1], row[0]);
        }

        Object[][] suppliers = {
                {1L, "华东精密零部件有限公司", "陈经理", "6228480123456789012"},
                {2L, "深圳电子科技供应商", "刘总", "6228480234567890123"},
                {3L, "苏州包装材料厂", "王厂长", "6228480345678901234"}
        };
        for (Object[] row : suppliers) {
            jdbcTemplate.update(
                    "UPDATE supplier SET supplier_name=?, contact_person=?, bank_account=? WHERE id=?",
                    row[1], row[2], row[3], row[0]);
        }

        Object[][] customers = {
                {1L, "上海智造科技有限公司", "华东", "赵总"},
                {2L, "北京创新电子", "华北", "钱经理"},
                {3L, "广州贸易集团", "华南", "孙主管"}
        };
        for (Object[] row : customers) {
            jdbcTemplate.update(
                    "UPDATE customer SET customer_name=?, region=?, contact_person=? WHERE id=?",
                    row[1], row[2], row[3], row[0]);
        }

        Object[][] materials = {
                {1L, "高精度轴承", "机械件"}, {2L, "控制芯片", "电子件"},
                {3L, "包装纸箱", "包材"}, {4L, "不锈钢螺丝", "紧固件"},
                {5L, "工业润滑油", "辅料"}
        };
        for (Object[] row : materials) {
            jdbcTemplate.update("UPDATE material SET material_name=?, category=? WHERE id=?", row[1], row[2], row[0]);
        }

        Object[][] warehouses = {
                {1L, "主仓库", "上海市浦东新区"},
                {2L, "原材料仓", "上海市浦东新区A区"}
        };
        for (Object[] row : warehouses) {
            jdbcTemplate.update("UPDATE warehouse SET warehouse_name=?, location=? WHERE id=?", row[1], row[2], row[0]);
        }

        jdbcTemplate.update(
                "UPDATE inventory i JOIN material m ON i.material_id=m.id SET i.material_name=m.material_name");
        jdbcTemplate.update(
                "UPDATE purchase_request_item i JOIN material m ON i.material_id=m.id SET i.material_name=m.material_name");
        jdbcTemplate.update(
                "UPDATE sales_order_item i JOIN material m ON i.material_id=m.id SET i.material_name=m.material_name");
    }

    private void initRoleMenus() {
        int[][] roleMenus = {
                {2, 1}, {2, 10}, {2, 11}, {2, 13}, {2, 30}, {2, 31}, {2, 33}, {2, 34},
                {3, 1}, {3, 10}, {3, 12}, {3, 13}, {3, 40}, {3, 41}, {3, 42}, {3, 43},
                {3, 44}, {3, 45}, {3, 46}, {3, 47},
                {4, 1}, {4, 10}, {4, 13}, {4, 14}, {4, 20}, {4, 21}, {4, 22}, {4, 23}, {4, 24},
                {5, 1}, {5, 30}, {5, 32}
        };
        for (int[] rm : roleMenus) {
            jdbcTemplate.update(
                    "INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)", rm[0], rm[1]);
        }
    }

    private void initDemoData() {
        ensureInventoryLevels();
        ensurePurchaseData();
        ensurePendingApproval();
        ensureStockRecords();
        ensureStocktakeData();
        ensureSalesPipeline();
        ensureAmountTrendData();
        enrichOperationLogs();
    }

    private void ensureInventoryLevels() {
        jdbcTemplate.update("UPDATE inventory SET quantity=85, safety_stock=100 WHERE warehouse_id=1 AND material_id=1");
        jdbcTemplate.update("UPDATE inventory SET quantity=45, safety_stock=50 WHERE warehouse_id=2 AND material_id=5");
    }

    /** 保证至少有一条待审批采购申请，便于演示审批按钮 */
    private void ensurePendingApproval() {
        Long pending = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM purchase_request WHERE is_deleted=0 AND status=1", Long.class);
        if (pending != null && pending > 0) return;
        jdbcTemplate.update(
                "UPDATE purchase_request SET status=1, submit_time=NOW() WHERE id=1 AND is_deleted=0");
        jdbcTemplate.update("DELETE FROM purchase_approval WHERE request_id=1");
    }

    private void ensurePurchaseData() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM purchase_request", Long.class);
        if (count != null && count > 0) return;

        jdbcTemplate.update(
                "INSERT INTO purchase_request (id, request_code, dept_id, applicant_id, total_amount, status, submit_time, create_by) VALUES " +
                        "(1,'PR202601150001',2,2,15600.00,1,DATE_SUB(NOW(),INTERVAL 2 DAY),2)," +
                        "(2,'PR202601180001',2,2,28500.00,2,DATE_SUB(NOW(),INTERVAL 5 DAY),2)," +
                        "(3,'PR202601200001',2,2,8900.00,4,DATE_SUB(NOW(),INTERVAL 10 DAY),2)");
        jdbcTemplate.update(
                "INSERT INTO purchase_request_item (request_id, material_id, material_code, material_name, quantity, unit_price) VALUES " +
                        "(1,1,'MAT202601010001','高精度轴承',200,35.00),(1,4,'MAT202601010004','不锈钢螺丝',500,0.80)," +
                        "(2,2,'MAT202601010002','控制芯片',500,45.00),(2,3,'MAT202601010003','包装纸箱',1000,3.50)," +
                        "(3,5,'MAT202601010005','工业润滑油',30,120.00)");
        jdbcTemplate.update(
                "INSERT INTO purchase_approval (request_id, approver_id, approval_level, result, comment, approval_time) VALUES " +
                        "(2,1,1,1,'同意采购，请尽快比价',DATE_SUB(NOW(),INTERVAL 4 DAY))," +
                        "(2,1,2,1,'分管审批通过',DATE_SUB(NOW(),INTERVAL 3 DAY))");
        jdbcTemplate.update(
                "INSERT INTO purchase_quote (request_id, supplier_id, quote_amount, delivery_days, quality_score, is_selected, create_by) VALUES " +
                        "(2,1,28200.00,7,92.0,1,2),(2,2,29100.00,5,88.5,0,2),(2,3,27800.00,10,85.0,0,2)");
        jdbcTemplate.update(
                "INSERT INTO purchase_order (id, order_code, request_id, supplier_id, total_amount, status, expected_arrival_time, create_by, create_time) VALUES " +
                        "(1,'PO202601200001',3,1,8900.00,2,DATE_SUB(NOW(),INTERVAL 3 DAY),2,DATE_SUB(NOW(),INTERVAL 8 DAY))," +
                        "(2,'PO202601220001',2,1,28200.00,1,DATE_ADD(NOW(),INTERVAL 3 DAY),2,DATE_SUB(NOW(),INTERVAL 2 DAY))");
    }

    private void ensureStockRecords() {
        Long recent = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_record WHERE is_deleted=0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)",
                Long.class);
        if (recent != null && recent > 0) return;

        jdbcTemplate.update("DELETE FROM stock_record WHERE record_code LIKE 'SR20260101%'");
        jdbcTemplate.update(
                "INSERT INTO stock_record (record_code, warehouse_id, material_id, business_type, quantity, before_quantity, after_quantity, create_by, create_time) VALUES " +
                        "('SR202601010001',1,1,1,100,250,350,4,DATE_SUB(NOW(),INTERVAL 6 DAY))," +
                        "('SR202601010002',1,2,1,200,600,800,4,DATE_SUB(NOW(),INTERVAL 6 DAY))," +
                        "('SR202601010003',1,3,2,50,1250,1200,4,DATE_SUB(NOW(),INTERVAL 5 DAY))," +
                        "('SR202601010004',2,4,1,500,4500,5000,4,DATE_SUB(NOW(),INTERVAL 4 DAY))," +
                        "('SR202601010005',1,1,2,30,380,350,4,DATE_SUB(NOW(),INTERVAL 3 DAY))," +
                        "('SR202601010006',1,2,2,100,900,800,4,DATE_SUB(NOW(),INTERVAL 2 DAY))," +
                        "('SR202601010007',2,5,1,20,100,120,4,DATE_SUB(NOW(),INTERVAL 1 DAY))," +
                        "('SR202601010008',1,3,2,80,1280,1200,4,NOW())");
    }

    private void ensureStocktakeData() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stocktake", Long.class);
        if (count != null && count > 0) return;

        jdbcTemplate.update(
                "INSERT INTO stocktake (id, stocktake_code, warehouse_id, status, create_by, create_time) VALUES " +
                        "(1,'ST202601100001',1,1,4,DATE_SUB(NOW(),INTERVAL 15 DAY))," +
                        "(2,'ST202601230001',2,0,4,DATE_SUB(NOW(),INTERVAL 1 DAY))");
        jdbcTemplate.update(
                "INSERT INTO stocktake_item (stocktake_id, material_id, system_quantity, actual_quantity, diff_quantity) VALUES " +
                        "(1,1,350,348,-2),(1,2,800,800,0),(1,3,1200,1195,-5),(2,4,5000,4998,-2),(2,5,120,118,-2)");
    }

    private void ensureSalesPipeline() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sales_lead", Long.class);
        if (count != null && count > 0) return;

        jdbcTemplate.update(
                "INSERT INTO sales_lead (id, lead_code, customer_name, source, level, owner_id, status, create_by, create_time) VALUES " +
                        "(1,'SL202601050001','杭州智能装备有限公司','展会',2,3,1,3,DATE_SUB(NOW(),INTERVAL 20 DAY))," +
                        "(2,'SL202601100001','成都电子科技','官网',1,3,0,3,DATE_SUB(NOW(),INTERVAL 15 DAY))," +
                        "(3,'SL202601150001','武汉机械制造','转介绍',3,3,2,3,DATE_SUB(NOW(),INTERVAL 10 DAY))");
        jdbcTemplate.update(
                "INSERT INTO sales_opportunity (id, opportunity_code, lead_id, customer_id, opportunity_level, expected_amount, stage, create_by, create_time) VALUES " +
                        "(1,'SO202601120001',3,1,3,580000.00,3,3,DATE_SUB(NOW(),INTERVAL 8 DAY))," +
                        "(2,'SO202601180001',NULL,2,2,320000.00,2,3,DATE_SUB(NOW(),INTERVAL 5 DAY))," +
                        "(3,'SO202601220001',NULL,3,1,150000.00,1,3,DATE_SUB(NOW(),INTERVAL 2 DAY))");
        jdbcTemplate.update(
                "INSERT INTO sales_quotation (id, quotation_code, opportunity_id, customer_id, total_amount, valid_until, status, create_by, create_time) VALUES " +
                        "(1,'SQ202601150001',1,1,565000.00,DATE_ADD(NOW(),INTERVAL 30 DAY),2,3,DATE_SUB(NOW(),INTERVAL 6 DAY))," +
                        "(2,'SQ202601200001',2,2,310000.00,DATE_ADD(NOW(),INTERVAL 15 DAY),1,3,DATE_SUB(NOW(),INTERVAL 3 DAY))");
        jdbcTemplate.update(
                "INSERT INTO sales_contract (id, contract_code, customer_id, quotation_id, total_amount, sign_status, sign_time, create_by, create_time) VALUES " +
                        "(1,'SC202601180001',1,1,560000.00,1,DATE_SUB(NOW(),INTERVAL 4 DAY),3,DATE_SUB(NOW(),INTERVAL 5 DAY))");
        jdbcTemplate.update(
                "INSERT INTO sales_order (id, order_code, contract_id, customer_id, total_amount, status, payment_status, create_by, create_time) VALUES " +
                        "(1,'SD202601200001',1,1,280000.00,2,1,3,DATE_SUB(NOW(),INTERVAL 3 DAY))," +
                        "(2,'SD202601220001',NULL,2,156000.00,1,0,3,DATE_SUB(NOW(),INTERVAL 1 DAY))," +
                        "(3,'SD202601240001',NULL,3,89000.00,3,2,3,DATE_SUB(NOW(),INTERVAL 7 DAY))");
        jdbcTemplate.update(
                "INSERT INTO sales_order_item (order_id, material_id, material_code, material_name, quantity, unit_price, delivered_quantity) VALUES " +
                        "(1,1,'MAT202601010001','高精度轴承',500,35.00,300),(1,2,'MAT202601010002','控制芯片',200,45.00,200)," +
                        "(2,3,'MAT202601010003','包装纸箱',2000,3.50,0),(2,4,'MAT202601010004','不锈钢螺丝',10000,0.80,0)," +
                        "(3,5,'MAT202601010005','工业润滑油',50,120.00,50)");
        jdbcTemplate.update(
                "INSERT INTO sales_delivery (id, delivery_code, order_id, warehouse_id, status, logistics_no, create_by, create_time) VALUES " +
                        "(1,'DL202601210001',1,1,1,'SF1234567890',4,DATE_SUB(NOW(),INTERVAL 2 DAY))," +
                        "(2,'DL202601230001',1,1,0,NULL,4,DATE_SUB(NOW(),INTERVAL 1 DAY))," +
                        "(3,'DL202601200001',3,2,2,'YT9876543210',4,DATE_SUB(NOW(),INTERVAL 5 DAY))");
        jdbcTemplate.update(
                "INSERT INTO sales_after_service (service_code, order_id, customer_id, service_type, status, content, create_by, create_time) VALUES " +
                        "('AS202601220001',3,3,1,1,'客户反馈产品质量良好，已完成回访',3,DATE_SUB(NOW(),INTERVAL 3 DAY))," +
                        "('AS202601240001',1,1,3,0,'客户反映部分轴承包装破损，正在处理',3,DATE_SUB(NOW(),INTERVAL 1 DAY))");
    }

    /** 补充近30日销售/采购金额趋势数据 */
    private void ensureAmountTrendData() {
        Long salesDays = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT DATE(create_time)) FROM sales_order WHERE is_deleted=0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 29 DAY)",
                Long.class);
        if (salesDays != null && salesDays >= 5) return;

        Long reqId = jdbcTemplate.queryForObject(
                "SELECT id FROM purchase_request WHERE is_deleted=0 ORDER BY id LIMIT 1", Long.class);
        if (reqId == null) return;

        jdbcTemplate.update(
                "INSERT IGNORE INTO sales_order (order_code, customer_id, total_amount, status, payment_status, create_by, create_time) VALUES " +
                        "('SD202606010001',1,125000.00,3,2,3,DATE_SUB(CURDATE(),INTERVAL 28 DAY))," +
                        "('SD202606010002',2,98000.00,3,1,3,DATE_SUB(CURDATE(),INTERVAL 24 DAY))," +
                        "('SD202606010003',3,76000.00,3,2,3,DATE_SUB(CURDATE(),INTERVAL 20 DAY))," +
                        "('SD202606010004',1,198000.00,2,1,3,DATE_SUB(CURDATE(),INTERVAL 14 DAY))," +
                        "('SD202606010005',2,87000.00,3,2,3,DATE_SUB(CURDATE(),INTERVAL 10 DAY))," +
                        "('SD202606010006',3,112000.00,1,0,3,DATE_SUB(CURDATE(),INTERVAL 6 DAY))," +
                        "('SD202606010007',1,65000.00,3,2,3,DATE_SUB(CURDATE(),INTERVAL 2 DAY))");
        jdbcTemplate.update(
                "INSERT IGNORE INTO purchase_order (order_code, request_id, supplier_id, total_amount, status, create_by, create_time) VALUES " +
                        "('PO202606010001',?,1,32000.00,3,2,DATE_SUB(CURDATE(),INTERVAL 27 DAY))," +
                        "('PO202606010002',?,2,45000.00,2,2,DATE_SUB(CURDATE(),INTERVAL 22 DAY))," +
                        "('PO202606010003',?,1,28000.00,3,2,DATE_SUB(CURDATE(),INTERVAL 18 DAY))," +
                        "('PO202606010004',?,3,52000.00,1,2,DATE_SUB(CURDATE(),INTERVAL 12 DAY))," +
                        "('PO202606010005',?,1,36000.00,3,2,DATE_SUB(CURDATE(),INTERVAL 8 DAY))," +
                        "('PO202606010006',?,2,41000.00,2,2,DATE_SUB(CURDATE(),INTERVAL 4 DAY))," +
                        "('PO202606010007',?,1,25000.00,3,2,DATE_SUB(CURDATE(),INTERVAL 1 DAY))",
                reqId, reqId, reqId, reqId, reqId, reqId, reqId);
    }

    private void enrichOperationLogs() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_operation_log", Long.class);
        if (count != null && count >= 8) return;
        jdbcTemplate.update(
                "INSERT INTO sys_operation_log (user_id, module_name, operation_type, request_uri, ip_address, operation_result, operation_time) VALUES " +
                        "(3,'销售管理','创建订单','/api/sales/orders','127.0.0.1',1,DATE_SUB(NOW(),INTERVAL 3 DAY))," +
                        "(2,'采购管理','提交申请','/api/purchase/requests','127.0.0.1',1,DATE_SUB(NOW(),INTERVAL 2 DAY))," +
                        "(4,'仓储管理','盘点','/api/wms/stocktake','127.0.0.1',1,DATE_SUB(NOW(),INTERVAL 1 DAY))," +
                        "(3,'销售管理','发货','/api/sales/delivery','127.0.0.1',1,NOW())");
        Object[][] logs = {
                {1L, "系统管理", "登录"}, {2L, "基础信息", "查询"},
                {3L, "采购管理", "提交"}, {4L, "仓储管理", "入库"}
        };
        for (Object[] row : logs) {
            jdbcTemplate.update("UPDATE sys_operation_log SET module_name=?, operation_type=? WHERE id=?",
                    row[1], row[2], row[0]);
        }
    }

    private void initSampleLogs() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_operation_log", Long.class);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO sys_operation_log (user_id, module_name, operation_type, request_uri, ip_address, operation_result, operation_time) VALUES " +
                            "(1,'系统管理','登录','/api/auth/login','127.0.0.1',1,NOW())," +
                            "(1,'基础信息','查询','/api/base/suppliers','127.0.0.1',1,NOW())," +
                            "(2,'采购管理','提交','/api/purchase/workflow/submit/1','127.0.0.1',1,NOW())," +
                            "(4,'仓储管理','入库','/api/wms/scan','127.0.0.1',1,NOW())");
        }
        Object[][] logs = {
                {1L, "系统管理", "登录"}, {2L, "基础信息", "查询"},
                {3L, "采购管理", "提交"}, {4L, "仓储管理", "入库"}
        };
        for (Object[] row : logs) {
            jdbcTemplate.update("UPDATE sys_operation_log SET module_name=?, operation_type=? WHERE id=?",
                    row[1], row[2], row[0]);
        }
    }
}
