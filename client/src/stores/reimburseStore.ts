import { ref } from 'vue';
import { ElMessage } from 'element-plus';

import {
  fetchMasterData,
  fetchReimbursementDetail,
  fetchReimbursementPage,
  previewSubsidy,
  submitReimbursement,
  voidReimbursement,
  type MasterDataResponse,
} from '@/api/reimbursement';
import type { AllowanceCalendarDay, AllowanceItemType, AllowanceRecord, BusinessTypeOption, BusinessTypeTreeNode, CityOption, ProjectOption, ReimCompanyOption, ReimDepartmentOption, ReimburseForm, ReimburseListItem, ReimburseQuery, ReimburserOption, TripRecord } from '@/types/reimburse';

const reimburseList = ref<ReimburseListItem[]>([]);
const listTotal = ref(0);
const listLoading = ref(false);
const detailLoading = ref(false);
const masterDataLoading = ref(false);

const reimCompanyOptions = ref<ReimCompanyOption[]>([]);
const reimDepartmentOptions = ref<ReimDepartmentOption[]>([]);
const reimburserOptions = ref<ReimburserOption[]>([]);
const businessTypeOptions = ref<BusinessTypeOption[]>([]);
const businessTypeTreeOptions = ref<BusinessTypeTreeNode[]>([]);
const cityOptions = ref<CityOption[]>([]);
const projectOptions = ref<ProjectOption[]>([]);

let masterDataLoaded = false;

const formatDate = (date: Date): string => {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const allowanceItemTypes: AllowanceItemType[] = ['meal', 'traffic', 'communication'];

const toTime = (date: string): number => new Date(`${date}T00:00:00`).getTime();

const getDateRange = (startDate: string, endDate: string): string[] => {
  const result: string[] = [];
  const current = new Date(`${startDate}T00:00:00`);
  const end = new Date(`${endDate}T00:00:00`);

  while (current <= end) {
    result.push(formatDate(current));
    current.setDate(current.getDate() + 1);
  }

  return result;
};

const getAllowanceStandard = (cityNo: string, itemType: AllowanceItemType): number => {
  if (itemType === 'traffic' || itemType === 'communication') {
    return 40;
  }

  const cityType = cityOptions.value.find((item) => item.cityNo === cityNo)?.cityType ?? '3';
  const standardMap = {
    1: 100,
    2: 80,
    3: 50,
  } as const;

  return standardMap[cityType];
};

const normalizeCalendarDay = (day: AllowanceCalendarDay): AllowanceCalendarDay => ({
  ...day,
  cells: allowanceItemTypes.reduce(
    (cells, itemType) => {
      const cell = day.cells?.[itemType];
      cells[itemType] = {
        itemType,
        standardAmount: Number(cell?.standardAmount ?? 0),
        allowanceAmount: Number(cell?.allowanceAmount ?? 0),
        selected: Boolean(cell?.selected),
      };
      return cells;
    },
    {} as AllowanceCalendarDay['cells'],
  ),
});

const buildLocalCalendarDays = (trips: TripRecord[]): AllowanceCalendarDay[] => {
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];

  return trips.flatMap((trip) =>
    getDateRange(trip.startDate, trip.endDate).map((date) => ({
      id: `${trip.id}-${date}`,
      date,
      weekday: weekdays[new Date(`${date}T00:00:00`).getDay()],
      allowanceCityNo: trip.arrivalCityNo,
      cells: allowanceItemTypes.reduce(
        (cells, itemType) => {
          const standardAmount = getAllowanceStandard(trip.arrivalCityNo, itemType);
          cells[itemType] = {
            itemType,
            standardAmount,
            allowanceAmount: 0,
            selected: false,
          };
          return cells;
        },
        {} as AllowanceCalendarDay['cells'],
      ),
    })),
  );
};

const summarizeCalendarAmount = (calendar: AllowanceCalendarDay[]) =>
  calendar.reduce(
    (summary, day) => {
      summary.mealAllowance += day.cells.meal.allowanceAmount;
      summary.transportationAllowance += day.cells.traffic.allowanceAmount;
      summary.phoneAllowance += day.cells.communication.allowanceAmount;
      allowanceItemTypes.forEach((itemType) => {
        const cell = day.cells[itemType];
        summary.applicationAmount += cell.selected ? cell.standardAmount : 0;
        summary.allowanceAmount += cell.allowanceAmount;
      });
      return summary;
    },
    {
      applicationAmount: 0,
      allowanceAmount: 0,
      mealAllowance: 0,
      transportationAllowance: 0,
      phoneAllowance: 0,
    },
  );

const buildAllowanceRecords = (trips: TripRecord[], calendarDays: AllowanceCalendarDay[]): AllowanceRecord[] => {
  const normalizedDays = calendarDays.map(normalizeCalendarDay);

  return trips.map((trip) => {
    const calendar = normalizedDays.filter((day) => {
      const dayTime = toTime(day.date);
      const inRange = dayTime >= toTime(trip.startDate) && dayTime <= toTime(trip.endDate);
      return day.id?.startsWith(`${trip.id}-`) || (inRange && day.allowanceCityNo === trip.arrivalCityNo);
    });
    const startCity = cityOptions.value.find((item) => item.cityNo === trip.departureCityNo);
    const endCity = cityOptions.value.find((item) => item.cityNo === trip.arrivalCityNo);
    const amountSummary = summarizeCalendarAmount(calendar);

    return {
      id: `allowance-${trip.id}`,
      tripId: trip.id,
      travelerId: trip.travelerId,
      startDate: trip.startDate,
      endDate: trip.endDate,
      days: calendar.length,
      routeText: `${startCity?.cityName ?? ''}-${endCity?.cityName ?? ''}`,
      allowanceCityNo: trip.arrivalCityNo,
      applicationAmount: amountSummary.applicationAmount,
      allowanceAmount: amountSummary.allowanceAmount,
      mealAllowance: amountSummary.mealAllowance,
      transportationAllowance: amountSummary.transportationAllowance,
      phoneAllowance: amountSummary.phoneAllowance,
      calendar,
    };
  });
};

const buildBusinessTypeTree = (options: BusinessTypeOption[]): BusinessTypeTreeNode[] => {
  const nodeMap = new Map<string, BusinessTypeTreeNode>();
  const roots: BusinessTypeTreeNode[] = [];

  options.forEach((item) => {
    nodeMap.set(item.businessTypeId, {
      ...item,
      label: item.businessTypeName,
      value: item.businessTypeId,
      children: [],
    });
  });

  nodeMap.forEach((node) => {
    const parent = nodeMap.get(node.superiorId);
    if (parent) {
      parent.children?.push(node);
    } else {
      roots.push(node);
    }
  });

  const prune = (nodes: BusinessTypeTreeNode[]): BusinessTypeTreeNode[] =>
    nodes.map((node) => {
      if (!node.children?.length) {
        const leafNode = { ...node };
        delete leafNode.children;
        return leafNode;
      }

      return {
        ...node,
        children: prune(node.children),
      };
    });

  return prune(roots);
};

const applyMasterData = (data: MasterDataResponse) => {
  reimCompanyOptions.value = data.reimCompanyOptions ?? [];
  reimDepartmentOptions.value = data.reimDepartmentOptions ?? [];
  reimburserOptions.value = data.reimburserOptions ?? [];
  businessTypeOptions.value = data.businessTypeOptions ?? [];
  businessTypeTreeOptions.value = buildBusinessTypeTree(data.businessTypeOptions ?? []);
  cityOptions.value = data.cityOptions ?? [];
  projectOptions.value = data.projectOptions ?? [];
};

const loadMasterData = async () => {
  if (masterDataLoaded || masterDataLoading.value) {
    return;
  }

  masterDataLoading.value = true;
  try {
    applyMasterData(await fetchMasterData());
    masterDataLoaded = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '主数据加载失败');
    throw error;
  } finally {
    masterDataLoading.value = false;
  }
};

const createBlankReimburseForm = (): ReimburseForm => ({
  id: '',
  reimbursementNo: undefined,
  documentStatusCode: 0,
  title: '',
  reason: '',
  submitDate: formatDate(new Date()),
  reimburserId: reimburserOptions.value[0]?.reimburserId ?? '',
  departmentId: reimDepartmentOptions.value[0]?.reimDepartmentId ?? '',
  companyId: reimCompanyOptions.value[0]?.reimCompanyId ?? '',
  businessTypeId: businessTypeOptions.value.find((item) => item.thereSubordinateNode === '0')?.businessTypeId ?? businessTypeOptions.value[0]?.businessTypeId ?? '',
  trips: [],
  allowances: [],
  allocations: [
    {
      id: '',
      companyId: reimCompanyOptions.value[0]?.reimCompanyId ?? '',
      projectId: projectOptions.value[0]?.projectId ?? '',
      ratio: 1,
      amount: 0,
    },
  ],
  remark: '',
});

const fetchReimburseList = async (query: ReimburseQuery, current: number, size: number) => {
  listLoading.value = true;
  try {
    const result = await fetchReimbursementPage({
      ...query,
      current,
      size,
    });
    reimburseList.value = result.records;
    listTotal.value = result.total;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '报销单列表加载失败');
    throw error;
  } finally {
    listLoading.value = false;
  }
};

const getReimburseDetail = async (id: string): Promise<ReimburseForm> => {
  await loadMasterData();

  if (id === 'new') {
    return createBlankReimburseForm();
  }

  detailLoading.value = true;
  try {
    return await fetchReimbursementDetail(id);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '报销单详情加载失败');
    throw error;
  } finally {
    detailLoading.value = false;
  }
};

const refreshAllowances = async (trips: TripRecord[]): Promise<AllowanceRecord[]> => {
  if (!trips.length) {
    return [];
  }

  try {
    const calendarDays = await previewSubsidy(trips);
    return buildAllowanceRecords(trips, calendarDays);
  } catch (error) {
    ElMessage.warning('补助日历预览接口异常，已按前端规则生成补助信息');
    return buildAllowanceRecords(trips, buildLocalCalendarDays(trips));
  }
};

const submitReimburseForm = async (form: ReimburseForm) => {
  try {
    return await submitReimbursement(form);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '报销单提交失败');
    throw error;
  }
};

const voidReimburseForm = async (id: string) => {
  try {
    await voidReimbursement(id);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '报销单作废失败');
    throw error;
  }
};

export const useReimburseStore = () => ({
  reimburseList,
  listTotal,
  listLoading,
  detailLoading,
  masterDataLoading,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
  businessTypeOptions,
  businessTypeTreeOptions,
  cityOptions,
  projectOptions,
  loadMasterData,
  createBlankReimburseForm,
  fetchReimburseList,
  getReimburseDetail,
  refreshAllowances,
  submitReimburseForm,
  voidReimburseForm,
});
