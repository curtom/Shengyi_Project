
package com.shengyi.fec.reimbursement.dto.request;

import java.util.List;

public class ReimbursementSaveRequest {

    private Long id;
    private String reimbNo;
    private String title;
    private String applicant;
    private String department;
    private String expenseCompany;
    private String businessType;
    private String reason;
    private String remark;
    private List<ItinerarySaveRequest> itineraries;
    private List<ReimbursementSubsidyDTO> subsidies;
    private List<AllocationDTO> allocations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReimbNo() { return reimbNo; }
    public void setReimbNo(String reimbNo) { this.reimbNo = reimbNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getApplicant() { return applicant; }
    public void setApplicant(String applicant) { this.applicant = applicant; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getExpenseCompany() { return expenseCompany; }
    public void setExpenseCompany(String expenseCompany) { this.expenseCompany = expenseCompany; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<ItinerarySaveRequest> getItineraries() { return itineraries; }
    public void setItineraries(List<ItinerarySaveRequest> itineraries) { this.itineraries = itineraries; }
    public List<ReimbursementSubsidyDTO> getSubsidies() { return subsidies; }
    public void setSubsidies(List<ReimbursementSubsidyDTO> subsidies) { this.subsidies = subsidies; }
    public List<AllocationDTO> getAllocations() { return allocations; }
    public void setAllocations(List<AllocationDTO> allocations) { this.allocations = allocations; }
}
