USE eims;

-- 编码规则
INSERT INTO code_rule (rule_code, rule_name, prefix, date_pattern, seq_length) VALUES
('SUPPLIER', '供应商编码', 'SUP', 'yyyyMMdd', 4),
('CUSTOMER', '客户编码', 'CUS', 'yyyyMMdd', 4),
('MATERIAL', '物料编码', 'MAT', 'yyyyMMdd', 4),
('WAREHOUSE', '仓库编码', 'WH', 'yyyyMMdd', 4),
('PUR_REQ', '采购申请', 'PR', 'yyyyMMdd', 4),
('PUR_ORD', '采购订单', 'PO', 'yyyyMMdd', 4),
('SAL_LEAD', '销售线索', 'SL', 'yyyyMMdd', 4),
('SAL_OPP', '商机编码', 'SO', 'yyyyMMdd', 4),
('SAL_QUO', '报价单', 'SQ', 'yyyyMMdd', 4),
('SAL_CON', '销售合同', 'SC', 'yyyyMMdd', 4),
('SAL_ORD', '销售订单', 'SD', 'yyyyMMdd', 4),
('SAL_AS', '售后服务', 'AS', 'yyyyMMdd', 4),
('STK_REC', '库存流水', 'SR', 'yyyyMMdd', 4),
('STK_TAKE', '盘点单', 'ST', 'yyyyMMdd', 4),
('DELIVERY', '发货单', 'DL', 'yyyyMMdd', 4)
ON DUPLICATE KEY UPDATE rule_name=VALUES(rule_name);

-- 部门
INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, sort_no) VALUES
(1, 0, '弗洛科技有限公司', 'ROOT', 0),
(2, 1, '采购部', 'PUR', 1),
(3, 1, '销售部', 'SAL', 2),
(4, 1, '仓储部', 'WMS', 3),
(5, 1, '信息技术部', 'IT', 4)
ON DUPLICATE KEY UPDATE dept_name=VALUES(dept_name);

-- 角色
INSERT INTO sys_role (id, role_name, role_code, data_scope) VALUES
(1, '系统管理员', 'ADMIN', 1),
(2, '采购专员', 'PURCHASER', 2),
(3, '销售专员', 'SALES', 2),
(4, '仓储专员', 'WAREHOUSE', 2),
(5, '部门负责人', 'MANAGER', 2)
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- 用户 admin/Admin@123456 (BCrypt)
INSERT INTO sys_user (id, dept_id, user_name, nick_name, password, phone, status) VALUES
(1, 5, 'admin', '系统管理员', '$2a$10$9uiLTq5BOYb4p9cYeaVYBOZ0MtXYUFKVAWoi6iCVNniH9EQG4kbqe', '13800000001', 1),
(2, 2, 'purchaser', '张采购', '$2a$10$9uiLTq5BOYb4p9cYeaVYBOZ0MtXYUFKVAWoi6iCVNniH9EQG4kbqe', '13800000002', 1),
(3, 3, 'sales', '李销售', '$2a$10$9uiLTq5BOYb4p9cYeaVYBOZ0MtXYUFKVAWoi6iCVNniH9EQG4kbqe', '13800000003', 1),
(4, 4, 'warehouse', '王仓储', '$2a$10$9uiLTq5BOYb4p9cYeaVYBOZ0MtXYUFKVAWoi6iCVNniH9EQG4kbqe', '13800000004', 1)
ON DUPLICATE KEY UPDATE nick_name=VALUES(nick_name);

-- 菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, icon, sort_no) VALUES
(1, 0, '工作台', 'dashboard', 2, '/dashboard', 'Odometer', 0),
(10, 0, '基础信息', 'base', 1, '/base', 'Collection', 10),
(11, 10, '供应商管理', 'base:supplier', 2, '/base/supplier', 'User', 1),
(12, 10, '客户管理', 'base:customer', 2, '/base/customer', 'Avatar', 2),
(13, 10, '物料管理', 'base:material', 2, '/base/material', 'Box', 3),
(14, 10, '仓库管理', 'base:warehouse', 2, '/base/warehouse', 'House', 4),
(15, 10, '编码规则', 'base:codeRule', 2, '/base/codeRule', 'Key', 5),
(20, 0, '仓储管理', 'wms', 1, '/wms', 'Goods', 20),
(21, 20, '库存查询', 'wms:inventory', 2, '/wms/inventory', 'Search', 1),
(22, 20, '扫码出入库', 'wms:scan', 2, '/wms/scan', 'Camera', 2),
(23, 20, '库存流水', 'wms:record', 2, '/wms/record', 'List', 3),
(24, 20, '盘点管理', 'wms:stocktake', 2, '/wms/stocktake', 'DocumentChecked', 4),
(30, 0, '采购管理', 'purchase', 1, '/purchase', 'ShoppingCart', 30),
(31, 30, '采购申请', 'purchase:request', 2, '/purchase/request', 'Edit', 1),
(32, 30, '采购审批', 'purchase:approval', 2, '/purchase/approval', 'Checked', 2),
(33, 30, '询价比价', 'purchase:quote', 2, '/purchase/quote', 'TrendCharts', 3),
(34, 30, '采购订单', 'purchase:order', 2, '/purchase/order', 'Tickets', 4),
(40, 0, '销售管理', 'sales', 1, '/sales', 'ShoppingCartFull', 40),
(41, 40, '销售线索', 'sales:lead', 2, '/sales/lead', 'Link', 1),
(42, 40, '商机管理', 'sales:opportunity', 2, '/sales/opportunity', 'TrendCharts', 2),
(43, 40, '销售报价', 'sales:quotation', 2, '/sales/quotation', 'PriceTag', 3),
(44, 40, '销售合同', 'sales:contract', 2, '/sales/contract', 'Document', 4),
(45, 40, '销售订单', 'sales:order', 2, '/sales/order', 'ShoppingBag', 5),
(46, 40, '发货跟踪', 'sales:delivery', 2, '/sales/delivery', 'Van', 6),
(47, 40, '售后服务', 'sales:afterService', 2, '/sales/afterService', 'Headset', 7),
(50, 0, '系统管理', 'system', 1, '/system', 'Setting', 50),
(51, 50, '用户管理', 'system:user', 2, '/system/user', 'UserFilled', 1),
(52, 50, '角色管理', 'system:role', 2, '/system/role', 'Stamp', 2),
(53, 50, '部门管理', 'system:dept', 2, '/system/dept', 'OfficeBuilding', 3),
(54, 50, '操作日志', 'system:log', 2, '/system/log', 'Notebook', 4)
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name);

-- 管理员拥有全部菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE is_deleted = 0
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 各角色菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 10), (2, 11), (2, 13), (2, 30), (2, 31), (2, 33), (2, 34),
(3, 1), (3, 10), (3, 12), (3, 13), (3, 40), (3, 41), (3, 42), (3, 43), (3, 44), (3, 45), (3, 46), (3, 47),
(4, 1), (4, 10), (4, 13), (4, 14), (4, 20), (4, 21), (4, 22), (4, 23), (4, 24),
(5, 1), (5, 30), (5, 32)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1,1),(2,2),(3,3),(4,4)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 示例供应商
INSERT INTO supplier (supplier_code, supplier_name, contact_person, contact_phone, bank_account, qualification_status, score) VALUES
('SUP202601010001', '华东精密零部件有限公司', '陈经理', '13911112222', '6228480123456789012', 1, 92.5),
('SUP202601010002', '深圳电子科技供应商', '刘总', '13922223333', '6228480234567890123', 1, 88.0),
('SUP202601010003', '苏州包装材料厂', '王厂长', '13933334444', '6228480345678901234', 1, 85.5)
ON DUPLICATE KEY UPDATE supplier_name=VALUES(supplier_name), bank_account=VALUES(bank_account);

-- 示例客户
INSERT INTO customer (customer_code, customer_name, region, level, contact_person, contact_phone) VALUES
('CUS202601010001', '上海智造科技有限公司', '华东', 3, '赵总', '13611112222'),
('CUS202601010002', '北京创新电子', '华北', 2, '钱经理', '13622223333'),
('CUS202601010003', '广州贸易集团', '华南', 1, '孙主管', '13633334444')
ON DUPLICATE KEY UPDATE customer_name=VALUES(customer_name);

-- 示例物料
INSERT INTO material (material_code, material_name, category, spec, unit, safety_stock) VALUES
('MAT202601010001', '高精度轴承', '机械件', '6205-2RS', '个', 100),
('MAT202601010002', '控制芯片', '电子件', 'STM32F103', '片', 200),
('MAT202601010003', '包装纸箱', '包材', '40x30x20cm', '个', 500),
('MAT202601010004', '不锈钢螺丝', '紧固件', 'M6x20', '个', 1000),
('MAT202601010005', '工业润滑油', '辅料', '1L/桶', '桶', 50)
ON DUPLICATE KEY UPDATE material_name=VALUES(material_name);

-- 示例仓库
INSERT INTO warehouse (warehouse_code, warehouse_name, location, manager_id, status) VALUES
('WH202601010001', '主仓库', '上海市浦东新区', 4, 1),
('WH202601010002', '原材料仓', '上海市浦东新区A区', 4, 1)
ON DUPLICATE KEY UPDATE warehouse_name=VALUES(warehouse_name);

-- 初始库存（轴承设为低库存用于预警演示）
INSERT INTO inventory (warehouse_id, material_id, material_code, material_name, quantity, safety_stock) VALUES
(1, 1, 'MAT202601010001', '高精度轴承', 85, 100),
(1, 2, 'MAT202601010002', '控制芯片', 800, 200),
(1, 3, 'MAT202601010003', '包装纸箱', 1200, 500),
(2, 4, 'MAT202601010004', '不锈钢螺丝', 5000, 1000),
(2, 5, 'MAT202601010005', '工业润滑油', 45, 50)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- ========== 演示业务数据 ==========

-- 采购申请
INSERT INTO purchase_request (id, request_code, dept_id, applicant_id, total_amount, status, submit_time, create_by) VALUES
(1, 'PR202601150001', 2, 2, 15600.00, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 2),
(2, 'PR202601180001', 2, 2, 28500.00, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 2),
(3, 'PR202601200001', 2, 2, 8900.00, 4, DATE_SUB(NOW(), INTERVAL 10 DAY), 2)
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

INSERT INTO purchase_request_item (request_id, material_id, material_code, material_name, quantity, unit_price) VALUES
(1, 1, 'MAT202601010001', '高精度轴承', 200, 35.00),
(1, 4, 'MAT202601010004', '不锈钢螺丝', 500, 0.80),
(2, 2, 'MAT202601010002', '控制芯片', 500, 45.00),
(2, 3, 'MAT202601010003', '包装纸箱', 1000, 3.50),
(3, 5, 'MAT202601010005', '工业润滑油', 30, 120.00)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- 采购审批
INSERT INTO purchase_approval (request_id, approver_id, approval_level, result, comment, approval_time) VALUES
(2, 1, 1, 1, '同意采购，请尽快比价', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 1, 2, 1, '分管审批通过', DATE_SUB(NOW(), INTERVAL 3 DAY))
ON DUPLICATE KEY UPDATE result=VALUES(result);

-- 询价比价
INSERT INTO purchase_quote (request_id, supplier_id, quote_amount, delivery_days, quality_score, is_selected, create_by) VALUES
(2, 1, 28200.00, 7, 92.0, 1, 2),
(2, 2, 29100.00, 5, 88.5, 0, 2),
(2, 3, 27800.00, 10, 85.0, 0, 2)
ON DUPLICATE KEY UPDATE quote_amount=VALUES(quote_amount);

-- 采购订单
INSERT INTO purchase_order (id, order_code, request_id, supplier_id, total_amount, status, expected_arrival_time, create_by, create_time) VALUES
(1, 'PO202601200001', 3, 1, 8900.00, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 2, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 'PO202601220001', 2, 1, 28200.00, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), 2, DATE_SUB(NOW(), INTERVAL 2 DAY))
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

-- 库存流水（近7日）
INSERT INTO stock_record (record_code, warehouse_id, material_id, business_type, quantity, before_quantity, after_quantity, create_by, create_time) VALUES
('SR202601010001', 1, 1, 1, 100, 250, 350, 4, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('SR202601010002', 1, 2, 1, 200, 600, 800, 4, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('SR202601010003', 1, 3, 2, 50, 1250, 1200, 4, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('SR202601010004', 2, 4, 1, 500, 4500, 5000, 4, DATE_SUB(NOW(), INTERVAL 4 DAY)),
('SR202601010005', 1, 1, 2, 30, 380, 350, 4, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('SR202601010006', 1, 2, 2, 100, 900, 800, 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('SR202601010007', 2, 5, 1, 20, 100, 120, 4, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('SR202601010008', 1, 3, 2, 80, 1280, 1200, 4, NOW())
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- 盘点单
INSERT INTO stocktake (id, stocktake_code, warehouse_id, status, create_by, create_time) VALUES
(1, 'ST202601100001', 1, 1, 4, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 'ST202601230001', 2, 0, 4, DATE_SUB(NOW(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE status=VALUES(status);

INSERT INTO stocktake_item (stocktake_id, material_id, system_quantity, actual_quantity, diff_quantity) VALUES
(1, 1, 350, 348, -2),
(1, 2, 800, 800, 0),
(1, 3, 1200, 1195, -5),
(2, 4, 5000, 4998, -2),
(2, 5, 120, 118, -2)
ON DUPLICATE KEY UPDATE actual_quantity=VALUES(actual_quantity);

-- 销售线索
INSERT INTO sales_lead (id, lead_code, customer_name, source, level, owner_id, status, create_by, create_time) VALUES
(1, 'SL202601050001', '杭州智能装备有限公司', '展会', 2, 3, 1, 3, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 'SL202601100001', '成都电子科技', '官网', 1, 3, 0, 3, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, 'SL202601150001', '武汉机械制造', '转介绍', 3, 3, 2, 3, DATE_SUB(NOW(), INTERVAL 10 DAY))
ON DUPLICATE KEY UPDATE customer_name=VALUES(customer_name);

-- 商机
INSERT INTO sales_opportunity (id, opportunity_code, lead_id, customer_id, opportunity_level, expected_amount, stage, create_by, create_time) VALUES
(1, 'SO202601120001', 3, 1, 3, 580000.00, 3, 3, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 'SO202601180001', NULL, 2, 2, 320000.00, 2, 3, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 'SO202601220001', NULL, 3, 1, 150000.00, 1, 3, DATE_SUB(NOW(), INTERVAL 2 DAY))
ON DUPLICATE KEY UPDATE expected_amount=VALUES(expected_amount);

-- 销售报价
INSERT INTO sales_quotation (id, quotation_code, opportunity_id, customer_id, total_amount, valid_until, status, create_by, create_time) VALUES
(1, 'SQ202601150001', 1, 1, 565000.00, DATE_ADD(NOW(), INTERVAL 30 DAY), 2, 3, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'SQ202601200001', 2, 2, 310000.00, DATE_ADD(NOW(), INTERVAL 15 DAY), 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY))
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

-- 销售合同
INSERT INTO sales_contract (id, contract_code, customer_id, quotation_id, total_amount, sign_status, sign_time, create_by, create_time) VALUES
(1, 'SC202601180001', 1, 1, 560000.00, 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 3, DATE_SUB(NOW(), INTERVAL 5 DAY))
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

-- 销售订单
INSERT INTO sales_order (id, order_code, contract_id, customer_id, total_amount, status, payment_status, create_by, create_time) VALUES
(1, 'SD202601200001', 1, 1, 280000.00, 2, 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'SD202601220001', NULL, 2, 156000.00, 1, 0, 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'SD202601240001', NULL, 3, 89000.00, 3, 2, 3, DATE_SUB(NOW(), INTERVAL 7 DAY))
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

INSERT INTO sales_order_item (order_id, material_id, material_code, material_name, quantity, unit_price, delivered_quantity) VALUES
(1, 1, 'MAT202601010001', '高精度轴承', 500, 35.00, 300),
(1, 2, 'MAT202601010002', '控制芯片', 200, 45.00, 200),
(2, 3, 'MAT202601010003', '包装纸箱', 2000, 3.50, 0),
(2, 4, 'MAT202601010004', '不锈钢螺丝', 10000, 0.80, 0),
(3, 5, 'MAT202601010005', '工业润滑油', 50, 120.00, 50)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- 发货单
INSERT INTO sales_delivery (id, delivery_code, order_id, warehouse_id, status, logistics_no, create_by, create_time) VALUES
(1, 'DL202601210001', 1, 1, 1, 'SF1234567890', 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'DL202601230001', 1, 1, 0, NULL, 4, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'DL202601200001', 3, 2, 2, 'YT9876543210', 4, DATE_SUB(NOW(), INTERVAL 5 DAY))
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 售后服务
INSERT INTO sales_after_service (service_code, order_id, customer_id, service_type, status, content, create_by, create_time) VALUES
('AS202601220001', 3, 3, 1, 1, '客户反馈产品质量良好，已完成回访', 3, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('AS202601240001', 1, 1, 3, 0, '客户反映部分轴承包装破损，正在处理', 3, DATE_SUB(NOW(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE content=VALUES(content);

-- 补充用户邮箱
UPDATE sys_user SET email=CONCAT(user_name, '@eims.com') WHERE email IS NULL OR email='';

-- 近30日销售/采购趋势补充数据
INSERT IGNORE INTO sales_order (order_code, customer_id, total_amount, status, payment_status, create_by, create_time) VALUES
('SD202606010001', 1, 125000.00, 3, 2, 3, DATE_SUB(CURDATE(), INTERVAL 28 DAY)),
('SD202606010002', 2, 98000.00, 3, 1, 3, DATE_SUB(CURDATE(), INTERVAL 24 DAY)),
('SD202606010003', 3, 76000.00, 3, 2, 3, DATE_SUB(CURDATE(), INTERVAL 20 DAY)),
('SD202606010004', 1, 198000.00, 2, 1, 3, DATE_SUB(CURDATE(), INTERVAL 14 DAY)),
('SD202606010005', 2, 87000.00, 3, 2, 3, DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
('SD202606010006', 3, 112000.00, 1, 0, 3, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
('SD202606010007', 1, 65000.00, 3, 2, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY));

INSERT IGNORE INTO purchase_order (order_code, request_id, supplier_id, total_amount, status, create_by, create_time) VALUES
('PO202606010001', 2, 1, 32000.00, 3, 2, DATE_SUB(CURDATE(), INTERVAL 27 DAY)),
('PO202606010002', 2, 2, 45000.00, 2, 2, DATE_SUB(CURDATE(), INTERVAL 22 DAY)),
('PO202606010003', 2, 1, 28000.00, 3, 2, DATE_SUB(CURDATE(), INTERVAL 18 DAY)),
('PO202606010004', 2, 3, 52000.00, 1, 2, DATE_SUB(CURDATE(), INTERVAL 12 DAY)),
('PO202606010005', 2, 1, 36000.00, 3, 2, DATE_SUB(CURDATE(), INTERVAL 8 DAY)),
('PO202606010006', 2, 2, 41000.00, 2, 2, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
('PO202606010007', 2, 1, 25000.00, 3, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY));

