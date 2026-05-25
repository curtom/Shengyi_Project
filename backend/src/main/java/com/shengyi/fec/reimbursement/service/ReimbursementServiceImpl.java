package com.shengyi.fec.reimbursement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengyi.fec.common.BusinessException;
import com.shengyi.fec.common.ErrorCode;
import com.shengyi.fec.common.PageResult;
import com.shengyi.fec.reimbursement.dto.*;
import com.shengyi.fec.reimbursement.entity.*;
import com.shengyi.fec.reimbursement.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReimbursementServiceImpl implements ReimbursementService {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ReimbursementMainMapper mainMapper;
    private final ItineraryMapper itineraryMapper;
    private final SubsidyMapper subsidyMapper;
    private final SubsidyCalendarMapper calendarMapper;
    private final AllocationMapper allocationMapper;
    private final SubsidyGenerator subsidyGenerator;

    @Override
    public PageResult<ReimbursementDTO> pageQuery(ReimbursementQueryReq req) {
        LambdaQueryWrapper<ReimbursementMainEntity> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getReimbursementNo())) {
            w.like(ReimbursementMainEntity::getReimbursementNo, req.getReimbursementNo());
        }
        if (StringUtils.hasText(req.getReimbursementTitle())) {
            w.like(ReimbursementMainEntity::getReimbursementTitle, req.getReimbursementTitle());
        }
        if (StringUtils.hasText(req.getBusinessTripReason())) {
            w.like(ReimbursementMainEntity::getBusinessTripReason, req.getBusinessTripReason());
        }
        if (StringUtils.hasText(req.getReimCompanyId())) {
            w.eq(ReimbursementMainEntity::getReimCompanyId, req.getReimCompanyId());
        }
        if (StringUtils.hasText(req.getReimDepartmentId())) {
            w.eq(ReimbursementMainEntity::getReimDepartmentId, req.getReimDepartmentId());
        }
        if (StringUtils.hasText(req.getReimburserId())) {
            w.eq(ReimbursementMainEntity::getReimburserId, req.getReimburserId());
        }
        if (StringUtils.hasText(req.getBusinessTypeId())) {
            w.eq(ReimbursementMainEntity::getBusinessTypeId, req.getBusinessTypeId());
        }
        if (StringUtils.hasText(req.getDocStatus())) {
            w.eq(ReimbursementMainEntity::getDocStatus, req.getDocStatus());
        }
        w.orderByDesc(ReimbursementMainEntity::getCreationTime);

        Page<ReimbursementMainEntity> page = mainMapper.selectPage(
                new Page<>(req.getCurrent(), req.getSize()), w);

        PageResult<ReimbursementDTO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(page.getRecords().stream().map(this::toListDto).collect(Collectors.toList()));
        return result;
    }

    @Override
    public ReimbursementDTO getDetail(String id) {
        ReimbursementMainEntity main = mainMapper.selectById(id);
        if (main == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "报销单不存在");
        }
        ReimbursementDTO dto = toDetailDto(main);

        List<ItineraryEntity> its = itineraryMapper.selectList(
                new LambdaQueryWrapper<ItineraryEntity>().eq(ItineraryEntity::getMainId, id));
        dto.setItineraries(its.stream().map(this::toItineraryDto).collect(Collectors.toList()));

        List<SubsidyEntity> subs = subsidyMapper.selectList(
                new LambdaQueryWrapper<SubsidyEntity>().eq(SubsidyEntity::getMainId, id));
        List<SubsidyDTO> subsidyDtos = new ArrayList<>();
        for (SubsidyEntity s : subs) {
            SubsidyDTO sd = toSubsidyDto(s);
            List<SubsidyCalendarEntity> cals = calendarMapper.selectList(
                    new LambdaQueryWrapper<SubsidyCalendarEntity>().eq(SubsidyCalendarEntity::getSubsidyId, s.getId()));
            sd.setCalendars(cals.stream().map(this::toCalendarDto).collect(Collectors.toList()));
            subsidyDtos.add(sd);
        }
        dto.setSubsidies(subsidyDtos);

        List<AllocationEntity> allocs = allocationMapper.selectList(
                new LambdaQueryWrapper<AllocationEntity>()
                        .eq(AllocationEntity::getMainId, id)
                        .orderByAsc(AllocationEntity::getSortNo));
        dto.setAllocations(allocs.stream().map(this::toAllocationDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveDraft(ReimbursementDTO dto) {
        validateItineraries(dto.getItineraries(), false);
        if (!CollectionUtils.isEmpty(dto.getItineraries())) {
            dto.setSubsidies(subsidyGenerator.generateFromItineraries(dto.getItineraries()));
        }
        recalcTotals(dto);
        return persist(dto, "0");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submit(ReimbursementDTO dto) {
        if (CollectionUtils.isEmpty(dto.getItineraries())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "请至少补录一条行程");
        }
        validateItineraries(dto.getItineraries(), true);
        dto.setSubsidies(subsidyGenerator.generateFromItineraries(dto.getItineraries()));
        applyCalendarOverrides(dto);
        recalcTotals(dto);
        validateAllocation(dto);
        return persist(dto, "1");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidDoc(String id) {
        ReimbursementMainEntity main = mainMapper.selectById(id);
        if (main == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "报销单不存在");
        }
        main.setDocStatus("2");
        mainMapper.updateById(main);
    }

    @Override
    public List<SubsidyCalendarDTO> previewSubsidyCalendar(List<ItineraryDTO> itineraries) {
        validateItineraries(itineraries, false);
        return subsidyGenerator.generateFromItineraries(itineraries).stream()
                .flatMap(s -> s.getCalendars().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> masterData() {
        Map<String, Object> data = new HashMap<String, Object>();
        List<Map<String, Object>> employees = new ArrayList<Map<String, Object>>();
        employees.add(entry("id", "E001", "no", "10001", "name", "张三", "deptId", "D001", "deptName", "研发中心"));
        employees.add(entry("id", "E002", "no", "10002", "name", "李四", "deptId", "D002", "deptName", "财务部"));
        employees.add(entry("id", "E003", "no", "10003", "name", "王五", "deptId", "D001", "deptName", "研发中心"));
        data.put("employees", employees);

        List<Map<String, Object>> departments = new ArrayList<Map<String, Object>>();
        departments.add(entry("id", "D001", "no", "RD", "name", "研发中心"));
        departments.add(entry("id", "D002", "no", "FIN", "name", "财务部"));
        departments.add(entry("id", "D003", "no", "HR", "name", "人力资源部"));
        data.put("departments", departments);

        List<Map<String, Object>> companies = new ArrayList<Map<String, Object>>();
        companies.add(entry("id", "C001", "no", "SY", "name", "胜意科技股份有限公司"));
        companies.add(entry("id", "C002", "no", "SY-SH", "name", "胜意科技（上海）有限公司"));
        data.put("companies", companies);

        List<Map<String, Object>> businessTypes = new ArrayList<Map<String, Object>>();
        Map<String, Object> bt1 = entry("id", "BT01", "no", "CL", "name", "差旅报销");
        bt1.put("children", new ArrayList<Map<String, Object>>());
        Map<String, Object> bt2 = entry("id", "BT02", "no", "YW", "name", "业务招待");
        List<Map<String, Object>> bt2Children = new ArrayList<Map<String, Object>>();
        bt2Children.add(entry("id", "BT0201", "no", "YW-CL", "name", "出差业务"));
        bt2.put("children", bt2Children);
        businessTypes.add(bt1);
        businessTypes.add(bt2);
        data.put("businessTypes", businessTypes);

        List<Map<String, Object>> projects = new ArrayList<Map<String, Object>>();
        projects.add(entry("id", "P001", "no", "PRJ-001", "name", "费控云一期"));
        projects.add(entry("id", "P002", "no", "PRJ-002", "name", "智慧财务平台"));
        data.put("projects", projects);

        List<Map<String, Object>> cities = new ArrayList<Map<String, Object>>();
        cities.add(entry("no", "110100", "name", "北京"));
        cities.add(entry("no", "310100", "name", "上海"));
        cities.add(entry("no", "440100", "name", "广州"));
        cities.add(entry("no", "440300", "name", "深圳"));
        cities.add(entry("no", "330100", "name", "杭州"));
        cities.add(entry("no", "320100", "name", "南京"));
        cities.add(entry("no", "510100", "name", "成都"));
        cities.add(entry("no", "610100", "name", "西安"));
        data.put("cities", cities);
        return data;
    }

    private Map<String, Object> entry(String... kv) {
        Map<String, Object> m = new HashMap<String, Object>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    private String persist(ReimbursementDTO dto, String status) {
        boolean isNew = !StringUtils.hasText(dto.getId());
        ReimbursementMainEntity main = isNew ? new ReimbursementMainEntity() : mainMapper.selectById(dto.getId());
        if (!isNew && main == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "报销单不存在");
        }
        if (isNew) {
            main = new ReimbursementMainEntity();
            main.setId(UUID.randomUUID().toString().replace("-", ""));
            main.setReimbursementNo("BX" + System.currentTimeMillis());
            main.setCreationTime(LocalDateTime.now().format(DT));
            main.setDocDate(LocalDate.now().format(D));
        }
        copyMain(dto, main, status);
        if (isNew) {
            mainMapper.insert(main);
        } else {
            mainMapper.updateById(main);
            deleteChildren(main.getId());
        }
        String mainId = main.getId();
        saveChildren(mainId, dto);
        return mainId;
    }

    private void deleteChildren(String mainId) {
        itineraryMapper.delete(new LambdaQueryWrapper<ItineraryEntity>().eq(ItineraryEntity::getMainId, mainId));
        List<SubsidyEntity> subs = subsidyMapper.selectList(
                new LambdaQueryWrapper<SubsidyEntity>().eq(SubsidyEntity::getMainId, mainId));
        for (SubsidyEntity s : subs) {
            calendarMapper.delete(new LambdaQueryWrapper<SubsidyCalendarEntity>()
                    .eq(SubsidyCalendarEntity::getSubsidyId, s.getId()));
        }
        subsidyMapper.delete(new LambdaQueryWrapper<SubsidyEntity>().eq(SubsidyEntity::getMainId, mainId));
        allocationMapper.delete(new LambdaQueryWrapper<AllocationEntity>().eq(AllocationEntity::getMainId, mainId));
    }

    private void saveChildren(String mainId, ReimbursementDTO dto) {
        Map<String, String> itineraryIdMap = new HashMap<>();
        for (ItineraryDTO it : dto.getItineraries()) {
            ItineraryEntity e = new ItineraryEntity();
            BeanUtils.copyProperties(it, e);
            e.setId(UUID.randomUUID().toString().replace("-", ""));
            e.setMainId(mainId);
            itineraryMapper.insert(e);
            itineraryIdMap.put(it.getId() != null ? it.getId() : it.getTravelerId() + it.getDepartureDate(), e.getId());
            if (it.getId() == null) {
                it.setId(e.getId());
            }
        }

        for (SubsidyDTO sub : dto.getSubsidies()) {
            SubsidyEntity se = new SubsidyEntity();
            BeanUtils.copyProperties(sub, se);
            se.setId(UUID.randomUUID().toString().replace("-", ""));
            se.setMainId(mainId);
            subsidyMapper.insert(se);

            if (!CollectionUtils.isEmpty(sub.getCalendars())) {
                for (SubsidyCalendarDTO cal : sub.getCalendars()) {
                    SubsidyCalendarEntity ce = new SubsidyCalendarEntity();
                    BeanUtils.copyProperties(cal, ce);
                    ce.setId(UUID.randomUUID().toString().replace("-", ""));
                    ce.setMainId(mainId);
                    ce.setSubsidyId(se.getId());
                    ce.setMealSelected(Boolean.TRUE.equals(cal.getMealSelected()) ? "1" : "0");
                    ce.setTrafficSelected(Boolean.TRUE.equals(cal.getTrafficSelected()) ? "1" : "0");
                    ce.setCommunicationSelected(Boolean.TRUE.equals(cal.getCommunicationSelected()) ? "1" : "0");
                    ce.setIsReimbursed(Boolean.TRUE.equals(cal.getIsReimbursed()) ? "1" : "0");
                    calendarMapper.insert(ce);
                }
            }
        }

        int sort = 0;
        for (AllocationDTO a : dto.getAllocations()) {
            AllocationEntity ae = new AllocationEntity();
            BeanUtils.copyProperties(a, ae);
            ae.setId(UUID.randomUUID().toString().replace("-", ""));
            ae.setMainId(mainId);
            ae.setSortNo(sort++);
            allocationMapper.insert(ae);
        }
    }

    private void validateItineraries(List<ItineraryDTO> list, boolean strict) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        LocalDate today = LocalDate.now();
        for (ItineraryDTO it : list) {
            LocalDate dep = LocalDate.parse(it.getDepartureDate(), D);
            LocalDate arr = LocalDate.parse(it.getArrivalDate(), D);
            if (arr.isBefore(dep) || arr.isAfter(today)) {
                throw new BusinessException(ErrorCode.DATE_INVALID);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            ItineraryDTO a = list.get(i);
            LocalDate aStart = LocalDate.parse(a.getDepartureDate(), D);
            LocalDate aEnd = LocalDate.parse(a.getArrivalDate(), D);
            for (int j = i + 1; j < list.size(); j++) {
                ItineraryDTO b = list.get(j);
                if (!a.getTravelerId().equals(b.getTravelerId())) {
                    continue;
                }
                LocalDate bStart = LocalDate.parse(b.getDepartureDate(), D);
                LocalDate bEnd = LocalDate.parse(b.getArrivalDate(), D);
                if (!(aEnd.isBefore(bStart) || bEnd.isBefore(aStart))) {
                    throw new BusinessException(ErrorCode.ITINERARY_DATE_DUP);
                }
            }
        }
    }

    private void validateAllocation(ReimbursementDTO dto) {
        if (CollectionUtils.isEmpty(dto.getAllocations())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "请至少维护一条费用分摊");
        }
        BigDecimal totalRatio = dto.getAllocations().stream()
                .map(a -> a.getAllocationRatio() != null ? a.getAllocationRatio() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalRatio.setScale(4, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException(ErrorCode.ALLOCATION_RATIO);
        }
        BigDecimal totalAmt = dto.getAllocations().stream()
                .map(a -> a.getAllocationAmount() != null ? a.getAllocationAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subsidy = dto.getSubsidyTotal() != null ? dto.getSubsidyTotal() : BigDecimal.ZERO;
        if (totalAmt.setScale(2, RoundingMode.HALF_UP).compareTo(subsidy.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new BusinessException(ErrorCode.ALLOCATION_AMOUNT);
        }
    }

    private void recalcTotals(ReimbursementDTO dto) {
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal meal = BigDecimal.ZERO;
        BigDecimal traffic = BigDecimal.ZERO;
        BigDecimal phone = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(dto.getSubsidies())) {
            for (SubsidyDTO s : dto.getSubsidies()) {
                subTotal = subTotal.add(nvl(s.getSubsidyAmount()));
                meal = meal.add(nvl(s.getMealAllowance()));
                traffic = traffic.add(nvl(s.getTransportationAllowance()));
                phone = phone.add(nvl(s.getPhoneAllowance()));
            }
        }
        dto.setSubsidyTotal(subTotal);
        dto.setMealAllowance(meal);
        dto.setTransportationAllowance(traffic);
        dto.setPhoneAllowance(phone);
    }

    private void applyCalendarOverrides(ReimbursementDTO dto) {
        for (SubsidyDTO sub : dto.getSubsidies()) {
            BigDecimal app = BigDecimal.ZERO;
            BigDecimal subAmt = BigDecimal.ZERO;
            BigDecimal meal = BigDecimal.ZERO;
            BigDecimal traffic = BigDecimal.ZERO;
            BigDecimal phone = BigDecimal.ZERO;
            if (CollectionUtils.isEmpty(sub.getCalendars())) {
                continue;
            }
            for (SubsidyCalendarDTO cal : sub.getCalendars()) {
                BigDecimal mealStd = nvl(cal.getStandardMealExpensesAmount());
                BigDecimal trafficStd = nvl(cal.getStandardTrafficAmount());
                BigDecimal commStd = nvl(cal.getStandardCommunicationAmount());
                if (Boolean.TRUE.equals(cal.getMealSelected())) {
                    BigDecimal m = nvl(cal.getMealExpensesAmount());
                    if (m.compareTo(mealStd) > 0) {
                        throw new BusinessException(ErrorCode.SUBSIDY_OVER_STANDARD);
                    }
                    app = app.add(mealStd);
                    subAmt = subAmt.add(m);
                    meal = meal.add(m);
                } else {
                    cal.setMealExpensesAmount(BigDecimal.ZERO);
                }
                if (Boolean.TRUE.equals(cal.getTrafficSelected())) {
                    BigDecimal t = nvl(cal.getTrafficAmount());
                    if (t.compareTo(trafficStd) > 0) {
                        throw new BusinessException(ErrorCode.SUBSIDY_OVER_STANDARD);
                    }
                    app = app.add(trafficStd);
                    subAmt = subAmt.add(t);
                    traffic = traffic.add(t);
                } else {
                    cal.setTrafficAmount(BigDecimal.ZERO);
                }
                if (Boolean.TRUE.equals(cal.getCommunicationSelected())) {
                    BigDecimal c = nvl(cal.getCommunicationAmount());
                    if (c.compareTo(commStd) > 0) {
                        throw new BusinessException(ErrorCode.SUBSIDY_OVER_STANDARD);
                    }
                    app = app.add(commStd);
                    subAmt = subAmt.add(c);
                    phone = phone.add(c);
                } else {
                    cal.setCommunicationAmount(BigDecimal.ZERO);
                }
            }
            sub.setApplicationAmount(app);
            sub.setSubsidyAmount(subAmt);
            sub.setMealAllowance(meal);
            sub.setTransportationAllowance(traffic);
            sub.setPhoneAllowance(phone);
        }
    }

    private void copyMain(ReimbursementDTO dto, ReimbursementMainEntity main, String status) {
        main.setReimbursementTitle(dto.getReimbursementTitle());
        main.setBusinessTripReason(dto.getBusinessTripReason());
        main.setReimburserId(dto.getReimburserId());
        main.setReimburserNo(dto.getReimburserNo());
        main.setReimburserName(dto.getReimburserName());
        main.setReimDepartmentId(dto.getReimDepartmentId());
        main.setReimDepartmentNo(dto.getReimDepartmentNo());
        main.setReimDepartmentName(dto.getReimDepartmentName());
        main.setReimCompanyId(dto.getReimCompanyId());
        main.setReimCompanyNo(dto.getReimCompanyNo());
        main.setReimCompanyName(dto.getReimCompanyName());
        main.setBusinessTypeId(dto.getBusinessTypeId());
        main.setBusinessTypeNo(dto.getBusinessTypeNo());
        main.setBusinessTypeName(dto.getBusinessTypeName());
        main.setRemarks(dto.getRemarks());
        main.setSubsidyTotal(dto.getSubsidyTotal());
        main.setMealAllowance(dto.getMealAllowance());
        main.setTransportationAllowance(dto.getTransportationAllowance());
        main.setPhoneAllowance(dto.getPhoneAllowance());
        main.setDocStatus(status);
        if (!StringUtils.hasText(main.getDocDate())) {
            main.setDocDate(dto.getDocDate() != null ? dto.getDocDate() : LocalDate.now().format(D));
        }
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private ReimbursementDTO toListDto(ReimbursementMainEntity e) {
        ReimbursementDTO d = new ReimbursementDTO();
        BeanUtils.copyProperties(e, d);
        return d;
    }

    private ReimbursementDTO toDetailDto(ReimbursementMainEntity e) {
        ReimbursementDTO d = new ReimbursementDTO();
        BeanUtils.copyProperties(e, d);
        return d;
    }

    private ItineraryDTO toItineraryDto(ItineraryEntity e) {
        ItineraryDTO d = new ItineraryDTO();
        BeanUtils.copyProperties(e, d);
        return d;
    }

    private SubsidyDTO toSubsidyDto(SubsidyEntity e) {
        SubsidyDTO d = new SubsidyDTO();
        BeanUtils.copyProperties(e, d);
        return d;
    }

    private SubsidyCalendarDTO toCalendarDto(SubsidyCalendarEntity e) {
        SubsidyCalendarDTO d = new SubsidyCalendarDTO();
        BeanUtils.copyProperties(e, d);
        d.setMealSelected("1".equals(e.getMealSelected()));
        d.setTrafficSelected("1".equals(e.getTrafficSelected()));
        d.setCommunicationSelected("1".equals(e.getCommunicationSelected()));
        d.setIsReimbursed("1".equals(e.getIsReimbursed()));
        return d;
    }

    private AllocationDTO toAllocationDto(AllocationEntity e) {
        AllocationDTO d = new AllocationDTO();
        BeanUtils.copyProperties(e, d);
        return d;
    }
}
