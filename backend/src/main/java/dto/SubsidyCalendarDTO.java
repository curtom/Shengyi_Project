package dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class SubsidyCalendarDTO {
    private String id;
    private String date;
    private String weekday;
    private String allowanceCityNo;
    private Map<String, AllowanceAmountCell> cells = new HashMap<>();

    @Data
    public static class AllowanceAmountCell {
        private String itemType;
        private BigDecimal standardAmount;
        private BigDecimal allowanceAmount;
        private Boolean selected;
    }
}