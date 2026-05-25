package com.shengyi.fec.reimbursement.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllocationDTO {
    private String id;
    private Integer sortNo;
    private String allocCompanyId;
    private String allocCompanyNo;
    private String allocCompanyName;
    private String projectId;
    private String projectNo;
    private String projectName;
    private BigDecimal allocationRatio;
    private BigDecimal allocationAmount;
}
