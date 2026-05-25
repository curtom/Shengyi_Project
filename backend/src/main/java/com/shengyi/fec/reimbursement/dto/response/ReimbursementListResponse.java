package com.shengyi.fec.reimbursement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReimbursementListResponse {

    private String id;
    private String reimbursementNo;
    private Integer documentStatusCode;
    private String documentStatusName;
    private String documentType;
    private ReimburserDTO reimburser;
    private DepartmentDTO department;
    private CompanyDTO company;
    private BusinessTypeDTO businessType;
    private String title;
    private String reason;
    private BigDecimal allowanceAmount;
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReimbursementNo() { return reimbursementNo; }
    public void setReimbursementNo(String reimbursementNo) { this.reimbursementNo = reimbursementNo; }
    public Integer getDocumentStatusCode() { return documentStatusCode; }
    public void setDocumentStatusCode(Integer documentStatusCode) { this.documentStatusCode = documentStatusCode; }
    public String getDocumentStatusName() { return documentStatusName; }
    public void setDocumentStatusName(String documentStatusName) { this.documentStatusName = documentStatusName; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public ReimburserDTO getReimburser() { return reimburser; }
    public void setReimburser(ReimburserDTO reimburser) { this.reimburser = reimburser; }
    public DepartmentDTO getDepartment() { return department; }
    public void setDepartment(DepartmentDTO department) { this.department = department; }
    public CompanyDTO getCompany() { return company; }
    public void setCompany(CompanyDTO company) { this.company = company; }
    public BusinessTypeDTO getBusinessType() { return businessType; }
    public void setBusinessType(BusinessTypeDTO businessType) { this.businessType = businessType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public BigDecimal getAllowanceAmount() { return allowanceAmount; }
    public void setAllowanceAmount(BigDecimal allowanceAmount) { this.allowanceAmount = allowanceAmount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public static class ReimburserDTO {
        private String reimburserId;
        private String reimburserNo;
        private String reimburserName;

        public String getReimburserId() { return reimburserId; }
        public void setReimburserId(String reimburserId) { this.reimburserId = reimburserId; }
        public String getReimburserNo() { return reimburserNo; }
        public void setReimburserNo(String reimburserNo) { this.reimburserNo = reimburserNo; }
        public String getReimburserName() { return reimburserName; }
        public void setReimburserName(String reimburserName) { this.reimburserName = reimburserName; }
    }

    public static class DepartmentDTO {
        private String reimDepartmentId;
        private String reimDepartmentNo;
        private String reimDepartmentName;

        public String getReimDepartmentId() { return reimDepartmentId; }
        public void setReimDepartmentId(String reimDepartmentId) { this.reimDepartmentId = reimDepartmentId; }
        public String getReimDepartmentNo() { return reimDepartmentNo; }
        public void setReimDepartmentNo(String reimDepartmentNo) { this.reimDepartmentNo = reimDepartmentNo; }
        public String getReimDepartmentName() { return reimDepartmentName; }
        public void setReimDepartmentName(String reimDepartmentName) { this.reimDepartmentName = reimDepartmentName; }
    }

    public static class CompanyDTO {
        private String reimCompanyId;
        private String reimCompanyNo;
        private String reimCompanyName;

        public String getReimCompanyId() { return reimCompanyId; }
        public void setReimCompanyId(String reimCompanyId) { this.reimCompanyId = reimCompanyId; }
        public String getReimCompanyNo() { return reimCompanyNo; }
        public void setReimCompanyNo(String reimCompanyNo) { this.reimCompanyNo = reimCompanyNo; }
        public String getReimCompanyName() { return reimCompanyName; }
        public void setReimCompanyName(String reimCompanyName) { this.reimCompanyName = reimCompanyName; }
    }

    public static class BusinessTypeDTO {
        private String businessTypeId;
        private String businessTypeNo;
        private String businessTypeName;

        public String getBusinessTypeId() { return businessTypeId; }
        public void setBusinessTypeId(String businessTypeId) { this.businessTypeId = businessTypeId; }
        public String getBusinessTypeNo() { return businessTypeNo; }
        public void setBusinessTypeNo(String businessTypeNo) { this.businessTypeNo = businessTypeNo; }
        public String getBusinessTypeName() { return businessTypeName; }
        public void setBusinessTypeName(String businessTypeName) { this.businessTypeName = businessTypeName; }
    }
}
