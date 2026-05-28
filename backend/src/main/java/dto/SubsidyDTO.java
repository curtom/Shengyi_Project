package dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SubsidyDTO {
    private String id;
    private String tripId;
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    private String startDate;
    private String endDate;
    private Integer days;
    private String routeText;
    private String allowanceCityNo;
    private BigDecimal applicationAmount;
    private BigDecimal allowanceAmount;
    private BigDecimal mealAllowance;
    private BigDecimal transportationAllowance;
    private BigDecimal phoneAllowance;
    private List<SubsidyCalendarDTO> calendar = new ArrayList<>();
}