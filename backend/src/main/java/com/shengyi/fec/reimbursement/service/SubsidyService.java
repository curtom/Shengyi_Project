
package com.shengyi.fec.reimbursement.service;

import com.shengyi.fec.reimbursement.dto.request.SubsidyCalendarUpdateRequest;
import com.shengyi.fec.reimbursement.entity.ReimbursementSubsidyEntity;
import com.shengyi.fec.reimbursement.entity.SubsidyCalendarEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SubsidyService {

    void generateSubsidies(Long mainId);

    void deleteByMainId(Long mainId);

    List<ReimbursementSubsidyEntity> listByMainId(Long mainId);

    List<SubsidyCalendarEntity> getCalendarByCityLevel(Integer cityLevel);

    void updateCalendar(List<SubsidyCalendarUpdateRequest> requests);

    BigDecimal getEffectiveAmount(Integer cityLevel, LocalDate date);
}
