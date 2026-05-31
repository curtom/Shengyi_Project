package controller;

import common.PageResult;
import common.Result;
import dto.ItineraryDTO;
import dto.ReimbursementDTO;
import dto.ReimbursementQueryReq;
import dto.SubsidyCalendarDTO;
import service.ReimbursementService;
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

    @PostMapping("/delete/{id}")
    public Result<Void> deleteDoc(@PathVariable String id) {
        reimbursementService.deleteDoc(id);
        return Result.ok();
    }

    @PostMapping("/push/{id}")
    public Result<Void> pushDoc(@PathVariable String id) {
        reimbursementService.pushDoc(id);
        return Result.ok();
    }

    @PostMapping("/withdraw/{id}")
    public Result<Void> withdrawDoc(@PathVariable String id) {
        reimbursementService.withdrawDoc(id);
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