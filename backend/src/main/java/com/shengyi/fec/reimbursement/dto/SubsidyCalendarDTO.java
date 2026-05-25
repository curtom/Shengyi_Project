package com.shengyi.fec.reimbursement.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubsidyCalendarDTO {
    private String id;
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
    private Boolean mealSelected;
    private Boolean trafficSelected;
    private Boolean communicationSelected;
    private Boolean isReimbursed;
}
