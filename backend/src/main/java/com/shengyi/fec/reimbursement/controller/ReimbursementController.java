package com.shengyi.fec.reimbursement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shengyi.fec.reimbursement.common.ErrorCode;
import com.shengyi.fec.reimbursement.common.PageParam;
import com.shengyi.fec.reimbursement.common.Result;
import com.shengyi.fec.reimbursement.dto.request.ReimbursementSaveRequest;
import com.shengyi.fec.reimbursement.dto.response.ReimbursementDetailResponse;
import com.shengyi.fec.reimbursement.dto.response.ReimbursementListResponse;
import com.shengyi.fec.reimbursement.service.ReimbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fccapi")
public class ReimbursementController {

    @Autowired
    private ReimbursementService reimbursementService;

    @GetMapping("/COMM_B2G_QueryReimbursementList")
    public Result<List<ReimbursementListResponse>> queryList(PageParam param) {
        IPage<ReimbursementListResponse> page = reimbursementService.getList(
                param.getPageNum(),
                param.getPageSize(),
                param.getKeyword(),
                param.getStatus(),
                param.getApplicant()
        );
        return Result.success(page.getRecords(), page.getTotal());
    }

    @PostMapping("/COMM_B2G_QueryReimbursementList")
    public Result<List<ReimbursementListResponse>> queryListPost(@RequestBody PageParam param) {
        IPage<ReimbursementListResponse> page = reimbursementService.getList(
                param.getPageNum() != null ? param.getPageNum() : 1,
                param.getPageSize() != null ? param.getPageSize() : 10,
                param.getKeyword(),
                param.getStatus(),
                param.getApplicant()
        );
        return Result.success(page.getRecords(), page.getTotal());
    }

    @GetMapping("/COMM_B2G_QueryReimbursementDetail/{id}")
    public Result<ReimbursementDetailResponse> detail(@PathVariable Long id) {
        ReimbursementDetailResponse detail = reimbursementService.getDetail(id);
        if (detail == null) {
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.success(detail);
    }

    @PostMapping("/COMM_B2G_SaveReimbursement")
    public Result<Long> save(@RequestBody ReimbursementSaveRequest request) {
        Long id = reimbursementService.save(request);
        return Result.success(id);
    }

    @PostMapping("/COMM_B2G_SubmitReimbursement/{id}")
    public Result<Long> submit(@PathVariable Long id) {
        Long result = reimbursementService.submit(id);
        if (result == null) {
            return Result.error(ErrorCode.SUBMIT_FAILED);
        }
        return Result.success(result);
    }

    @GetMapping("/COMM_B2G_QueryAcceptanceBillPageList")
    public Result<Map<String, Object>> queryAcceptanceBillPageListGet(@RequestParam(required = false) Map<String, Object> request) {
        return queryAcceptanceBillPageList(request != null ? request : new HashMap<>());
    }

    @PostMapping("/COMM_B2G_QueryAcceptanceBillPageList")
    public Result<Map<String, Object>> queryAcceptanceBillPageList(@RequestBody Map<String, Object> request) {
        Integer current = (Integer) request.getOrDefault("current", 1);
        Integer size = (Integer) request.getOrDefault("size", 10);
        
        IPage<ReimbursementListResponse> page = reimbursementService.getList(
                current, size, null, null, null
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (ReimbursementListResponse item : page.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", item.getId());
            record.put("ticketNo", item.getReimbursementNo());
            record.put("drawerName", item.getReimburser() != null ? item.getReimburser().getReimburserName() : "");
            record.put("faceAmount", item.getAllowanceAmount() != null ? item.getAllowanceAmount().toString() : "0");
            record.put("acceptStatus", item.getDocumentStatusCode());
            record.put("dueDateStr", item.getCreatedAt());
            records.add(record);
        }
        data.put("records", records);
        
        Map<String, Object> condition = new HashMap<>();
        Map<String, String> total = new HashMap<>();
        total.put("faceAmount", "0");
        total.put("balance", "0");
        total.put("underAcceptanceAmount", "0");
        total.put("acceptedAmount", "0");
        condition.put("total", total);
        condition.put("subTotal", total);
        data.put("condition", condition);
        
        return Result.success(data);
    }
}
