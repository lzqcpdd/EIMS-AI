# EIMS 企业综合管理平台

基于需求规格说明书、概要设计说明书、详细设计文档实现的 **EIMS（Enterprise Integrated Management System）** 前后端分离项目。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + ECharts |
| 后端 | Spring Boot 2.7 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0 |

## 功能模块

- **基础信息管理**：供应商、客户、物料、仓库、统一编码规则
- **仓储管理 (WMS)**：库存查询、扫码/RFID 出入库、库存流水、盘点
- **系统管理**：用户、角色、部门、RBAC 权限、操作日志
- **采购管理**：采购申请、多级审批、询价比价、采购订单、到货入库
- **销售管理**：线索、商机、报价、合同、订单、发货、售后

## 快速启动

### 1. 初始化数据库

```bash
mysql -uroot -p123456 < eims-backend/src/main/resources/db/schema.sql
mysql -uroot -p123456 < eims-backend/src/main/resources/db/data.sql
```

### 2. 启动后端

```bash
cd eims-backend
mvn spring-boot:run
```

默认使用 **dev** 环境（`application-dev.yml`），本地无需额外配置。

后端地址：http://localhost:8080/api

### 3. 启动前端

```bash
cd eims-frontend
npm install
npm run dev
```

前端地址：http://localhost:5173

## 环境配置

### 后端

| 文件 | 说明 |
|------|------|
| `application.yml` | 公共配置 |
| `application-dev.yml` | 开发环境（默认），含本地数据库默认值 |
| `application-prod.yml` | 生产环境，敏感项必须通过环境变量注入 |
| `.env.example` | 环境变量说明模板 |

**开发环境**（默认）：

```bash
cd eims-backend
mvn spring-boot:run
```

**生产环境**：

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:mysql://host:3306/eims?useSSL=true&serverTimezone=Asia/Shanghai"
export DB_USERNAME=eims
export DB_PASSWORD=your-password
export JWT_SECRET=your-secret-at-least-32-chars
export CORS_ALLOWED_ORIGINS=https://your-frontend.example.com
mvn spring-boot:run
```

### 前端

| 文件 | 说明 |
|------|------|
| `.env.development` | 开发环境（`npm run dev`） |
| `.env.production` | 生产构建（`npm run build`） |
| `.env.example` | 可复制的本地覆盖模板 |

关键变量：

- `VITE_API_BASE_URL` — API 基础路径，默认同域 `/api`
- `VITE_PROXY_TARGET` — 开发代理目标，默认 `http://localhost:8080`

前后端分离部署时，构建前设置：

```bash
VITE_API_BASE_URL=https://api.example.com/api npm run build
```

## 演示账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | Admin@123456 | 系统管理员 |
| purchaser | Admin@123456 | 采购专员 |
| sales | Admin@123456 | 销售专员 |
| warehouse | Admin@123456 | 仓储专员 |

## 项目结构

```
project/
├── eims-backend/          # Spring Boot 后端
│   └── src/main/java/com/eims/
│       ├── module/admin/  # 系统管理
│       ├── module/base/   # 基础信息
│       ├── module/wms/    # 仓储管理
│       ├── module/purchase/
│       └── module/sales/
├── eims-frontend/         # Vue3 前端
└── README.md
```

## API 规范

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "traceId": "abc123"
}
```

认证方式：请求头 `Authorization: Bearer <token>`
