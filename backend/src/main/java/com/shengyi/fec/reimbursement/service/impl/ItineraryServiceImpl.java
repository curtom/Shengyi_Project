
package com.shengyi.fec.reimbursement.service.impl;

import com.shengyi.fec.reimbursement.dto.request.ItinerarySaveRequest;
import com.shengyi.fec.reimbursement.entity.ReimbursementItineraryEntity;
import com.shengyi.fec.reimbursement.mapper.ReimbursementItineraryMapper;
import com.shengyi.fec.reimbursement.service.ItineraryService;
import com.shengyi.fec.reimbursement.service.SubsidyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItineraryServiceImpl implements ItineraryService {

    @Autowired
    private ReimbursementItineraryMapper itineraryMapper;

    @Autowired
    private SubsidyService subsidyService;

    @Override
    @Transactional
    public Long save(ItinerarySaveRequest request) {
        ReimbursementItineraryEntity entity = new ReimbursementItineraryEntity();
        entity.setId(request.getId());
        entity.setMainId(request.getMainId());
        entity.setTraveler(request.getTraveler());
        entity.setTravelDate(request.getTravelDate());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setDepartureCity(request.getDepartureCity());
        entity.setDestinationCity(request.getDestinationCity());
        entity.setRoute(request.getRoute());
        entity.setCityLevel(request.getCityLevel());
        entity.setTransportType(request.getTransportType());
        entity.setRemark(request.getRemark());
        entity.setDescription(request.getDescription());

        if (entity.getId() == null) {
            itineraryMapper.insert(entity);
        } else {
            itineraryMapper.updateById(entity);
        }

        if (entity.getMainId() != null) {
            subsidyService.generateSubsidies(entity.getMainId());
        }

        return entity.getId();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ReimbursementItineraryEntity entity = itineraryMapper.selectById(id);
        if (entity != null && entity.getMainId() != null) {
            subsidyService.generateSubsidies(entity.getMainId());
        }
        itineraryMapper.deleteById(id);
    }

    @Override
    public ReimbursementItineraryEntity getById(Long id) {
        return itineraryMapper.selectById(id);
    }

    @Override
    public List<ReimbursementItineraryEntity> listByMainId(Long mainId) {
        return itineraryMapper.selectByMainId(mainId);
    }

    @Override
    @Transactional
    public void deleteByMainId(Long mainId) {
        itineraryMapper.deleteByMainId(mainId);
    }
}
