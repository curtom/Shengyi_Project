package service;

import dto.ItineraryDTO;
import dto.SubsidyCalendarDTO;
import dto.SubsidyDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SubsidyGenerator {
    private static final BigDecimal TRAFFIC_STD = BigDecimal.valueOf(40);
    private static final BigDecimal COMM_STD = BigDecimal.valueOf(40);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] WEEKDAYS = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    private final CityTierCache cityTierCache;

    public SubsidyGenerator(CityTierCache cityTierCache) {
        this.cityTierCache = cityTierCache;
    }

    public List<SubsidyDTO> generateFromItineraries(List<ItineraryDTO> itineraries) {
        List<SubsidyDTO> result = new ArrayList<>();
        for (ItineraryDTO it : itineraries) {
            SubsidyDTO sub = new SubsidyDTO();
            sub.setId("allowance-" + it.getId());
            sub.setTripId(it.getId());
            sub.setTravelerId(it.getTravelerId());
            sub.setTravelerNo(it.getTravelerNo());
            sub.setTravelerName(it.getTravelerName());
            sub.setStartDate(it.getStartDate());
            sub.setEndDate(it.getEndDate());
            sub.setAllowanceCityNo(it.getArrivalCityNo());

            LocalDate start = LocalDate.parse(it.getStartDate(), FMT);
            LocalDate end = LocalDate.parse(it.getEndDate(), FMT);
            int days = (int) ChronoUnit.DAYS.between(start, end) + 1;
            sub.setDays(days);

            BigDecimal mealStd = cityTierCache.mealStandard(it.getArrivalCityNo(), "");
            List<SubsidyCalendarDTO> calendars = new ArrayList<>();
            BigDecimal appTotal = BigDecimal.ZERO;
            BigDecimal subTotal = BigDecimal.ZERO;
            BigDecimal mealTotal = BigDecimal.ZERO;
            BigDecimal trafficTotal = BigDecimal.ZERO;
            BigDecimal phoneTotal = BigDecimal.ZERO;

            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                SubsidyCalendarDTO cal = new SubsidyCalendarDTO();
                cal.setId(it.getId() + "-" + d.format(FMT));
                cal.setDate(d.format(FMT));
                cal.setWeekday(WEEKDAYS[d.getDayOfWeek().getValue() % 7]);
                cal.setAllowanceCityNo(it.getArrivalCityNo());

                Map<String, SubsidyCalendarDTO.AllowanceAmountCell> cells = new HashMap<>();

                SubsidyCalendarDTO.AllowanceAmountCell mealCell = new SubsidyCalendarDTO.AllowanceAmountCell();
                mealCell.setItemType("meal");
                mealCell.setStandardAmount(mealStd);
                mealCell.setAllowanceAmount(BigDecimal.ZERO);
                mealCell.setSelected(false);
                cells.put("meal", mealCell);

                SubsidyCalendarDTO.AllowanceAmountCell trafficCell = new SubsidyCalendarDTO.AllowanceAmountCell();
                trafficCell.setItemType("traffic");
                trafficCell.setStandardAmount(TRAFFIC_STD);
                trafficCell.setAllowanceAmount(BigDecimal.ZERO);
                trafficCell.setSelected(false);
                cells.put("traffic", trafficCell);

                SubsidyCalendarDTO.AllowanceAmountCell commCell = new SubsidyCalendarDTO.AllowanceAmountCell();
                commCell.setItemType("communication");
                commCell.setStandardAmount(COMM_STD);
                commCell.setAllowanceAmount(BigDecimal.ZERO);
                commCell.setSelected(false);
                cells.put("communication", commCell);

                cal.setCells(cells);
                calendars.add(cal);
            }

            sub.setCalendar(calendars);
            sub.setApplicationAmount(appTotal);
            sub.setAllowanceAmount(subTotal);
            sub.setMealAllowance(mealTotal);
            sub.setTransportationAllowance(trafficTotal);
            sub.setPhoneAllowance(phoneTotal);
            result.add(sub);
        }
        return result;
    }
}