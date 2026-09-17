package com.eims.module.sales.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalesOpportunityVO {
    private Long id;
    private String opportunityCode;
    private Long leadId;
    private String leadCode;
    private String leadCustomerName;
    private Long customerId;
    private String customerName;
    private Integer opportunityLevel;
    private BigDecimal expectedAmount;
    private Integer stage;
    private String stageLabel;
    private Integer currentStep;
    private String currentStepName;
    private LocalDateTime createTime;
    private List<SalesQuotationVO> quotations;
    private List<SalesContractVO> contracts;
    private List<SalesOrderVO> orders;
    private List<SalesDeliveryVO> deliveries;
    private List<SalesAfterServiceVO> afterServices;
    private List<SalesWorkflowEventVO> timeline;
}
