
package com.shengyi.fec.reimbursement.controller;

import com.shengyi.fec.reimbursement.common.Result;
import com.shengyi.fec.reimbursement.dto.request.SubsidyCalendarUpdateRequest;
import com.shengyi.fec.reimbursement.entity.ReimbursementSubsidyEntity;
import com.shengyi.fec.reimbursement.entity.SubsidyCalendarEntity;
import com.shengyi.fec.reimbursement.service.SubsidyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subsidy")
public class SubsidyController {

    @Autowired
    private SubsidyService subsidyService;

    @GetMapping("/list/{mainId}")
    public Result<List<ReimbursementSubsidyEntity>> list(@PathVariable Long mainId) {
        List<ReimbursementSubsidyEntity> list = subsidyService.listByMainId(mainId);
        return Result.success(list);
    }

    @GetMapping("/calendar/{cityLevel}")
    public Result<List<SubsidyCalendarEntity>> getCalendar(@PathVariable Integer cityLevel) {
        List<SubsidyCalendarEntity> list = subsidyService.getCalendarByCityLevel(cityLevel);
        return Result.success(list);
    }

    @PutMapping("/calendar")
    public Result<Void> updateCalendar(@RequestBody List<SubsidyCalendarUpdateRequest> requests) {
        subsidyService.updateCalendar(requests);
        return Result.success();
    }
}
