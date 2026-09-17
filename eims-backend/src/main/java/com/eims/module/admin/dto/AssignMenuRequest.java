package com.eims.module.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignMenuRequest {
    private List<Long> menuIds;
}
