package service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.BusinessException;
import common.ErrorCode;
import common.PageResult;
import dto.*;
import entity.*;
import mapper.*;
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
    private static final String[] WEEKDAYS = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

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
        if (StringUtils.hasText(req.getTitle())) {
            w.like(ReimbursementMainEntity::getReimbursementTitle, req.getTitle());
        }
        if (StringUtils.hasText(req.getReason())) {
            w.like(ReimbursementMainEntity::getBusinessTripReason, req.getReason());
        }
        if (StringUtils.hasText(req.getCompanyId())) {
            w.eq(ReimbursementMainEntity::getReimCompanyId, req.getCompanyId());
        }
        if (StringUtils.hasText(req.getDepartmentId())) {
            w.eq(ReimbursementMainEntity::getReimDepartmentId, req.getDepartmentId());
        }
        if (StringUtils.hasText(req.getReimburserId())) {
            w.eq(ReimbursementMainEntity::getReimburserId, req.getReimburserId());
        }
        if (StringUtils.hasText(req.getBusinessTypeId())) {
            w.eq(ReimbursementMainEntity::getBusinessTypeId, req.getBusinessTypeId());
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
        dto.setTrips(its.stream().map(this::toTripDto).collect(Collectors.toList()));

        List<SubsidyEntity> subs = subsidyMapper.selectList(
                new LambdaQueryWrapper<SubsidyEntity>().eq(SubsidyEntity::getMainId, id));
        List<SubsidyDTO> allowanceDtos = new ArrayList<>();
        for (SubsidyEntity s : subs) {
            SubsidyDTO sd = toAllowanceDto(s);
            List<SubsidyCalendarEntity> cals = calendarMapper.selectList(
                    new LambdaQueryWrapper<SubsidyCalendarEntity>().eq(SubsidyCalendarEntity::getSubsidyId, s.getId()));
            sd.setCalendar(cals.stream().map(this::toCalendarDto).collect(Collectors.toList()));
            allowanceDtos.add(sd);
        }
        dto.setAllowances(allowanceDtos);

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
        validateItineraries(dto.getTrips(), false);
        if (!CollectionUtils.isEmpty(dto.getTrips())) {
            dto.setAllowances(subsidyGenerator.generateFromItineraries(dto.getTrips()));
        }
        recalcTotals(dto);
        return persist(dto, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submit(ReimbursementDTO dto) {
        if (CollectionUtils.isEmpty(dto.getTrips())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "请至少补录一条行程");
        }
        validateItineraries(dto.getTrips(), true);
        if (CollectionUtils.isEmpty(dto.getAllowances())) {
            dto.setAllowances(subsidyGenerator.generateFromItineraries(dto.getTrips()));
        }
        applyCalendarOverrides(dto);
        recalcTotals(dto);
        validateAllocation(dto);
        return persist(dto, 1);
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
                .flatMap(s -> s.getCalendar().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> masterData() {
        Map<String, Object> data = new HashMap<>();

        List<Map<String, Object>> reimCompanyOptions = new ArrayList<>();
        reimCompanyOptions.add(entry("reimCompanyId", "1C54557F1782E000", "reimCompanyNo", "0407", "reimCompanyName", "胜意科技北京分公司"));
        reimCompanyOptions.add(entry("reimCompanyId", "19218A262C976000", "reimCompanyNo", "0408", "reimCompanyName", "胜意科技上海分公司"));
        reimCompanyOptions.add(entry("reimCompanyId", "1C61686865DA8000", "reimCompanyNo", "0409", "reimCompanyName", "胜意科技武汉分公司"));
        reimCompanyOptions.add(entry("reimCompanyId", "1717271D1DA15000", "reimCompanyNo", "0410", "reimCompanyName", "胜意科技杭州分公司"));
        reimCompanyOptions.add(entry("reimCompanyId", "16AE93CC7EF92002", "reimCompanyNo", "0411", "reimCompanyName", "胜意科技荆州分公司"));
        data.put("reimCompanyOptions", reimCompanyOptions);

        List<Map<String, Object>> reimDepartmentOptions = new ArrayList<>();
        reimDepartmentOptions.add(entry("reimDepartmentId", "13AB8D7B52A9B002", "reimDepartmentNo", "072001", "reimDepartmentName", "客户成功事业部"));
        reimDepartmentOptions.add(entry("reimDepartmentId", "13BFD31C6029A002", "reimDepartmentNo", "072002", "reimDepartmentName", "企业消费事业部"));
        reimDepartmentOptions.add(entry("reimDepartmentId", "14515BB4BFB92003", "reimDepartmentNo", "072003", "reimDepartmentName", "企业费控事业部"));
        reimDepartmentOptions.add(entry("reimDepartmentId", "19206611C47A6000", "reimDepartmentNo", "072004", "reimDepartmentName", "集采事业部"));
        reimDepartmentOptions.add(entry("reimDepartmentId", "19D32F9FE9647000", "reimDepartmentNo", "072005", "reimDepartmentName", "航旅事业部"));
        reimDepartmentOptions.add(entry("reimDepartmentId", "13C7E2BAE0393001", "reimDepartmentNo", "072006", "reimDepartmentName", "运营事业部"));
        reimDepartmentOptions.add(entry("reimDepartmentId", "14055D22BB808001", "reimDepartmentNo", "072007", "reimDepartmentName", "营销事业部"));
        data.put("reimDepartmentOptions", reimDepartmentOptions);

        List<Map<String, Object>> reimburserOptions = new ArrayList<>();
        reimburserOptions.add(entry("reimburserId", "13AB3A3F72409002", "reimburserNo", "74541", "reimburserName", "徐年年"));
        reimburserOptions.add(entry("reimburserId", "13AB498CC6409002", "reimburserNo", "74008", "reimburserName", "郑雨雪"));
        reimburserOptions.add(entry("reimburserId", "13AB4A56BB009002", "reimburserNo", "21552", "reimburserName", "邹薇"));
        reimburserOptions.add(entry("reimburserId", "13AB591FE8009002", "reimburserNo", "80681", "reimburserName", "王成军"));
        reimburserOptions.add(entry("reimburserId", "13AB77281A408001", "reimburserNo", "89899", "reimburserName", "潘展飞"));
        reimburserOptions.add(entry("reimburserId", "13AB7925EB808001", "reimburserNo", "10503", "reimburserName", "姜林"));
        data.put("reimburserOptions", reimburserOptions);

        List<Map<String, Object>> businessTypeOptions = new ArrayList<>();
        businessTypeOptions.add(entry("businessTypeId", "18F0916A8C2C4000", "businessTypeNo", "1001001", "businessTypeName", "员工差旅活动", "thereSubordinateNode", "1", "superiorId", "none"));
        businessTypeOptions.add(entry("businessTypeId", "18F091913EEC4000", "businessTypeNo", "100100101", "businessTypeName", "境内出差", "thereSubordinateNode", "1", "superiorId", "18F0916A8C2C4000"));
        businessTypeOptions.add(entry("businessTypeId", "1B5FEB7DD4396000", "businessTypeNo", "10010010101", "businessTypeName", "项目出差", "thereSubordinateNode", "0", "superiorId", "18F091913EEC4000"));
        businessTypeOptions.add(entry("businessTypeId", "1A92E43082EFC000", "businessTypeNo", "10010010102", "businessTypeName", "市场拓展出差", "thereSubordinateNode", "0", "superiorId", "18F091913EEC4000"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A4138008001", "businessTypeNo", "100100102", "businessTypeName", "境外出差", "thereSubordinateNode", "1", "superiorId", "18F0916A8C2C4000"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A4248008002", "businessTypeNo", "10010010201", "businessTypeName", "国外考察", "thereSubordinateNode", "0", "superiorId", "13AB3A4138008001"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A4154008001", "businessTypeNo", "10010010202", "businessTypeName", "售后维护出差", "thereSubordinateNode", "0", "superiorId", "13AB3A4138008001"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A4172008001", "businessTypeNo", "1001002", "businessTypeName", "人力资源", "thereSubordinateNode", "1", "superiorId", "none"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A418F808001", "businessTypeNo", "100100201", "businessTypeName", "个人团队培训", "thereSubordinateNode", "0", "superiorId", "13AB3A4172008001"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A41AC408001", "businessTypeNo", "100100202", "businessTypeName", "招聘会", "thereSubordinateNode", "0", "superiorId", "13AB3A4172008001"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A41CD808002", "businessTypeNo", "1001003", "businessTypeName", "员工福利", "thereSubordinateNode", "1", "superiorId", "none"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A41ED408002", "businessTypeNo", "100100301", "businessTypeName", "员工旅游", "thereSubordinateNode", "0", "superiorId", "13AB3A41CD808002"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A420CC08002", "businessTypeNo", "100100302", "businessTypeName", "员工团建", "thereSubordinateNode", "0", "superiorId", "13AB3A41CD808002"));
        businessTypeOptions.add(entry("businessTypeId", "13AB3A422A808001", "businessTypeNo", "100100303", "businessTypeName", "员工体检", "thereSubordinateNode", "0", "superiorId", "13AB3A41CD808002"));
        data.put("businessTypeOptions", businessTypeOptions);

        List<Map<String, Object>> businessTypeTreeOptions = buildBusinessTypeTree(businessTypeOptions);
        data.put("businessTypeTreeOptions", businessTypeTreeOptions);

        List<Map<String, Object>> cityOptions = new ArrayList<>();
        cityOptions.add(entry("cityNo", "10119", "cityName", "北京", "cityType", "1"));
        cityOptions.add(entry("cityNo", "10621", "cityName", "上海", "cityType", "1"));
        cityOptions.add(entry("cityNo", "10458", "cityName", "武汉", "cityType", "2"));
        cityOptions.add(entry("cityNo", "10216", "cityName", "杭州", "cityType", "2"));
        cityOptions.add(entry("cityNo", "10455", "cityName", "荆州", "cityType", "3"));
        data.put("cityOptions", cityOptions);

        List<Map<String, Object>> projectOptions = new ArrayList<>();
        projectOptions.add(entry("projectId", "12BC248B25083001", "projectNo", "nonProjectRelated", "projectName", "非项目类费用归集"));
        projectOptions.add(entry("projectId", "1C811ABF96195000", "projectNo", "centralChina", "projectName", "华中客户定制化项目"));
        projectOptions.add(entry("projectId", "1C5931735AC4A000", "projectNo", "southChina", "projectName", "华南客户定制化项目"));
        projectOptions.add(entry("projectId", "1771EC45F2443000", "projectNo", "northChina", "projectName", "华北客户定制化项目"));
        projectOptions.add(entry("projectId", "1762792DB4E9A002", "projectNo", "eastChina", "projectName", "华东客户定制化项目"));
        projectOptions.add(entry("projectId", "17071065FC29A002", "projectNo", "southWest", "projectName", "西南客户定制化项目"));
        projectOptions.add(entry("projectId", "162664EBE9ABE001", "projectNo", "northWest", "projectName", "西北客户定制化项目"));
        projectOptions.add(entry("projectId", "162664B8526BE002", "projectNo", "northEast", "projectName", "东北客户定制化项目"));
        data.put("projectOptions", projectOptions);

        return data;
    }

    private List<Map<String, Object>> buildBusinessTypeTree(List<Map<String, Object>> items) {
        Map<String, Map<String, Object>> nodeMap = new HashMap<>();

        for (Map<String, Object> item : items) {
            Map<String, Object> node = new HashMap<>(item);
            node.put("label", item.get("businessTypeName"));
            node.put("value", item.get("businessTypeId"));
            node.put("children", new ArrayList<>());
            nodeMap.put((String) item.get("businessTypeId"), node);
        }

        List<Map<String, Object>> roots = new ArrayList<>();

        for (Map<String, Object> node : nodeMap.values()) {
            String superiorId = (String) node.get("superiorId");
            if ("none".equals(superiorId)) {
                roots.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(superiorId);
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(node);
                }
            }
        }

        return pruneEmptyChildren(roots);
    }

    private List<Map<String, Object>> pruneEmptyChildren(List<Map<String, Object>> nodes) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> node : nodes) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children == null || children.isEmpty()) {
                Map<String, Object> leaf = new HashMap<>(node);
                leaf.remove("children");
                result.add(leaf);
            } else {
                Map<String, Object> parent = new HashMap<>(node);
                parent.put("children", pruneEmptyChildren(children));
                result.add(parent);
            }
        }
        return result;
    }

    private Map<String, Object> entry(String... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    private String persist(ReimbursementDTO dto, int status) {
        boolean isNew = !StringUtils.hasText(dto.getId());
        ReimbursementMainEntity main = isNew ? new ReimbursementMainEntity() : mainMapper.selectById(dto.getId());
        if (!isNew && main == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "报销单不存在");
        }
        if (isNew) {
            main = new ReimbursementMainEntity();
            main.setId(UUID.randomUUID().toString().replace("-", ""));
            main.setReimbursementNo("RCBX" + System.currentTimeMillis());
            main.setCreationTime(LocalDateTime.now().format(DT));
            main.setDocDate(LocalDate.now().format(D));
        }
        resolveNames(dto);
        copyMain(dto, main, String.valueOf(status));
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
        for (ItineraryDTO it : dto.getTrips()) {
            ItineraryEntity e = new ItineraryEntity();
            e.setId(it.getId() != null ? it.getId() : UUID.randomUUID().toString().replace("-", ""));
            e.setMainId(mainId);
            e.setTravelerId(it.getTravelerId());
            e.setTravelerNo(it.getTravelerNo());
            e.setTravelerName(it.getTravelerName());
            e.setDepartureDate(it.getStartDate());
            e.setArrivalDate(it.getEndDate());
            e.setDepartureCity(it.getDepartureCityNo());
            e.setDepartureCityNo(it.getDepartureCityNo());
            e.setArrivingCity(it.getArrivalCityNo());
            e.setArrivingCityNo(it.getArrivalCityNo());
            e.setItineraryInstructions(it.getDescription());
            itineraryMapper.insert(e);
            itineraryIdMap.put(it.getId() != null ? it.getId() : it.getTravelerId() + it.getStartDate(), e.getId());
            if (it.getId() == null) {
                it.setId(e.getId());
            }
        }

        for (SubsidyDTO sub : dto.getAllowances()) {
            SubsidyEntity se = new SubsidyEntity();
            se.setId(UUID.randomUUID().toString().replace("-", ""));
            se.setMainId(mainId);
            se.setItineraryId(sub.getTripId());
            se.setTravelerId(sub.getTravelerId());
            se.setTravelerNo(sub.getTravelerNo());
            se.setTravelerName(sub.getTravelerName());
            se.setDepartureDate(sub.getStartDate());
            se.setArrivalDate(sub.getEndDate());
            se.setSubsidyDays(sub.getDays());
            se.setDepartureCity("");
            se.setDepartureCityNo(sub.getAllowanceCityNo());
            se.setArrivingCity("");
            se.setArrivingCityNo(sub.getAllowanceCityNo());
            se.setApplicationAmount(sub.getApplicationAmount());
            se.setSubsidyAmount(sub.getAllowanceAmount());
            se.setMealAllowance(sub.getMealAllowance());
            se.setTransportationAllowance(sub.getTransportationAllowance());
            se.setPhoneAllowance(sub.getPhoneAllowance());
            subsidyMapper.insert(se);

            if (!CollectionUtils.isEmpty(sub.getCalendar())) {
                for (SubsidyCalendarDTO cal : sub.getCalendar()) {
                    SubsidyCalendarEntity ce = new SubsidyCalendarEntity();
                    ce.setId(UUID.randomUUID().toString().replace("-", ""));
                    ce.setMainId(mainId);
                    ce.setSubsidyId(se.getId());
                    ce.setTravelDate(cal.getDate());
                    ce.setTravelDateWeek(cal.getWeekday());
                    ce.setSubsidizedCityNumber(cal.getAllowanceCityNo());

                    SubsidyCalendarDTO.AllowanceAmountCell mealCell = cal.getCells().get("meal");
                    SubsidyCalendarDTO.AllowanceAmountCell trafficCell = cal.getCells().get("traffic");
                    SubsidyCalendarDTO.AllowanceAmountCell commCell = cal.getCells().get("communication");

                    ce.setStandardMealExpensesAmount(mealCell != null ? mealCell.getStandardAmount() : BigDecimal.ZERO);
                    ce.setStandardTrafficAmount(trafficCell != null ? trafficCell.getStandardAmount() : BigDecimal.ZERO);
                    ce.setStandardCommunicationAmount(commCell != null ? commCell.getStandardAmount() : BigDecimal.ZERO);
                    ce.setMealExpensesAmount(mealCell != null ? mealCell.getAllowanceAmount() : BigDecimal.ZERO);
                    ce.setTrafficAmount(trafficCell != null ? trafficCell.getAllowanceAmount() : BigDecimal.ZERO);
                    ce.setCommunicationAmount(commCell != null ? commCell.getAllowanceAmount() : BigDecimal.ZERO);
                    ce.setMealSelected(mealCell != null && Boolean.TRUE.equals(mealCell.getSelected()) ? "1" : "0");
                    ce.setTrafficSelected(trafficCell != null && Boolean.TRUE.equals(trafficCell.getSelected()) ? "1" : "0");
                    ce.setCommunicationSelected(commCell != null && Boolean.TRUE.equals(commCell.getSelected()) ? "1" : "0");
                    ce.setIsReimbursed("0");
                    calendarMapper.insert(ce);
                }
            }
        }

        int sort = 0;
        for (AllocationDTO a : dto.getAllocations()) {
            AllocationEntity ae = new AllocationEntity();
            ae.setId(UUID.randomUUID().toString().replace("-", ""));
            ae.setMainId(mainId);
            ae.setSortNo(sort++);
            ae.setAllocCompanyId(a.getCompanyId());
            ae.setAllocCompanyNo(a.getCompanyNo());
            ae.setAllocCompanyName(a.getCompanyName());
            ae.setProjectId(a.getProjectId());
            ae.setProjectNo(a.getProjectNo());
            ae.setProjectName(a.getProjectName());
            ae.setAllocationRatio(a.getRatio());
            ae.setAllocationAmount(a.getAmount());
            allocationMapper.insert(ae);
        }
    }

    private void validateItineraries(List<ItineraryDTO> list, boolean strict) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        LocalDate today = LocalDate.now();
        for (ItineraryDTO it : list) {
            LocalDate dep = LocalDate.parse(it.getStartDate(), D);
            LocalDate arr = LocalDate.parse(it.getEndDate(), D);
            if (arr.isBefore(dep) || arr.isAfter(today)) {
                throw new BusinessException(ErrorCode.DATE_INVALID);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            ItineraryDTO a = list.get(i);
            LocalDate aStart = LocalDate.parse(a.getStartDate(), D);
            LocalDate aEnd = LocalDate.parse(a.getEndDate(), D);
            for (int j = i + 1; j < list.size(); j++) {
                ItineraryDTO b = list.get(j);
                if (!a.getTravelerId().equals(b.getTravelerId())) {
                    continue;
                }
                LocalDate bStart = LocalDate.parse(b.getStartDate(), D);
                LocalDate bEnd = LocalDate.parse(b.getEndDate(), D);
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
                .map(a -> a.getRatio() != null ? a.getRatio() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalRatio.setScale(4, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException(ErrorCode.ALLOCATION_RATIO);
        }
        BigDecimal totalAmt = dto.getAllocations().stream()
                .map(a -> a.getAmount() != null ? a.getAmount() : BigDecimal.ZERO)
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
        if (!CollectionUtils.isEmpty(dto.getAllowances())) {
            for (SubsidyDTO s : dto.getAllowances()) {
                subTotal = subTotal.add(nvl(s.getAllowanceAmount()));
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
        for (SubsidyDTO sub : dto.getAllowances()) {
            BigDecimal app = BigDecimal.ZERO;
            BigDecimal subAmt = BigDecimal.ZERO;
            BigDecimal meal = BigDecimal.ZERO;
            BigDecimal traffic = BigDecimal.ZERO;
            BigDecimal phone = BigDecimal.ZERO;
            if (CollectionUtils.isEmpty(sub.getCalendar())) {
                continue;
            }
            for (SubsidyCalendarDTO cal : sub.getCalendar()) {
                SubsidyCalendarDTO.AllowanceAmountCell mealCell = cal.getCells().get("meal");
                SubsidyCalendarDTO.AllowanceAmountCell trafficCell = cal.getCells().get("traffic");
                SubsidyCalendarDTO.AllowanceAmountCell commCell = cal.getCells().get("communication");

                BigDecimal mealStd = mealCell != null ? nvl(mealCell.getStandardAmount()) : BigDecimal.ZERO;
                BigDecimal trafficStd = trafficCell != null ? nvl(trafficCell.getStandardAmount()) : BigDecimal.ZERO;
                BigDecimal commStd = commCell != null ? nvl(commCell.getStandardAmount()) : BigDecimal.ZERO;

                if (mealCell != null && Boolean.TRUE.equals(mealCell.getSelected())) {
                    BigDecimal m = nvl(mealCell.getAllowanceAmount());
                    if (m.compareTo(mealStd) > 0) {
                        throw new BusinessException(ErrorCode.SUBSIDY_OVER_STANDARD);
                    }
                    app = app.add(mealStd);
                    subAmt = subAmt.add(m);
                    meal = meal.add(m);
                } else if (mealCell != null) {
                    mealCell.setAllowanceAmount(BigDecimal.ZERO);
                }
                if (trafficCell != null && Boolean.TRUE.equals(trafficCell.getSelected())) {
                    BigDecimal t = nvl(trafficCell.getAllowanceAmount());
                    if (t.compareTo(trafficStd) > 0) {
                        throw new BusinessException(ErrorCode.SUBSIDY_OVER_STANDARD);
                    }
                    app = app.add(trafficStd);
                    subAmt = subAmt.add(t);
                    traffic = traffic.add(t);
                } else if (trafficCell != null) {
                    trafficCell.setAllowanceAmount(BigDecimal.ZERO);
                }
                if (commCell != null && Boolean.TRUE.equals(commCell.getSelected())) {
                    BigDecimal c = nvl(commCell.getAllowanceAmount());
                    if (c.compareTo(commStd) > 0) {
                        throw new BusinessException(ErrorCode.SUBSIDY_OVER_STANDARD);
                    }
                    app = app.add(commStd);
                    subAmt = subAmt.add(c);
                    phone = phone.add(c);
                } else if (commCell != null) {
                    commCell.setAllowanceAmount(BigDecimal.ZERO);
                }
            }
            sub.setApplicationAmount(app);
            sub.setAllowanceAmount(subAmt);
            sub.setMealAllowance(meal);
            sub.setTransportationAllowance(traffic);
            sub.setPhoneAllowance(phone);
        }
    }

    @SuppressWarnings("unchecked")
    private void resolveNames(ReimbursementDTO dto) {
        Map<String, Object> master = masterData();

        if (StringUtils.hasText(dto.getCompanyId())) {
            for (Map<String, Object> opt : (List<Map<String, Object>>) master.get("reimCompanyOptions")) {
                if (dto.getCompanyId().equals(opt.get("reimCompanyId"))) {
                    dto.setReimCompanyNo((String) opt.get("reimCompanyNo"));
                    dto.setReimCompanyName((String) opt.get("reimCompanyName"));
                    break;
                }
            }
        }
        if (StringUtils.hasText(dto.getDepartmentId())) {
            for (Map<String, Object> opt : (List<Map<String, Object>>) master.get("reimDepartmentOptions")) {
                if (dto.getDepartmentId().equals(opt.get("reimDepartmentId"))) {
                    dto.setReimDepartmentNo((String) opt.get("reimDepartmentNo"));
                    dto.setReimDepartmentName((String) opt.get("reimDepartmentName"));
                    break;
                }
            }
        }
        if (StringUtils.hasText(dto.getReimburserId())) {
            for (Map<String, Object> opt : (List<Map<String, Object>>) master.get("reimburserOptions")) {
                if (dto.getReimburserId().equals(opt.get("reimburserId"))) {
                    dto.setReimburserNo((String) opt.get("reimburserNo"));
                    dto.setReimburserName((String) opt.get("reimburserName"));
                    break;
                }
            }
        }
        if (StringUtils.hasText(dto.getBusinessTypeId())) {
            for (Map<String, Object> opt : (List<Map<String, Object>>) master.get("businessTypeOptions")) {
                if (dto.getBusinessTypeId().equals(opt.get("businessTypeId"))) {
                    dto.setBusinessTypeNo((String) opt.get("businessTypeNo"));
                    dto.setBusinessTypeName((String) opt.get("businessTypeName"));
                    break;
                }
            }
        }
    }

    private void copyMain(ReimbursementDTO dto, ReimbursementMainEntity main, String status) {
        main.setReimbursementTitle(dto.getTitle());
        main.setBusinessTripReason(dto.getReason());
        main.setReimburserId(dto.getReimburserId());
        main.setReimburserNo(dto.getReimburserNo());
        main.setReimburserName(dto.getReimburserName());
        main.setReimDepartmentId(dto.getDepartmentId());
        main.setReimDepartmentNo(dto.getReimDepartmentNo());
        main.setReimDepartmentName(dto.getReimDepartmentName());
        main.setReimCompanyId(dto.getCompanyId());
        main.setReimCompanyNo(dto.getReimCompanyNo());
        main.setReimCompanyName(dto.getReimCompanyName());
        main.setBusinessTypeId(dto.getBusinessTypeId());
        main.setBusinessTypeNo(dto.getBusinessTypeNo());
        main.setBusinessTypeName(dto.getBusinessTypeName());
        main.setRemarks(dto.getRemark());
        main.setSubsidyTotal(dto.getSubsidyTotal());
        main.setMealAllowance(dto.getMealAllowance());
        main.setTransportationAllowance(dto.getTransportationAllowance());
        main.setPhoneAllowance(dto.getPhoneAllowance());
        main.setDocStatus(status);
        if (!StringUtils.hasText(main.getDocDate())) {
            main.setDocDate(dto.getSubmitDate() != null ? dto.getSubmitDate() : LocalDate.now().format(D));
        }
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private ReimbursementDTO toListDto(ReimbursementMainEntity e) {
        ReimbursementDTO d = new ReimbursementDTO();
        d.setId(e.getId());
        d.setReimbursementNo(e.getReimbursementNo());
        d.setDocumentStatusCode(Integer.parseInt(e.getDocStatus()));
        d.setTitle(e.getReimbursementTitle());
        d.setReason(e.getBusinessTripReason());
        d.setReimburserId(e.getReimburserId());
        d.setReimburserNo(e.getReimburserNo());
        d.setReimburserName(e.getReimburserName());
        d.setDepartmentId(e.getReimDepartmentId());
        d.setReimDepartmentNo(e.getReimDepartmentNo());
        d.setReimDepartmentName(e.getReimDepartmentName());
        d.setCompanyId(e.getReimCompanyId());
        d.setReimCompanyNo(e.getReimCompanyNo());
        d.setReimCompanyName(e.getReimCompanyName());
        d.setBusinessTypeId(e.getBusinessTypeId());
        d.setBusinessTypeNo(e.getBusinessTypeNo());
        d.setBusinessTypeName(e.getBusinessTypeName());
        d.setSubsidyTotal(e.getSubsidyTotal());
        d.setSubmitDate(e.getCreationTime());
        return d;
    }

    private ReimbursementDTO toDetailDto(ReimbursementMainEntity e) {
        ReimbursementDTO d = new ReimbursementDTO();
        d.setId(e.getId());
        d.setReimbursementNo(e.getReimbursementNo());
        d.setDocumentStatusCode(Integer.parseInt(e.getDocStatus()));
        d.setTitle(e.getReimbursementTitle());
        d.setReason(e.getBusinessTripReason());
        d.setSubmitDate(e.getDocDate());
        d.setReimburserId(e.getReimburserId());
        d.setReimburserNo(e.getReimburserNo());
        d.setReimburserName(e.getReimburserName());
        d.setDepartmentId(e.getReimDepartmentId());
        d.setReimDepartmentNo(e.getReimDepartmentNo());
        d.setReimDepartmentName(e.getReimDepartmentName());
        d.setCompanyId(e.getReimCompanyId());
        d.setReimCompanyNo(e.getReimCompanyNo());
        d.setReimCompanyName(e.getReimCompanyName());
        d.setBusinessTypeId(e.getBusinessTypeId());
        d.setBusinessTypeNo(e.getBusinessTypeNo());
        d.setBusinessTypeName(e.getBusinessTypeName());
        d.setRemark(e.getRemarks());
        d.setSubsidyTotal(e.getSubsidyTotal());
        d.setMealAllowance(e.getMealAllowance());
        d.setTransportationAllowance(e.getTransportationAllowance());
        d.setPhoneAllowance(e.getPhoneAllowance());
        return d;
    }

    private ItineraryDTO toTripDto(ItineraryEntity e) {
        ItineraryDTO d = new ItineraryDTO();
        d.setId(e.getId());
        d.setTravelerId(e.getTravelerId());
        d.setTravelerNo(e.getTravelerNo());
        d.setTravelerName(e.getTravelerName());
        d.setDepartureCityNo(e.getDepartureCityNo());
        d.setArrivalCityNo(e.getArrivingCityNo());
        d.setStartDate(e.getDepartureDate());
        d.setEndDate(e.getArrivalDate());
        d.setDescription(e.getItineraryInstructions());
        return d;
    }

    private SubsidyDTO toAllowanceDto(SubsidyEntity e) {
        SubsidyDTO d = new SubsidyDTO();
        d.setId(e.getId());
        d.setTripId(e.getItineraryId());
        d.setTravelerId(e.getTravelerId());
        d.setTravelerNo(e.getTravelerNo());
        d.setTravelerName(e.getTravelerName());
        d.setStartDate(e.getDepartureDate());
        d.setEndDate(e.getArrivalDate());
        d.setDays(e.getSubsidyDays());
        d.setAllowanceCityNo(e.getArrivingCityNo());
        d.setApplicationAmount(e.getApplicationAmount());
        d.setAllowanceAmount(e.getSubsidyAmount());
        d.setMealAllowance(e.getMealAllowance());
        d.setTransportationAllowance(e.getTransportationAllowance());
        d.setPhoneAllowance(e.getPhoneAllowance());
        return d;
    }

    private SubsidyCalendarDTO toCalendarDto(SubsidyCalendarEntity e) {
        SubsidyCalendarDTO d = new SubsidyCalendarDTO();
        d.setId(e.getId());
        d.setDate(e.getTravelDate());
        d.setWeekday(e.getTravelDateWeek());
        d.setAllowanceCityNo(e.getSubsidizedCityNumber());

        Map<String, SubsidyCalendarDTO.AllowanceAmountCell> cells = new HashMap<>();

        SubsidyCalendarDTO.AllowanceAmountCell mealCell = new SubsidyCalendarDTO.AllowanceAmountCell();
        mealCell.setItemType("meal");
        mealCell.setStandardAmount(e.getStandardMealExpensesAmount());
        mealCell.setAllowanceAmount(e.getMealExpensesAmount());
        mealCell.setSelected("1".equals(e.getMealSelected()));
        cells.put("meal", mealCell);

        SubsidyCalendarDTO.AllowanceAmountCell trafficCell = new SubsidyCalendarDTO.AllowanceAmountCell();
        trafficCell.setItemType("traffic");
        trafficCell.setStandardAmount(e.getStandardTrafficAmount());
        trafficCell.setAllowanceAmount(e.getTrafficAmount());
        trafficCell.setSelected("1".equals(e.getTrafficSelected()));
        cells.put("traffic", trafficCell);

        SubsidyCalendarDTO.AllowanceAmountCell commCell = new SubsidyCalendarDTO.AllowanceAmountCell();
        commCell.setItemType("communication");
        commCell.setStandardAmount(e.getStandardCommunicationAmount());
        commCell.setAllowanceAmount(e.getCommunicationAmount());
        commCell.setSelected("1".equals(e.getCommunicationSelected()));
        cells.put("communication", commCell);

        d.setCells(cells);
        return d;
    }

    private AllocationDTO toAllocationDto(AllocationEntity e) {
        AllocationDTO d = new AllocationDTO();
        d.setId(e.getId());
        d.setCompanyId(e.getAllocCompanyId());
        d.setCompanyNo(e.getAllocCompanyNo());
        d.setCompanyName(e.getAllocCompanyName());
        d.setProjectId(e.getProjectId());
        d.setProjectNo(e.getProjectNo());
        d.setProjectName(e.getProjectName());
        d.setRatio(e.getAllocationRatio());
        d.setAmount(e.getAllocationAmount());
        return d;
    }
}