
package com.shengyi.fec.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("fk_subsidy_calendar")
public class SubsidyCalendarEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer cityLevel;
    private LocalDate effectiveDate;
    private BigDecimal subsidyAmount;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getCityLevel() { return cityLevel; }
    public void setCityLevel(Integer cityLevel) { this.cityLevel = cityLevel; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public BigDecimal getSubsidyAmount() { return subsidyAmount; }
    public void setSubsidyAmount(BigDecimal subsidyAmount) { this.subsidyAmount = subsidyAmount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
