package com.shengyi.fec.reimbursement.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItineraryDTO {
    private String id;
    @NotBlank(message = "出行人不能为空")
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    @NotBlank(message = "出发日期不能为空")
    private String departureDate;
    @NotBlank(message = "到达日期不能为空")
    private String arrivalDate;
    @NotBlank(message = "出发城市不能为空")
    private String departureCity;
    private String departureCityNo;
    @NotBlank(message = "到达城市不能为空")
    private String arrivingCity;
    private String arrivingCityNo;
    @NotBlank(message = "行程说明不能为空")
    private String itineraryInstructions;
}
