package com.shengyi.fec.reimbursement.service;

import com.shengyi.fec.reimbursement.dto.ItineraryDTO;
import com.shengyi.fec.reimbursement.dto.SubsidyCalendarDTO;
import com.shengyi.fec.reimbursement.dto.SubsidyDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class SubsidyGenerator {
    private static final BigDecimal TRAFFIC_STD = BigDecimal.valueOf(40);
    private static final BigDecimal COMM_STD = BigDecimal.valueOf(40);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CityTierCache cityTierCache;

    public SubsidyGenerator(CityTierCache cityTierCache) {
        this.cityTierCache = cityTierCache;
    }

    public List<SubsidyDTO> generateFromItineraries(List<ItineraryDTO> itineraries) {
        List<SubsidyDTO> result = new ArrayList<>();
        for (ItineraryDTO it : itineraries) {
            SubsidyDTO sub = new SubsidyDTO();
            sub.setItineraryId(it.getId());
            sub.setTravelerId(it.getTravelerId());
            sub.setTravelerNo(it.getTravelerNo());
            sub.setTravelerName(it.getTravelerName());
            sub.setDepartureDate(it.getDepartureDate());
            sub.setArrivalDate(it.getArrivalDate());
            sub.setDepartureCity(it.getDepartureCity());
            sub.setDepartureCityNo(it.getDepartureCityNo());
            sub.setArrivingCity(it.getArrivingCity());
            sub.setArrivingCityNo(it.getArrivingCityNo());

            LocalDate start = LocalDate.parse(it.getDepartureDate(), FMT);
            LocalDate end = LocalDate.parse(it.getArrivalDate(), FMT);
            int days = (int) ChronoUnit.DAYS.between(start, end) + 1;
            sub.setSubsidyDays(days);

            BigDecimal mealStd = cityTierCache.mealStandard(it.getArrivingCityNo(), it.getArrivingCity());
            List<SubsidyCalendarDTO> calendars = new ArrayList<>();
            BigDecimal appTotal = BigDecimal.ZERO;
            BigDecimal subTotal = BigDecimal.ZERO;
            BigDecimal mealTotal = BigDecimal.ZERO;
            BigDecimal trafficTotal = BigDecimal.ZERO;
            BigDecimal phoneTotal = BigDecimal.ZERO;

            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                SubsidyCalendarDTO cal = new SubsidyCalendarDTO();
                cal.setTravelDate(d.format(FMT));
                cal.setTravelDateWeek(weekName(d.getDayOfWeek()));
                cal.setSubsidizedCities(it.getArrivingCity());
                cal.setSubsidizedCityNumber(it.getArrivingCityNo());
                cal.setStandardMealExpensesAmount(mealStd);
                cal.setStandardTrafficAmount(TRAFFIC_STD);
                cal.setStandardCommunicationAmount(COMM_STD);
                cal.setMealSelected(true);
                cal.setTrafficSelected(true);
                cal.setCommunicationSelected(true);
                cal.setIsReimbursed(true);
                cal.setMealExpensesAmount(mealStd);
                cal.setTrafficAmount(TRAFFIC_STD);
                cal.setCommunicationAmount(COMM_STD);
                calendars.add(cal);

                appTotal = appTotal.add(mealStd).add(TRAFFIC_STD).add(COMM_STD);
                subTotal = subTotal.add(mealStd).add(TRAFFIC_STD).add(COMM_STD);
                mealTotal = mealTotal.add(mealStd);
                trafficTotal = trafficTotal.add(TRAFFIC_STD);
                phoneTotal = phoneTotal.add(COMM_STD);
            }

            sub.setCalendars(calendars);
            sub.setApplicationAmount(appTotal);
            sub.setSubsidyAmount(subTotal);
            sub.setMealAllowance(mealTotal);
            sub.setTransportationAllowance(trafficTotal);
            sub.setPhoneAllowance(phoneTotal);
            result.add(sub);
        }
        return result;
    }

    private String weekName(DayOfWeek dow) {
        return dow.getDisplayName(java.time.format.TextStyle.SHORT, Locale.CHINA);
    }
}
