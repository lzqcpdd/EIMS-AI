package com.eims.module.sales.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalesWorkflowEventVO {
    private String stepName;
    private String title;
    private String description;
    /** success / warning / info / danger */
    private String type;
    private LocalDateTime eventTime;
}
