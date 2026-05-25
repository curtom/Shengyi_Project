package com.shengyi.fec.reimbursement.controller;

import com.shengyi.fec.common.PageResult;
import com.shengyi.fec.common.Result;
import com.shengyi.fec.reimbursement.dto.ItineraryDTO;
import com.shengyi.fec.reimbursement.dto.ReimbursementDTO;
import com.shengyi.fec.reimbursement.dto.ReimbursementQueryReq;
import com.shengyi.fec.reimbursement.dto.SubsidyCalendarDTO;
import com.shengyi.fec.reimbursement.service.ReimbursementService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reimbursement")
@RequiredArgsConstructor
public class ReimbursementController {

    private final ReimbursementService reimbursementService;

    @PostMapping("/page")
    public Result<PageResult<ReimbursementDTO>> page(@RequestBody ReimbursementQueryReq req) {
        return Result.ok(reimbursementService.pageQuery(req));
    }

    @GetMapping("/{id}")
    public Result<ReimbursementDTO> detail(@PathVariable String id) {
        return Result.ok(reimbursementService.getDetail(id));
    }

    @PostMapping("/save")
    public Result<String> save(@Valid @RequestBody ReimbursementDTO dto) {
        return Result.ok(reimbursementService.saveDraft(dto));
    }

    @PostMapping("/submit")
    public Result<String> submit(@Valid @RequestBody ReimbursementDTO dto) {
        return Result.ok(reimbursementService.submit(dto));
    }

    @PostMapping("/void/{id}")
    public Result<Void> voidDoc(@PathVariable String id) {
        reimbursementService.voidDoc(id);
        return Result.ok();
    }

    @PostMapping("/subsidy/preview")
    public Result<List<SubsidyCalendarDTO>> previewSubsidy(@RequestBody List<ItineraryDTO> itineraries) {
        return Result.ok(reimbursementService.previewSubsidyCalendar(itineraries));
    }

    @GetMapping("/master-data")
    public Result<Map<String, Object>> masterData() {
        return Result.ok(reimbursementService.masterData());
    }
}
