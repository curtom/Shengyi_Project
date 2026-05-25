
package com.shengyi.fec.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("fk_reim_subsidy")
public class ReimbursementSubsidyEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long mainId;
    private LocalDate subsidyDate;
    private Integer cityLevel;
    private BigDecimal standardAmount;
    private BigDecimal actualAmount;
    private String subsidyType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMainId() { return mainId; }
    public void setMainId(Long mainId) { this.mainId = mainId; }
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
