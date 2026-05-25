package com.shengyi.fec.reimbursement.service;

import com.shengyi.fec.common.PageResult;
import com.shengyi.fec.reimbursement.dto.ReimbursementDTO;
import com.shengyi.fec.reimbursement.dto.ReimbursementQueryReq;
import com.shengyi.fec.reimbursement.dto.SubsidyCalendarDTO;

import java.util.List;
import java.util.Map;

public interface ReimbursementService {
    PageResult<ReimbursementDTO> pageQuery(ReimbursementQueryReq req);

    ReimbursementDTO getDetail(String id);

    String saveDraft(ReimbursementDTO dto);

    String submit(ReimbursementDTO dto);

    void voidDoc(String id);

    List<SubsidyCalendarDTO> previewSubsidyCalendar(List<com.shengyi.fec.reimbursement.dto.ItineraryDTO> itineraries);

    Map<String, Object> masterData();
}
