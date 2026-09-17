-- EIMS 企业综合管理平台 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS eims DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eims;

-- ========== 系统管理 ==========
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT UNSIGNED DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) NOT NULL COMMENT '部门编码',
    sort_no INT UNSIGNED DEFAULT 0,
    status TINYINT UNSIGNED DEFAULT 1 COMMENT '0-停用 1-正常',
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_dept_code (dept_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB COMMENT='部门表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    data_scope TINYINT UNSIGNED DEFAULT 1 COMMENT '1-全部 2-本部门 3-本人',
    status TINYINT UNSIGNED DEFAULT 1,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT UNSIGNED DEFAULT 0,
    menu_name VARCHAR(100) NOT NULL,
    menu_code VARCHAR(100) NOT NULL,
    menu_type TINYINT UNSIGNED DEFAULT 1 COMMENT '1-目录 2-菜单 3-按钮 4-接口',
    path VARCHAR(200),
    icon VARCHAR(100),
    sort_no INT UNSIGNED DEFAULT 0,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_menu_code (menu_code)
) ENGINE=InnoDB COMMENT='菜单权限表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT UNSIGNED NOT NULL,
    menu_id BIGINT UNSIGNED NOT NULL,
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联';

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    dept_id BIGINT UNSIGNED,
    user_name VARCHAR(50) NOT NULL,
    nick_name VARCHAR(50),
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT UNSIGNED DEFAULT 1,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_user_name (user_name),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB COMMENT='用户角色关联';

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED,
    module_name VARCHAR(50),
    operation_type VARCHAR(50) NOT NULL,
    business_id BIGINT UNSIGNED,
    request_uri VARCHAR(255),
    ip_address VARCHAR(50),
    operation_result TINYINT UNSIGNED DEFAULT 1,
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB COMMENT='操作日志';

-- ========== 基础信息 ==========
CREATE TABLE IF NOT EXISTS code_rule (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(50) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(100) NOT NULL,
    prefix VARCHAR(20) NOT NULL COMMENT '前缀',
    date_pattern VARCHAR(20) DEFAULT 'yyyyMMdd',
    seq_length INT UNSIGNED DEFAULT 4,
    current_seq INT UNSIGNED DEFAULT 0,
    status TINYINT UNSIGNED DEFAULT 1,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_rule_code (rule_code)
) ENGINE=InnoDB COMMENT='编码规则';

CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    bank_account VARCHAR(50),
    qualification_status TINYINT UNSIGNED DEFAULT 1 COMMENT '0-失效 1-有效',
    score DECIMAL(5,2) DEFAULT 0,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_supplier_code (supplier_code)
) ENGINE=InnoDB COMMENT='供应商';

CREATE TABLE IF NOT EXISTS customer (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    region VARCHAR(50),
    level TINYINT UNSIGNED DEFAULT 1 COMMENT '1-普通 2-重点 3-战略',
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_customer_code (customer_code),
    KEY idx_region (region)
) ENGINE=InnoDB COMMENT='客户';

CREATE TABLE IF NOT EXISTS material (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    spec VARCHAR(100),
    unit VARCHAR(20) NOT NULL,
    safety_stock INT UNSIGNED DEFAULT 0,
    status TINYINT UNSIGNED DEFAULT 1,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_material_code (material_code),
    KEY idx_category (category)
) ENGINE=InnoDB COMMENT='物料';

CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    warehouse_code VARCHAR(50) NOT NULL,
    warehouse_name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    manager_id BIGINT UNSIGNED,
    status TINYINT UNSIGNED DEFAULT 1,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_warehouse_code (warehouse_code)
) ENGINE=InnoDB COMMENT='仓库';

-- ========== 仓储管理 ==========
CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT UNSIGNED NOT NULL,
    material_id BIGINT UNSIGNED NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    quantity INT UNSIGNED DEFAULT 0,
    locked_quantity INT UNSIGNED DEFAULT 0,
    safety_stock INT UNSIGNED DEFAULT 0,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_wh_material (warehouse_id, material_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB COMMENT='库存';

CREATE TABLE IF NOT EXISTS stock_record (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    record_code VARCHAR(50) NOT NULL,
    warehouse_id BIGINT UNSIGNED NOT NULL,
    material_id BIGINT UNSIGNED NOT NULL,
    business_type TINYINT UNSIGNED NOT NULL COMMENT '1-采购入库 2-销售出库 3-盘点调整 4-退货入库 5-其他',
    quantity INT UNSIGNED NOT NULL,
    before_quantity INT UNSIGNED NOT NULL,
    after_quantity INT UNSIGNED NOT NULL,
    business_id BIGINT UNSIGNED,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_record_code (record_code),
    KEY idx_business_id (business_id)
) ENGINE=InnoDB COMMENT='库存流水';

CREATE TABLE IF NOT EXISTS stocktake (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    stocktake_code VARCHAR(50) NOT NULL,
    warehouse_id BIGINT UNSIGNED NOT NULL,
    status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-进行中 1-已完成 2-已确认',
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_stocktake_code (stocktake_code)
) ENGINE=InnoDB COMMENT='盘点单';

CREATE TABLE IF NOT EXISTS stocktake_item (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    stocktake_id BIGINT UNSIGNED NOT NULL,
    material_id BIGINT UNSIGNED NOT NULL,
    system_quantity INT UNSIGNED DEFAULT 0,
    actual_quantity INT UNSIGNED DEFAULT 0,
    diff_quantity INT DEFAULT 0,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    KEY idx_stocktake_id (stocktake_id)
) ENGINE=InnoDB COMMENT='盘点明细';

-- ========== 采购管理 ==========
CREATE TABLE IF NOT EXISTS purchase_request (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_code VARCHAR(50) NOT NULL,
    dept_id BIGINT UNSIGNED NOT NULL,
    applicant_id BIGINT UNSIGNED NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0,
    status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-草稿 1-审批中 2-通过 3-驳回 4-已生成订单',
    submit_time DATETIME,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_request_code (request_code),
    KEY idx_status (status)
) ENGINE=InnoDB COMMENT='采购申请';

CREATE TABLE IF NOT EXISTS purchase_request_item (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT UNSIGNED NOT NULL,
    material_id BIGINT UNSIGNED NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    quantity INT UNSIGNED NOT NULL,
    unit_price DECIMAL(12,2) DEFAULT 0,
    expected_date DATETIME,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    KEY idx_request_id (request_id)
) ENGINE=InnoDB COMMENT='采购申请明细';

CREATE TABLE IF NOT EXISTS purchase_quote (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT UNSIGNED NOT NULL,
    supplier_id BIGINT UNSIGNED NOT NULL,
    quote_amount DECIMAL(12,2) DEFAULT 0,
    delivery_days INT UNSIGNED DEFAULT 0,
    quality_score DECIMAL(5,2) DEFAULT 0,
    is_selected TINYINT UNSIGNED DEFAULT 0,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    KEY idx_request_id (request_id),
    KEY idx_supplier_id (supplier_id)
) ENGINE=InnoDB COMMENT='询价比价';

CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL,
    request_id BIGINT UNSIGNED NOT NULL,
    supplier_id BIGINT UNSIGNED NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0,
    status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-待确认 1-部分入库 2-全部入库 3-已完成 4-取消',
    expected_arrival_time DATETIME,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_order_code (order_code),
    KEY idx_supplier_id (supplier_id)
) ENGINE=InnoDB COMMENT='采购订单';

CREATE TABLE IF NOT EXISTS purchase_approval (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT UNSIGNED NOT NULL,
    approver_id BIGINT UNSIGNED NOT NULL,
    approval_level TINYINT UNSIGNED NOT NULL COMMENT '1-部门 2-分管 3-总经理',
    result TINYINT UNSIGNED COMMENT '1-通过 2-驳回',
    comment VARCHAR(500),
    approval_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    KEY idx_request_id (request_id)
) ENGINE=InnoDB COMMENT='采购审批记录';

-- ========== 销售管理 ==========
CREATE TABLE IF NOT EXISTS sales_lead (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    lead_code VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    source VARCHAR(50),
    level TINYINT UNSIGNED DEFAULT 1,
    owner_id BIGINT UNSIGNED NOT NULL,
    status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-待跟进 1-跟进中 2-转商机 3-无效',
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_lead_code (lead_code),
    KEY idx_owner_id (owner_id)
) ENGINE=InnoDB COMMENT='销售线索';

CREATE TABLE IF NOT EXISTS sales_opportunity (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    opportunity_code VARCHAR(50) NOT NULL,
    lead_id BIGINT UNSIGNED,
    customer_id BIGINT UNSIGNED NOT NULL,
    opportunity_level TINYINT UNSIGNED DEFAULT 1,
    expected_amount DECIMAL(12,2) DEFAULT 0,
    stage TINYINT UNSIGNED DEFAULT 1 COMMENT '1-初步 2-报价 3-合同 4-成交 5-失败',
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_opportunity_code (opportunity_code),
    KEY idx_customer_id (customer_id)
) ENGINE=InnoDB COMMENT='商机';

CREATE TABLE IF NOT EXISTS sales_quotation (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    quotation_code VARCHAR(50) NOT NULL,
    opportunity_id BIGINT UNSIGNED NOT NULL,
    customer_id BIGINT UNSIGNED NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0,
    valid_until DATETIME,
    status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-草稿 1-已提交 2-已确认 3-失效',
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_quotation_code (quotation_code)
) ENGINE=InnoDB COMMENT='销售报价';

CREATE TABLE IF NOT EXISTS sales_contract (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    contract_code VARCHAR(50) NOT NULL,
    customer_id BIGINT UNSIGNED NOT NULL,
    quotation_id BIGINT UNSIGNED,
    total_amount DECIMAL(12,2) DEFAULT 0,
    sign_status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-待签 1-已签 2-作废',
    sign_time DATETIME,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_contract_code (contract_code)
) ENGINE=InnoDB COMMENT='销售合同';

CREATE TABLE IF NOT EXISTS sales_order (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL,
    contract_id BIGINT UNSIGNED,
    customer_id BIGINT UNSIGNED NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0,
    status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-待确认 1-待发货 2-部分发货 3-已完成 4-取消',
    payment_status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-未回款 1-部分 2-已回款',
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_order_code (order_code)
) ENGINE=InnoDB COMMENT='销售订单';

CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    material_id BIGINT UNSIGNED NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    quantity INT UNSIGNED NOT NULL,
    unit_price DECIMAL(12,2) DEFAULT 0,
    delivered_quantity INT UNSIGNED DEFAULT 0,
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB COMMENT='销售订单明细';

CREATE TABLE IF NOT EXISTS sales_delivery (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    delivery_code VARCHAR(50) NOT NULL,
    order_id BIGINT UNSIGNED NOT NULL,
    warehouse_id BIGINT UNSIGNED NOT NULL,
    status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-待出库 1-已出库 2-已签收',
    logistics_no VARCHAR(100),
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_delivery_code (delivery_code)
) ENGINE=InnoDB COMMENT='发货单';

CREATE TABLE IF NOT EXISTS sales_after_service (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    service_code VARCHAR(50) NOT NULL,
    order_id BIGINT UNSIGNED NOT NULL,
    customer_id BIGINT UNSIGNED NOT NULL,
    service_type TINYINT UNSIGNED DEFAULT 1 COMMENT '1-回访 2-退货 3-投诉',
    status TINYINT UNSIGNED DEFAULT 0 COMMENT '0-处理中 1-已完成',
    content VARCHAR(1000),
    create_by BIGINT UNSIGNED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT UNSIGNED,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT UNSIGNED DEFAULT 0,
    remark VARCHAR(500),
    UNIQUE KEY uk_service_code (service_code)
) ENGINE=InnoDB COMMENT='售后服务';
