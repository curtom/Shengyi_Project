
package com.shengyi.fec.reimbursement.dto.request;

import java.math.BigDecimal;

public class AllocationDTO {

    private Long id;
    private String expenseType;
    private String expenseTypeName;
    private String projectCode;
    private String projectName;
    private BigDecimal allocationRatio;
    private BigDecimal allocationAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }
    public String getExpenseTypeName() { return expenseTypeName; }
    public void setExpenseTypeName(String expenseTypeName) { this.expenseTypeName = expenseTypeName; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public BigDecimal getAllocationRatio() { return allocationRatio; }
    public void setAllocationRatio(BigDecimal allocationRatio) { this.allocationRatio = allocationRatio; }
    public BigDecimal getAllocationAmount() { return allocationAmount; }
    public void setAllocationAmount(BigDecimal allocationAmount) { this.allocationAmount = allocationAmount; }
}
