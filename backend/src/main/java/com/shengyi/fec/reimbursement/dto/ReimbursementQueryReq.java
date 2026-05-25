package com.shengyi.fec.reimbursement.dto;

import lombok.Data;

@Data
public class ReimbursementQueryReq {
    private Integer current = 1;
    private Integer size = 10;
    private String reimbursementNo;
    private String reimbursementTitle;
    private String businessTripReason;
    private String reimCompanyId;
    private String reimDepartmentId;
    private String reimburserId;
    private String businessTypeId;
    private String docStatus;
}
