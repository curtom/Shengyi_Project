
package com.shengyi.fec.reimbursement.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReimbursementSubsidyDTO {

    private Long id;
    private LocalDate subsidyDate;
    private Integer cityLevel;
    private BigDecimal standardAmount;
    private BigDecimal actualAmount;
    private String subsidyType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getSubsidyDate() { return subsidyDate; }
    public void setSubsidyDate(LocalDate subsidyDate) { this.subsidyDate = subsidyDate; }
    public Integer getCityLevel() { return cityLevel; }
    public void setCityLevel(Integer cityLevel) { this.cityLevel = cityLevel; }
    public BigDecimal getStandardAmount() { return standardAmount; }
    public void setStandardAmount(BigDecimal standardAmount) { this.standardAmount = standardAmount; }
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
    public String getSubsidyType() { return subsidyType; }
    public void setSubsidyType(String subsidyType) { this.subsidyType = subsidyType; }
}
