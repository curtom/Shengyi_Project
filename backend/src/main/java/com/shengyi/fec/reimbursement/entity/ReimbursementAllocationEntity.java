
package com.shengyi.fec.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("fk_reim_allocation")
public class ReimbursementAllocationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long mainId;
    private String expenseType;
    private String expenseTypeName;
    private String projectCode;
    private String projectName;
    private BigDecimal allocationRatio;
    private BigDecimal allocationAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMainId() { return mainId; }
    public void setMainId(Long mainId) { this.mainId = mainId; }
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
