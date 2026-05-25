package com.shengyi.fec.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("fk_reim_subsidy")
public class SubsidyEntity {
    @TableId
    private String id;
    private String mainId;
    private String itineraryId;
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    private String departureDate;
    private String arrivalDate;
    private Integer subsidyDays;
    private String departureCity;
    private String departureCityNo;
    private String arrivingCity;
    private String arrivingCityNo;
    private BigDecimal applicationAmount;
    private BigDecimal subsidyAmount;
    private BigDecimal mealAllowance;
    private BigDecimal transportationAllowance;
    private BigDecimal phoneAllowance;
    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;
}
