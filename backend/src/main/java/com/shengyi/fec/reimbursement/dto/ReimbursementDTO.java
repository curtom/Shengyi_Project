package com.shengyi.fec.reimbursement.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReimbursementDTO {
    private String id;
    private String reimbursementNo;
    @NotBlank(message = "报销标题不能为空")
    @Size(max = 500, message = "报销标题不能超过500字")
    private String reimbursementTitle;
    @NotBlank(message = "出差事由不能为空")
    @Size(max = 500, message = "出差事由不能超过500字")
    private String businessTripReason;
    @NotBlank(message = "报销人不能为空")
    private String reimburserId;
    private String reimburserNo;
    private String reimburserName;
    @NotBlank(message = "报销部门不能为空")
    private String reimDepartmentId;
    private String reimDepartmentNo;
    private String reimDepartmentName;
    @NotBlank(message = "费用归属公司不能为空")
    private String reimCompanyId;
    private String reimCompanyNo;
    private String reimCompanyName;
    @NotBlank(message = "业务类型不能为空")
    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;
    @Size(max = 1000, message = "备注不能超过1000字")
    private String remarks;
    private String docStatus;
    private String docDate;
    private BigDecimal subsidyTotal;
    private BigDecimal mealAllowance;
    private BigDecimal transportationAllowance;
    private BigDecimal phoneAllowance;
    @Valid
    private List<ItineraryDTO> itineraries = new ArrayList<>();
    @Valid
    private List<SubsidyDTO> subsidies = new ArrayList<>();
    @Valid
    private List<AllocationDTO> allocations = new ArrayList<>();
}
