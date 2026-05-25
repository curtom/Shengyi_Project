
package com.shengyi.fec.reimbursement.controller;

import com.shengyi.fec.reimbursement.common.ErrorCode;
import com.shengyi.fec.reimbursement.common.Result;
import com.shengyi.fec.reimbursement.dto.request.ItinerarySaveRequest;
import com.shengyi.fec.reimbursement.entity.ReimbursementItineraryEntity;
import com.shengyi.fec.reimbursement.service.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;

    @GetMapping("/list/{mainId}")
    public Result<List<ReimbursementItineraryEntity>> list(@PathVariable Long mainId) {
        List<ReimbursementItineraryEntity> list = itineraryService.listByMainId(mainId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<ReimbursementItineraryEntity> getById(@PathVariable Long id) {
        ReimbursementItineraryEntity entity = itineraryService.getById(id);
        if (entity == null) {
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.success(entity);
    }

    @PostMapping("/save")
    public Result<Long> save(@RequestBody ItinerarySaveRequest request) {
        Long id = itineraryService.save(request);
        return Result.success(id);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        itineraryService.delete(id);
        return Result.success();
    }
}
