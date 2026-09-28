package com.eims.module.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 智能查询核心服务
 * 流程：用户问题 → 大模型生成 SQL → 安全校验 → 执行 SQL → 返回结果
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiQueryService {

    private final DeepSeekClient deepSeekClient;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 数据库表结构说明（精简版）
     * 把这部分作为 Prompt 的一部分发给大模型，让它知道有哪些表和字段可用
     * 注意：Java 8 不支持文本块（"""），用字符串拼接代替
     */
    private static final String SCHEMA_HINT =
            "你是一个 MySQL SQL 生成专家。请根据用户的中文问题，生成一条可直接执行的 SELECT 查询语句。\n" +
            "\n" +
            "数据库表结构如下（所有表都有 is_deleted 字段，软删除标记，查询时必须加 WHERE is_deleted=0）：\n" +
            "\n" +
            "【供应商表】supplier: id, supplier_name, contact_person, score, qualification_status\n" +
            "【客户表】customer: id, customer_name, region, level(1普通/2重点/3战略)\n" +
            "【物料表】material: id, material_name, category, spec, unit, safety_stock\n" +
            "【库存表】inventory: id, warehouse_id, material_id, material_name, quantity, safety_stock\n" +
            "【库存流水表】stock_record: id, business_type(1采购入库/2销售出库/3盘点/4退货), quantity, create_time\n" +
            "【采购订单表】purchase_order: id, order_code, supplier_id, total_amount, status, create_time\n" +
            "【采购申请表】purchase_request: id, request_code, total_amount, status, submit_time\n" +
            "【销售订单表】sales_order: id, order_code, customer_id, total_amount, status, create_time\n" +
            "【销售合同表】sales_contract: id, contract_code, customer_id, total_amount, sign_time\n" +
            "【销售线索表】sales_lead: id, lead_code, customer_name, status, level\n" +
            "【商机表】sales_opportunity: id, opportunity_code, customer_id, expected_amount, stage(1初步/2报价/3合同/4成交/5失败)\n" +
            "【售后服务表】sales_after_service: id, service_code, customer_id, service_type(1回访/2退货/3投诉), create_time\n" +
            "\n" +
            "要求：\n" +
            "1. 只生成 SELECT 语句，不要 INSERT/UPDATE/DELETE\n" +
            "2. 所有表必须加 is_deleted=0 条件\n" +
            "3. 需要关联表时用 JOIN，外键关系：purchase_order.supplier_id -> supplier.id，sales_order.customer_id -> customer.id\n" +
            "4. 金额字段 total_amount 是 DECIMAL\n" +
            "5. 时间字段是 create_time 或 submit_time 或 sign_time\n" +
            "6. 只输出 SQL 本身，用 ```sql 代码块包裹，不要任何解释文字\n";

    /**
     * 危险 SQL 关键词黑名单（防止大模型生成破坏性 SQL）
     */
    private static final Pattern DANGEROUS_SQL = Pattern.compile(
            "\\b(INSERT|UPDATE|DELETE|DROP|ALTER|TRUNCATE|CREATE|GRANT|REVOKE|EXEC|EXECUTE|MERGE|REPLACE)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * 从大模型返回中提取纯 SQL（去掉 ```sql 代码块标记和多余文字）
     */
    private static final Pattern SQL_BLOCK = Pattern.compile("```(?:sql)?\\s*(.*?)\\s*```", Pattern.DOTALL);

    /**
     * 主入口：处理用户的自然语言问题
     *
     * @param question 用户用中文提的业务问题
     * @return 查询结果（包含 SQL 和数据）
     */
    public Map<String, Object> ask(String question) {
        // 1. 让大模型把中文问题转成 SQL
        String rawResponse = deepSeekClient.chat(SCHEMA_HINT, question);
        String sql = extractSql(rawResponse);

        // 2. 安全校验：只能是 SELECT 查询
        validateSql(sql);

        // 3. 执行 SQL
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        log.info("AI 查询执行成功，返回 {} 行数据", rows.size());

        // 4. 组装返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);       // 用户原始问题
        result.put("sql", sql);                // 生成的 SQL（给前端展示用）
        result.put("rows", rows);              // 查询数据
        result.put("rowCount", rows.size());   // 行数
        return result;
    }

    /**
     * 从大模型返回中提取 SQL
     * 大模型可能返回 ```sql SELECT ... ``` 这种格式，也可能直接返回纯 SQL
     */
    private String extractSql(String raw) {
        Matcher m = SQL_BLOCK.matcher(raw);
        if (m.find()) {
            return m.group(1).trim();
        }
        return raw.trim();
    }

    /**
     * 安全校验 SQL：只允许 SELECT，禁止任何写操作和 DDL
     */
    private void validateSql(String sql) {
        String upperSql = sql.toUpperCase().trim();

        // 必须以 SELECT 或 WITH 开头
        if (!upperSql.startsWith("SELECT") && !upperSql.startsWith("WITH")) {
            throw new IllegalArgumentException("只允许查询语句（SELECT），当前生成的不是 SELECT");
        }

        // 检查危险关键词
        Matcher m = DANGEROUS_SQL.matcher(upperSql);
        if (m.find()) {
            throw new IllegalArgumentException("检测到危险操作关键词：" + m.group() + "，已拒绝执行");
        }

        log.info("SQL 安全校验通过");
    }
}
