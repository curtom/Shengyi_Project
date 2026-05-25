package com.shengyi.fec.reimbursement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SubsidyDTO {
    private String id;
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
    private List<SubsidyCalendarDTO> calendars = new ArrayList<>();
}
