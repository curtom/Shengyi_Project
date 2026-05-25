
package com.shengyi.fec.reimbursement.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubsidyCalendarUpdateRequest {

    private Integer cityLevel;
    private LocalDate effectiveDate;
    private BigDecimal subsidyAmount;
    private String remark;

    public Integer getCityLevel() { return cityLevel; }
    public void setCityLevel(Integer cityLevel) { this.cityLevel = cityLevel; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public BigDecimal getSubsidyAmount() { return subsidyAmount; }
    public void setSubsidyAmount(BigDecimal subsidyAmount) { this.subsidyAmount = subsidyAmount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
