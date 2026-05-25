package com.shengyi.fec.reimbursement.dto.response;

import java.util.List;

public class ReimbursementDetailResponse {

    private String id;
    private String reimbursementNo;
    private Integer documentStatusCode;
    private String title;
    private String reason;
    private String submitDate;
    private String reimburserId;
    private String departmentId;
    private String companyId;
    private String businessTypeId;
    private List<TripRecord> trips;
    private List<AllowanceRecord> allowances;
    private List<AllocationRecord> allocations;
    private String remark;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReimbursementNo() { return reimbursementNo; }
    public void setReimbursementNo(String reimbursementNo) { this.reimbursementNo = reimbursementNo; }
    public Integer getDocumentStatusCode() { return documentStatusCode; }
    public void setDocumentStatusCode(Integer documentStatusCode) { this.documentStatusCode = documentStatusCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getSubmitDate() { return submitDate; }
    public void setSubmitDate(String submitDate) { this.submitDate = submitDate; }
    public String getReimburserId() { return reimburserId; }
    public void setReimburserId(String reimburserId) { this.reimburserId = reimburserId; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getBusinessTypeId() { return businessTypeId; }
    public void setBusinessTypeId(String businessTypeId) { this.businessTypeId = businessTypeId; }
    public List<TripRecord> getTrips() { return trips; }
    public void setTrips(List<TripRecord> trips) { this.trips = trips; }
    public List<AllowanceRecord> getAllowances() { return allowances; }
    public void setAllowances(List<AllowanceRecord> allowances) { this.allowances = allowances; }
    public List<AllocationRecord> getAllocations() { return allocations; }
    public void setAllocations(List<AllocationRecord> allocations) { this.allocations = allocations; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public static class TripRecord {
        private String id;
        private String travelerId;
        private String departureCityNo;
        private String arrivalCityNo;
        private String startDate;
        private String endDate;
        private String description;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTravelerId() { return travelerId; }
        public void setTravelerId(String travelerId) { this.travelerId = travelerId; }
        public String getDepartureCityNo() { return departureCityNo; }
        public void setDepartureCityNo(String departureCityNo) { this.departureCityNo = departureCityNo; }
        public String getArrivalCityNo() { return arrivalCityNo; }
        public void setArrivalCityNo(String arrivalCityNo) { this.arrivalCityNo = arrivalCityNo; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class AllowanceRecord {
        private String id;
        private String tripId;
        private String travelerId;
        private String startDate;
        private String endDate;
        private Integer days;
        private String routeText;
        private String allowanceCityNo;
        private Double applicationAmount;
        private Double allowanceAmount;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTripId() { return tripId; }
        public void setTripId(String tripId) { this.tripId = tripId; }
        public String getTravelerId() { return travelerId; }
        public void setTravelerId(String travelerId) { this.travelerId = travelerId; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public Integer getDays() { return days; }
        public void setDays(Integer days) { this.days = days; }
        public String getRouteText() { return routeText; }
        public void setRouteText(String routeText) { this.routeText = routeText; }
        public String getAllowanceCityNo() { return allowanceCityNo; }
        public void setAllowanceCityNo(String allowanceCityNo) { this.allowanceCityNo = allowanceCityNo; }
        public Double getApplicationAmount() { return applicationAmount; }
        public void setApplicationAmount(Double applicationAmount) { this.applicationAmount = applicationAmount; }
        public Double getAllowanceAmount() { return allowanceAmount; }
        public void setAllowanceAmount(Double allowanceAmount) { this.allowanceAmount = allowanceAmount; }
    }

    public static class AllocationRecord {
        private String id;
        private String companyId;
        private String projectId;
        private Double ratio;
        private Double amount;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getCompanyId() { return companyId; }
        public void setCompanyId(String companyId) { this.companyId = companyId; }
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        public Double getRatio() { return ratio; }
        public void setRatio(Double ratio) { this.ratio = ratio; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }
}
