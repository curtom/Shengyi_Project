
package com.shengyi.fec.reimbursement.service;

import com.shengyi.fec.reimbursement.dto.request.ItinerarySaveRequest;
import com.shengyi.fec.reimbursement.entity.ReimbursementItineraryEntity;

import java.util.List;

public interface ItineraryService {

    Long save(ItinerarySaveRequest request);

    void delete(Long id);

    ReimbursementItineraryEntity getById(Long id);

    List<ReimbursementItineraryEntity> listByMainId(Long mainId);

    void deleteByMainId(Long mainId);
}
