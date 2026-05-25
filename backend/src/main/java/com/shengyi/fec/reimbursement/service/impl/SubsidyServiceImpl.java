
package com.shengyi.fec.reimbursement.service.impl;

import com.shengyi.fec.reimbursement.dto.request.SubsidyCalendarUpdateRequest;
import com.shengyi.fec.reimbursement.entity.ReimbursementItineraryEntity;
import com.shengyi.fec.reimbursement.entity.ReimbursementSubsidyEntity;
import com.shengyi.fec.reimbursement.entity.SubsidyCalendarEntity;
import com.shengyi.fec.reimbursement.mapper.ReimbursementItineraryMapper;
import com.shengyi.fec.reimbursement.mapper.ReimbursementSubsidyMapper;
import com.shengyi.fec.reimbursement.mapper.SubsidyCalendarMapper;
import com.shengyi.fec.reimbursement.service.SubsidyService;
import com.shengyi.fec.reimbursement.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubsidyServiceImpl implements SubsidyService {

    @Autowired
    private ReimbursementSubsidyMapper subsidyMapper;

    @Autowired
    private ReimbursementItineraryMapper itineraryMapper;

    @Autowired
    private SubsidyCalendarMapper calendarMapper;

    @Override
    @Transactional
    public void generateSubsidies(Long mainId) {
        subsidyMapper.deleteByMainId(mainId);

        List<ReimbursementItineraryEntity> itineraries = itineraryMapper.selectByMainId(mainId);
        if (itineraries.isEmpty()) {
            return;
        }

        Map<LocalDate, Integer> dateCityLevelMap = new LinkedHashMap<>();
        for (ReimbursementItineraryEntity itinerary : itineraries) {
            LocalDate travelDate = itinerary.getStartDate() != null ? itinerary.getStartDate() : LocalDate.now();
            dateCityLevelMap.put(travelDate, itinerary.getCityLevel());
        }

        List<ReimbursementSubsidyEntity> subsidies = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : dateCityLevelMap.entrySet()) {
            LocalDate date = entry.getKey();
            Integer cityLevel = entry.getValue();

            ReimbursementSubsidyEntity subsidy = new ReimbursementSubsidyEntity();
            subsidy.setMainId(mainId);
            subsidy.setSubsidyDate(date);
            subsidy.setCityLevel(cityLevel);

            BigDecimal amount = getEffectiveAmount(cityLevel, date);
            subsidy.setStandardAmount(amount);
            subsidy.setActualAmount(amount);
            subsidy.setSubsidyType(DateUtil.isWeekend(date) ? "周末补助" : "工作日补助");

            subsidies.add(subsidy);
        }

        if (!subsidies.isEmpty()) {
            subsidies.forEach(subsidyMapper::insert);
        }
    }

    @Override
    @Transactional
    public void deleteByMainId(Long mainId) {
        subsidyMapper.deleteByMainId(mainId);
    }

    @Override
    public List<ReimbursementSubsidyEntity> listByMainId(Long mainId) {
        return subsidyMapper.selectByMainId(mainId);
    }

    @Override
    public List<SubsidyCalendarEntity> getCalendarByCityLevel(Integer cityLevel) {
        return calendarMapper.selectByCityLevel(cityLevel);
    }

    @Override
    @Transactional
    public void updateCalendar(List<SubsidyCalendarUpdateRequest> requests) {
        for (SubsidyCalendarUpdateRequest request : requests) {
            SubsidyCalendarEntity entity = new SubsidyCalendarEntity();
            entity.setCityLevel(request.getCityLevel());
            entity.setEffectiveDate(request.getEffectiveDate());
            entity.setSubsidyAmount(request.getSubsidyAmount());
            entity.setRemark(request.getRemark());

            calendarMapper.insert(entity);
        }
    }

    @Override
    public BigDecimal getEffectiveAmount(Integer cityLevel, LocalDate date) {
        BigDecimal amount = calendarMapper.selectEffectiveAmount(cityLevel, date);
        if (amount != null) {
            return amount;
        }

        return switch (cityLevel) {
            case 1 -> new BigDecimal("200.00");
            case 2 -> new BigDecimal("150.00");
            case 3 -> new BigDecimal("100.00");
            default -> new BigDecimal("80.00");
        };
    }
}
