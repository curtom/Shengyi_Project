package service;

import common.PageResult;
import dto.ItineraryDTO;
import dto.ReimbursementDTO;
import dto.ReimbursementQueryReq;
import dto.SubsidyCalendarDTO;

import java.util.List;
import java.util.Map;

public interface ReimbursementService {
    PageResult<ReimbursementDTO> pageQuery(ReimbursementQueryReq req);

    ReimbursementDTO getDetail(String id);

    String saveDraft(ReimbursementDTO dto);

    String submit(ReimbursementDTO dto);

    void voidDoc(String id);

    void deleteDoc(String id);

    void pushDoc(String id);

    void withdrawDoc(String id);

    List<SubsidyCalendarDTO> previewSubsidyCalendar(List<ItineraryDTO> itineraries);

    Map<String, Object> masterData();
}