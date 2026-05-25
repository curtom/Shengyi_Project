package com.shengyi.fec.reimbursement.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("fk_subsidy_calendar")
public class SubsidyCalendarEntity {
    @TableId
    private String id;
    private String mainId;
    private String subsidyId;
    private String travelDate;
    private String travelDateWeek;
    private String subsidizedCities;
    private String subsidizedCityNumber;
    private String remark;
    private BigDecimal standardMealExpensesAmount;
    private BigDecimal standardTrafficAmount;
    private BigDecimal standardCommunicationAmount;
    private BigDecimal mealExpensesAmount;
    private BigDecimal trafficAmount;
    private BigDecimal communicationAmount;
    private String mealSelected;
    private String trafficSelected;
    private String communicationSelected;
    private String isReimbursed;
}
