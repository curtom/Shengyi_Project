package dto;

import lombok.Data;

@Data
public class ReimbursementQueryReq {
    private Integer current = 1;
    private Integer size = 10;
    private String reimbursementNo;
    private String title;
    private String reason;
    private String companyId;
    private String departmentId;
    private String reimburserId;
    private String businessTypeId;
}