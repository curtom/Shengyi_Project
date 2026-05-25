
package com.shengyi.fec.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("fk_reim_main")
public class ReimbursementMainEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String reimbNo;
    private String title;
    private String applicant;
    private String department;
    private String expenseCompany;
    private String businessType;
    private String reason;
    private BigDecimal totalAmount;
    private BigDecimal subsidyTotal;
    private BigDecimal mealAllowance;
    private BigDecimal transportAllowance;
    private BigDecimal communicationAllowance;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReimbNo() { return reimbNo; }
    public void setReimbNo(String reimbNo) { this.reimbNo = reimbNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getApplicant() { return applicant; }
    public void setApplicant(String applicant) { this.applicant = applicant; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getExpenseCompany() { return expenseCompany; }
    public void setExpenseCompany(String expenseCompany) { this.expenseCompany = expenseCompany; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getSubsidyTotal() { return subsidyTotal; }
    public void setSubsidyTotal(BigDecimal subsidyTotal) { this.subsidyTotal = subsidyTotal; }
    public BigDecimal getMealAllowance() { return mealAllowance; }
    public void setMealAllowance(BigDecimal mealAllowance) { this.mealAllowance = mealAllowance; }
    public BigDecimal getTransportAllowance() { return transportAllowance; }
    public void setTransportAllowance(BigDecimal transportAllowance) { this.transportAllowance = transportAllowance; }
    public BigDecimal getCommunicationAllowance() { return communicationAllowance; }
    public void setCommunicationAllowance(BigDecimal communicationAllowance) { this.communicationAllowance = communicationAllowance; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
