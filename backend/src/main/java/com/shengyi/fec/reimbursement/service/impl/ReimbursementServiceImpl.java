package com.shengyi.fec.reimbursement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengyi.fec.reimbursement.dto.request.AllocationDTO;
import com.shengyi.fec.reimbursement.dto.request.ItinerarySaveRequest;
import com.shengyi.fec.reimbursement.dto.request.ReimbursementSaveRequest;
import com.shengyi.fec.reimbursement.dto.response.ReimbursementDetailResponse;
import com.shengyi.fec.reimbursement.dto.response.ReimbursementListResponse;
import com.shengyi.fec.reimbursement.entity.ReimbursementAllocationEntity;
import com.shengyi.fec.reimbursement.entity.ReimbursementItineraryEntity;
import com.shengyi.fec.reimbursement.entity.ReimbursementMainEntity;
import com.shengyi.fec.reimbursement.entity.ReimbursementSubsidyEntity;
import com.shengyi.fec.reimbursement.mapper.ReimbursementAllocationMapper;
import com.shengyi.fec.reimbursement.mapper.ReimbursementMainMapper;
import com.shengyi.fec.reimbursement.service.ItineraryService;
import com.shengyi.fec.reimbursement.service.ReimbursementService;
import com.shengyi.fec.reimbursement.service.SubsidyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    @Autowired
    private ReimbursementMainMapper mainMapper;

    @Autowired
    private ReimbursementAllocationMapper allocationMapper;

    @Autowired
    private ItineraryService itineraryService;

    @Autowired
    private SubsidyService subsidyService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional
    public Long save(ReimbursementSaveRequest request) {
        ReimbursementMainEntity main = new ReimbursementMainEntity();
        main.setId(request.getId());
        main.setReimbNo(request.getReimbNo());
        main.setTitle(request.getTitle());
        main.setApplicant(request.getApplicant());
        main.setDepartment(request.getDepartment());
        main.setExpenseCompany(request.getExpenseCompany());
        main.setBusinessType(request.getBusinessType());
        main.setReason(request.getReason());
        main.setRemark(request.getRemark());
        main.setStatus(0);
        main.setUpdateTime(LocalDateTime.now());

        if (main.getId() == null) {
            if (main.getReimbNo() == null || main.getReimbNo().isEmpty()) {
                main.setReimbNo(generateReimbursementNo());
            }
            main.setCreateTime(LocalDateTime.now());
            mainMapper.insert(main);
        } else {
            mainMapper.updateById(main);
            itineraryService.deleteByMainId(main.getId());
            subsidyService.deleteByMainId(main.getId());
            allocationMapper.deleteByMainId(main.getId());
        }

        if (request.getItineraries() != null && !request.getItineraries().isEmpty()) {
            request.getItineraries().forEach(itinerary -> {
                itinerary.setMainId(main.getId());
                itineraryService.save(itinerary);
            });
            subsidyService.generateSubsidies(main.getId());
        }

        if (request.getAllocations() != null && !request.getAllocations().isEmpty()) {
            request.getAllocations().forEach(allocation -> {
                ReimbursementAllocationEntity entity = new ReimbursementAllocationEntity();
                entity.setMainId(main.getId());
                entity.setExpenseType(allocation.getExpenseType());
                entity.setExpenseTypeName(allocation.getExpenseTypeName());
                entity.setProjectCode(allocation.getProjectCode());
                entity.setProjectName(allocation.getProjectName());
                entity.setAllocationRatio(allocation.getAllocationRatio());
                entity.setAllocationAmount(allocation.getAllocationAmount());
                allocationMapper.insert(entity);
            });
        }

        updateTotalAmount(main.getId());
        return main.getId();
    }

    @Override
    @Transactional
    public Long submit(Long id) {
        ReimbursementMainEntity main = mainMapper.selectById(id);
        if (main == null) {
            throw new RuntimeException("报销单不存在");
        }
        main.setStatus(1);
        main.setUpdateTime(LocalDateTime.now());
        mainMapper.updateById(main);
        return id;
    }

    @Override
    public ReimbursementDetailResponse getDetail(Long id) {
        ReimbursementMainEntity main = mainMapper.selectById(id);
        if (main == null) {
            return null;
        }

        ReimbursementDetailResponse response = new ReimbursementDetailResponse();
        response.setId(String.valueOf(main.getId()));
        response.setReimbursementNo(main.getReimbNo());
        response.setDocumentStatusCode(main.getStatus());
        response.setTitle(main.getTitle());
        response.setReason(main.getReason());
        response.setSubmitDate(main.getCreateTime() != null ? main.getCreateTime().format(DATE_FORMATTER) : null);
        response.setReimburserId("user-" + main.getId());
        response.setDepartmentId("dept-" + main.getId());
        response.setCompanyId("comp-" + main.getId());
        response.setBusinessTypeId("biz-" + main.getId());
        response.setRemark(main.getRemark());

        List<ReimbursementItineraryEntity> itineraryEntities = itineraryService.listByMainId(id);
        List<ReimbursementDetailResponse.TripRecord> trips = itineraryEntities.stream().map(it -> {
            ReimbursementDetailResponse.TripRecord trip = new ReimbursementDetailResponse.TripRecord();
            trip.setId("trip-" + it.getId());
            trip.setTravelerId("user-" + it.getId());
            trip.setDepartureCityNo(it.getDepartureCity());
            trip.setArrivalCityNo(it.getDestinationCity());
            trip.setStartDate(it.getStartDate() != null ? it.getStartDate().format(DATE_FORMATTER) : null);
            trip.setEndDate(it.getEndDate() != null ? it.getEndDate().format(DATE_FORMATTER) : null);
            trip.setDescription(it.getDescription());
            return trip;
        }).collect(Collectors.toList());
        response.setTrips(trips);

        List<ReimbursementSubsidyEntity> subsidyEntities = subsidyService.listByMainId(id);
        List<ReimbursementDetailResponse.AllowanceRecord> allowances = subsidyEntities.stream().map(s -> {
            ReimbursementDetailResponse.AllowanceRecord allowance = new ReimbursementDetailResponse.AllowanceRecord();
            allowance.setId("allowance-" + s.getId());
            allowance.setTripId("trip-" + s.getId());
            allowance.setTravelerId("user-" + s.getId());
            allowance.setStartDate(s.getSubsidyDate().format(DATE_FORMATTER));
            allowance.setEndDate(s.getSubsidyDate().format(DATE_FORMATTER));
            allowance.setDays(1);
            allowance.setRouteText(itineraryEntities.isEmpty() ? "" : itineraryEntities.get(0).getRoute());
            allowance.setAllowanceCityNo(String.valueOf(s.getCityLevel()));
            allowance.setApplicationAmount(s.getActualAmount().doubleValue());
            allowance.setAllowanceAmount(s.getStandardAmount().doubleValue());
            return allowance;
        }).collect(Collectors.toList());
        response.setAllowances(allowances);

        List<ReimbursementAllocationEntity> allocationEntities = allocationMapper.selectByMainId(id);
        List<ReimbursementDetailResponse.AllocationRecord> allocations = allocationEntities.stream().map(a -> {
            ReimbursementDetailResponse.AllocationRecord allocation = new ReimbursementDetailResponse.AllocationRecord();
            allocation.setId("alloc-" + a.getId());
            allocation.setCompanyId(a.getExpenseType());
            allocation.setProjectId(a.getProjectCode());
            allocation.setRatio(a.getAllocationRatio().doubleValue());
            allocation.setAmount(a.getAllocationAmount().doubleValue());
            return allocation;
        }).collect(Collectors.toList());
        response.setAllocations(allocations);

        return response;
    }

    @Override
    public IPage<ReimbursementListResponse> getList(Integer pageNum, Integer pageSize, String keyword, Integer status, String applicant) {
        Page<ReimbursementMainEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReimbursementMainEntity> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ReimbursementMainEntity::getReimbNo, keyword)
                   .or()
                   .like(ReimbursementMainEntity::getApplicant, keyword);
        }
        if (status != null) {
            wrapper.eq(ReimbursementMainEntity::getStatus, status);
        }
        if (applicant != null && !applicant.isEmpty()) {
            wrapper.eq(ReimbursementMainEntity::getApplicant, applicant);
        }

        wrapper.orderByDesc(ReimbursementMainEntity::getCreateTime);
        IPage<ReimbursementMainEntity> resultPage = mainMapper.selectPage(page, wrapper);

        return resultPage.convert(main -> {
            ReimbursementListResponse response = new ReimbursementListResponse();
            response.setId(String.valueOf(main.getId()));
            response.setReimbursementNo(main.getReimbNo());
            response.setDocumentStatusCode(main.getStatus());
            response.setDocumentStatusName(getStatusName(main.getStatus()));
            response.setDocumentType("日常报销单");
            
            ReimbursementListResponse.ReimburserDTO reimburser = new ReimbursementListResponse.ReimburserDTO();
            reimburser.setReimburserId("user-" + main.getId());
            reimburser.setReimburserNo("EMP" + String.format("%04d", main.getId()));
            reimburser.setReimburserName(main.getApplicant());
            response.setReimburser(reimburser);

            ReimbursementListResponse.DepartmentDTO department = new ReimbursementListResponse.DepartmentDTO();
            department.setReimDepartmentId("dept-" + main.getId());
            department.setReimDepartmentNo("DEPT" + String.format("%03d", main.getId()));
            department.setReimDepartmentName(main.getDepartment());
            response.setDepartment(department);

            ReimbursementListResponse.CompanyDTO company = new ReimbursementListResponse.CompanyDTO();
            company.setReimCompanyId("comp-" + main.getId());
            company.setReimCompanyNo("COMP" + String.format("%03d", main.getId()));
            company.setReimCompanyName(main.getExpenseCompany());
            response.setCompany(company);

            ReimbursementListResponse.BusinessTypeDTO businessType = new ReimbursementListResponse.BusinessTypeDTO();
            businessType.setBusinessTypeId("biz-" + main.getId());
            businessType.setBusinessTypeNo("BIZ" + String.format("%03d", main.getId()));
            businessType.setBusinessTypeName(main.getBusinessType());
            response.setBusinessType(businessType);

            response.setTitle(main.getTitle());
            response.setReason(main.getReason());
            response.setAllowanceAmount(main.getSubsidyTotal() != null ? main.getSubsidyTotal() : BigDecimal.ZERO);
            response.setCreatedAt(main.getCreateTime() != null ? main.getCreateTime().format(DATE_FORMATTER) : null);
            
            return response;
        });
    }

    private String getStatusName(Integer status) {
        if (status == 0) return "草稿";
        if (status == 1) return "已完成";
        if (status == 2) return "已作废";
        return "未知";
    }

    private void updateTotalAmount(Long mainId) {
        List<ReimbursementSubsidyEntity> subsidies = subsidyService.listByMainId(mainId);
        
        BigDecimal subsidyTotal = subsidies.stream()
                .map(ReimbursementSubsidyEntity::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal mealAllowance = subsidies.stream()
                .filter(s -> "餐补".equals(s.getSubsidyType()) || "伙食补助".equals(s.getSubsidyType()))
                .map(ReimbursementSubsidyEntity::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal transportAllowance = subsidies.stream()
                .filter(s -> "交通补助".equals(s.getSubsidyType()))
                .map(ReimbursementSubsidyEntity::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal communicationAllowance = subsidies.stream()
                .filter(s -> "通讯补助".equals(s.getSubsidyType()))
                .map(ReimbursementSubsidyEntity::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReimbursementMainEntity main = new ReimbursementMainEntity();
        main.setId(mainId);
        main.setTotalAmount(subsidyTotal);
        main.setSubsidyTotal(subsidyTotal);
        main.setMealAllowance(mealAllowance);
        main.setTransportAllowance(transportAllowance);
        main.setCommunicationAllowance(communicationAllowance);
        mainMapper.updateById(main);
    }

    private String generateReimbursementNo() {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new java.util.Random().nextInt(9999));
        return "RCBX" + timestamp + random;
    }
}
