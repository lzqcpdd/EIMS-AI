USE eims;

-- 菜单
UPDATE sys_menu SET menu_name='工作台', icon='Odometer' WHERE id=1;
UPDATE sys_menu SET menu_name='基础信息' WHERE id=10;
UPDATE sys_menu SET menu_name='供应商管理' WHERE id=11;
UPDATE sys_menu SET menu_name='客户管理' WHERE id=12;
UPDATE sys_menu SET menu_name='物料管理' WHERE id=13;
UPDATE sys_menu SET menu_name='仓库管理' WHERE id=14;
UPDATE sys_menu SET menu_name='编码规则' WHERE id=15;
UPDATE sys_menu SET menu_name='仓储管理' WHERE id=20;
UPDATE sys_menu SET menu_name='库存查询' WHERE id=21;
UPDATE sys_menu SET menu_name='扫码出入库' WHERE id=22;
UPDATE sys_menu SET menu_name='库存流水' WHERE id=23;
UPDATE sys_menu SET menu_name='盘点管理' WHERE id=24;
UPDATE sys_menu SET menu_name='采购管理' WHERE id=30;
UPDATE sys_menu SET menu_name='采购申请' WHERE id=31;
UPDATE sys_menu SET menu_name='采购审批' WHERE id=32;
UPDATE sys_menu SET menu_name='询价比价' WHERE id=33;
UPDATE sys_menu SET menu_name='采购订单' WHERE id=34;
UPDATE sys_menu SET menu_name='销售管理', icon='ShoppingCartFull' WHERE id=40;
UPDATE sys_menu SET menu_name='销售线索', icon='Link' WHERE id=41;
UPDATE sys_menu SET menu_name='商机管理', icon='TrendCharts' WHERE id=42;
UPDATE sys_menu SET menu_name='销售报价' WHERE id=43;
UPDATE sys_menu SET menu_name='销售合同' WHERE id=44;
UPDATE sys_menu SET menu_name='销售订单' WHERE id=45;
UPDATE sys_menu SET menu_name='发货跟踪' WHERE id=46;
UPDATE sys_menu SET menu_name='售后服务', icon='Headset' WHERE id=47;
UPDATE sys_menu SET menu_name='系统管理' WHERE id=50;
UPDATE sys_menu SET menu_name='用户管理' WHERE id=51;
UPDATE sys_menu SET menu_name='角色管理' WHERE id=52;
UPDATE sys_menu SET menu_name='部门管理' WHERE id=53;
UPDATE sys_menu SET menu_name='操作日志' WHERE id=54;

-- 用户与部门
UPDATE sys_user SET nick_name='系统管理员' WHERE user_name='admin';
UPDATE sys_user SET nick_name='张采购' WHERE user_name='purchaser';
UPDATE sys_user SET nick_name='李销售' WHERE user_name='sales';
UPDATE sys_user SET nick_name='王仓储' WHERE user_name='warehouse';
UPDATE sys_dept SET dept_name='弗洛科技有限公司' WHERE id=1;
UPDATE sys_dept SET dept_name='采购部' WHERE id=2;
UPDATE sys_dept SET dept_name='销售部' WHERE id=3;
UPDATE sys_dept SET dept_name='仓储部' WHERE id=4;
UPDATE sys_dept SET dept_name='信息技术部' WHERE id=5;

-- 角色
UPDATE sys_role SET role_name='系统管理员' WHERE id=1;
UPDATE sys_role SET role_name='采购专员' WHERE id=2;
UPDATE sys_role SET role_name='销售专员' WHERE id=3;
UPDATE sys_role SET role_name='仓储专员' WHERE id=4;
UPDATE sys_role SET role_name='部门负责人' WHERE id=5;

-- 供应商
UPDATE supplier SET supplier_name='华东精密零部件有限公司', contact_person='陈经理', bank_account='6228480123456789012' WHERE id=1;
UPDATE supplier SET supplier_name='深圳电子科技供应商', contact_person='刘总', bank_account='6228480234567890123' WHERE id=2;
UPDATE supplier SET supplier_name='苏州包装材料厂', contact_person='王厂长', bank_account='6228480345678901234' WHERE id=3;

-- 客户
UPDATE customer SET customer_name='上海智造科技有限公司', region='华东', contact_person='赵总' WHERE id=1;
UPDATE customer SET customer_name='北京创新电子', region='华北', contact_person='钱经理' WHERE id=2;
UPDATE customer SET customer_name='广州贸易集团', region='华南', contact_person='孙主管' WHERE id=3;

-- 物料
UPDATE material SET material_name='高精度轴承', category='机械件' WHERE id=1;
UPDATE material SET material_name='控制芯片', category='电子件' WHERE id=2;
UPDATE material SET material_name='包装纸箱', category='包材' WHERE id=3;
UPDATE material SET material_name='不锈钢螺丝', category='紧固件' WHERE id=4;
UPDATE material SET material_name='工业润滑油', category='辅料' WHERE id=5;

-- 仓库
UPDATE warehouse SET warehouse_name='主仓库', location='上海市浦东新区' WHERE id=1;
UPDATE warehouse SET warehouse_name='原材料仓', location='上海市浦东新区A区' WHERE id=2;

-- 库存物料名称同步
UPDATE inventory i JOIN material m ON i.material_id=m.id SET i.material_name=m.material_name;

-- 编码规则
UPDATE code_rule SET rule_name='供应商编码' WHERE rule_code='SUPPLIER';
UPDATE code_rule SET rule_name='客户编码' WHERE rule_code='CUSTOMER';
UPDATE code_rule SET rule_name='物料编码' WHERE rule_code='MATERIAL';
UPDATE code_rule SET rule_name='仓库编码' WHERE rule_code='WAREHOUSE';
