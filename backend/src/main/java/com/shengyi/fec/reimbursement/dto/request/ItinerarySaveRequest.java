
package com.shengyi.fec.reimbursement.dto.request;

import java.time.LocalDate;

public class ItinerarySaveRequest {

    private Long id;
    private Long mainId;
    private String traveler;
    private String travelDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String departureCity;
    private String destinationCity;
    private String route;
    private Integer cityLevel;
    private String transportType;
    private String remark;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMainId() { return mainId; }
    public void setMainId(Long mainId) { this.mainId = mainId; }
    public String getTraveler() { return traveler; }
    public void setTraveler(String traveler) { this.traveler = traveler; }
    public String getTravelDate() { return travelDate; }
    public void setTravelDate(String travelDate) { this.travelDate = travelDate; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }
    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public Integer getCityLevel() { return cityLevel; }
    public void setCityLevel(Integer cityLevel) { this.cityLevel = cityLevel; }
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
