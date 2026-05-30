import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';

import { useReimburseStore } from '@/stores/reimburseStore';
import type { AllocationRecord, AllowanceItemType, AllowanceRecord, ReimburseForm, TripRecord } from '@/types/reimburse';

export const useReimburseDetail = () => {
const route = useRoute();
const router = useRouter();
const {
  getReimburseDetail,
  refreshAllowances,
  submitReimburseForm,
  loadMasterData,
  createBlankReimburseForm,
  businessTypeOptions,
  businessTypeTreeOptions,
  cityOptions,
  projectOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
} = useReimburseStore();
const detail = reactive<ReimburseForm>(createBlankReimburseForm());

const clonePlain = <T>(value: T): T => JSON.parse(JSON.stringify(value)) as T;

const applyDetail = (nextDetail: ReimburseForm) => {
  Object.assign(detail, clonePlain(nextDetail));
};

onMounted(async () => {
  await loadMasterData();
  applyDetail(await getReimburseDetail(String(route.params.id ?? 'new')));
});

const expanded = reactive({
  basic: true,
  trip: true,
  allowance: true,
  total: true,
  allocation: true,
  remark: true,
});

type TripDialogMode = 'create' | 'edit' | 'copy';

interface TripFormState {
  id: string;
  travelerId: string;
  departureCityNo: string;
  arrivalCityNo: string;
  dateRange: [string, string] | [];
  description: string;
}

const tripDialog = reactive({
  visible: false,
  mode: 'create' as TripDialogMode,
  editingId: '',
});

const tripFormRef = ref<FormInstance>();
const tripForm = reactive<TripFormState>({
  id: '',
  travelerId: '',
  departureCityNo: '',
  arrivalCityNo: '',
  dateRange: [],
  description: '',
});
const tripCalendarVisible = ref(false);
const tripCalendarMonth = ref(new Date());
const tripRangeStart = ref('');
const tripRangeEnd = ref('');

const allowanceDialog = reactive({
  visible: false,
  allowanceId: '',
});

const allowanceItemLabels: Record<AllowanceItemType, string> = {
  meal: '餐费补助',
  traffic: '交通补助',
  communication: '通讯补助',
};

const allowanceItemTypes: AllowanceItemType[] = ['meal', 'traffic', 'communication'];

const resetTripForm = () => {
  Object.assign(tripForm, {
    id: '',
    travelerId: '',
    departureCityNo: '',
    arrivalCityNo: '',
    dateRange: [],
    description: '',
  });
};

const todayText = computed(() => new Date().toISOString().slice(0, 10));

const tripDateRangeText = computed(() => {
  if (!tripRangeStart.value && !tripRangeEnd.value) {
    return '';
  }

  return `${tripRangeStart.value || '出发日期'} - ${tripRangeEnd.value || '到达日期'}`;
});

const tripDialogTitle = computed(() => {
  const titleMap: Record<TripDialogMode, string> = {
    create: '补录行程',
    edit: '编辑行程',
    copy: '复制行程',
  };
  return titleMap[tripDialog.mode];
});

const reimburserName = computed(() => {
  const reimburser = reimburserOptions.value.find((item) => item.reimburserId === detail.reimburserId);
  return reimburser ? `${reimburser.reimburserName}` : '';
});

const tripRows = computed(() =>
  detail.trips.map((trip, index) => {
    const traveler = reimburserOptions.value.find((item) => item.reimburserId === trip.travelerId);
    const startCity = cityOptions.value.find((item) => item.cityNo === trip.departureCityNo);
    const endCity = cityOptions.value.find((item) => item.cityNo === trip.arrivalCityNo);

    return {
      ...trip,
      index: index + 1,
      travelerText: traveler ? `${traveler.reimburserName}/${traveler.reimburserNo}` : '',
      dateText: `${trip.startDate} 至 ${trip.endDate}`,
      routeText: `${startCity?.cityName ?? ''} - ${endCity?.cityName ?? ''}`,
    };
  }),
);

const allowanceRows = computed(() =>
  detail.allowances.map((allowance, index) => {
    const traveler = reimburserOptions.value.find((item) => item.reimburserId === allowance.travelerId);
    const city = cityOptions.value.find((item) => item.cityNo === allowance.allowanceCityNo);

    return {
      ...allowance,
      index: index + 1,
      travelerText: traveler?.reimburserName ?? '',
      dateText: `${allowance.startDate} 至 ${allowance.endDate}`,
      cityText: city?.cityName ?? '',
    };
  }),
);

const amountTotals = computed(() => {
  let meal = 0;
  let traffic = 0;
  let communication = 0;

  detail.allowances.forEach((allowance) => {
    allowance.calendar?.forEach((day) => {
      meal += day.cells?.meal?.allowanceAmount ?? 0;
      traffic += day.cells?.traffic?.allowanceAmount ?? 0;
      communication += day.cells?.communication?.allowanceAmount ?? 0;
    });
  });

  return {
    meal,
    traffic,
    communication,
    total: meal + traffic + communication,
  };
});

const currentAllowance = computed(() =>
  detail.allowances.find((allowance) => allowance.id === allowanceDialog.allowanceId),
);

const currentAllowanceTraveler = computed(() => {
  const traveler = reimburserOptions.value.find((item) => item.reimburserId === currentAllowance.value?.travelerId);
  return traveler ? `${traveler.reimburserName}/${traveler.reimburserNo}` : '';
});

const currentAllowanceCity = computed(() => {
  const city = cityOptions.value.find((item) => item.cityNo === currentAllowance.value?.allowanceCityNo);
  return city?.cityName ?? '';
});

const businessTypeName = computed(() => {
  const businessType = businessTypeOptions.value.find((item) => item.businessTypeId === detail.businessTypeId);
  return businessType?.businessTypeName ?? '';
});

const currentAllowanceSummary = computed(() => {
  const allowance = currentAllowance.value;
  if (!allowance) {
    return {
      standardAmount: 0,
      allowanceAmount: 0,
      applicationAmount: 0,
    };
  }

  return (allowance.calendar ?? []).reduce(
    (summary, day) => {
      allowanceItemTypes.forEach((itemType) => {
        const cell = day.cells?.[itemType];
        if (cell?.selected) {
          summary.standardAmount += cell.standardAmount ?? 0;
          summary.allowanceAmount += cell.allowanceAmount ?? 0;
          summary.applicationAmount += cell.standardAmount ?? 0;
        }
      });
      return summary;
    },
    { standardAmount: 0, allowanceAmount: 0, applicationAmount: 0 },
  );
});

const formatAmount = (amount: number | undefined): string => Number(amount ?? 0).toFixed(2);

const roundAmount = (amount: number): number => Number(amount.toFixed(2));

const allocationRatioTotal = computed(() =>
  detail.allocations.reduce((total, allocation) => total + allocation.ratio, 0),
);

const allocationAmountTotal = computed(() =>
  detail.allocations.reduce((total, allocation) => total + allocation.amount, 0),
);

const toggleSection = (key: keyof typeof expanded) => {
  expanded[key] = !expanded[key];
};

const getTripDateRange = (trip: TripRecord): [string, string] => [trip.startDate, trip.endDate];

const toTime = (date: string): number => new Date(`${date}T00:00:00`).getTime();

const formatDate = (date: Date): string => {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const tripCalendarMonthLabel = computed(() => {
  const year = tripCalendarMonth.value.getFullYear();
  const month = tripCalendarMonth.value.toLocaleString('en-US', { month: 'long' });
  return `${year} ${month}`;
});

const tripCalendarDays = computed(() => {
  const year = tripCalendarMonth.value.getFullYear();
  const month = tripCalendarMonth.value.getMonth();
  const firstDay = new Date(year, month, 1);
  const gridStart = new Date(firstDay);
  gridStart.setDate(firstDay.getDate() - firstDay.getDay());

  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(gridStart);
    date.setDate(gridStart.getDate() + index);
    const value = formatDate(date);

    return {
      value,
      day: date.getDate(),
      inMonth: date.getMonth() === month,
      disabled: toTime(value) > toTime(todayText.value),
    };
  });
});

const changeTripCalendarMonth = (offset: number) => {
  const next = new Date(tripCalendarMonth.value);
  next.setMonth(next.getMonth() + offset);
  tripCalendarMonth.value = next;
};

const syncTripRange = () => {
  if (tripRangeStart.value && tripRangeEnd.value) {
    tripForm.dateRange = [tripRangeStart.value, tripRangeEnd.value];
    tripFormRef.value?.clearValidate('dateRange');
    return;
  }

  tripForm.dateRange = [];
};

const selectTripCalendarDate = (date: string, disabled: boolean) => {
  if (disabled) {
    return;
  }

  if (!tripRangeStart.value || (tripRangeStart.value && tripRangeEnd.value)) {
    tripRangeStart.value = date;
    tripRangeEnd.value = '';
    syncTripRange();
    return;
  }

  if (toTime(date) < toTime(tripRangeStart.value)) {
    ElMessage.warning('到达日期不可早于出发日期');
    tripRangeStart.value = date;
    tripRangeEnd.value = '';
    syncTripRange();
    return;
  }

  tripRangeEnd.value = date;
  syncTripRange();
  tripCalendarVisible.value = false;
};

const clearTripCalendarRange = () => {
  tripRangeStart.value = '';
  tripRangeEnd.value = '';
  syncTripRange();
};

const hasDateOverlap = (
  travelerId: string,
  startDate: string,
  endDate: string,
  excludedTripId = '',
): boolean => {
  const start = toTime(startDate);
  const end = toTime(endDate);

  return detail.trips.some((trip) => {
    if (trip.id === excludedTripId || trip.travelerId !== travelerId) {
      return false;
    }

    return start <= toTime(trip.endDate) && end >= toTime(trip.startDate);
  });
};

const validateDateRange = (_rule: unknown, value: TripFormState['dateRange'], callback: (error?: Error) => void) => {
  if (!value.length) {
    callback(new Error('请选择出发到达日期'));
    return;
  }

  const [startDate, endDate] = value;
  if (toTime(endDate) < toTime(startDate)) {
    callback(new Error('到达日期不可早于出发日期'));
    return;
  }

  if (toTime(endDate) > toTime(todayText.value)) {
    callback(new Error('到达日期不可晚于当前日期'));
    return;
  }

  callback();
};

const tripRules: FormRules<TripFormState> = {
  travelerId: [{ required: true, message: '请选择出行人', trigger: 'change' }],
  departureCityNo: [{ required: true, message: '请选择出发城市', trigger: 'change' }],
  arrivalCityNo: [{ required: true, message: '请选择到达城市', trigger: 'change' }],
  dateRange: [{ required: true, validator: validateDateRange, trigger: 'change' }],
  description: [
    { required: true, message: '请输入行程说明', trigger: 'blur' },
    { max: 500, message: '行程说明不可超过500字', trigger: 'blur' },
  ],
};

const refreshAllowancePreview = async () => {
  detail.allowances = await refreshAllowances(detail.trips);
};

const recalculateAllowance = (allowance: AllowanceRecord) => {
  let applicationAmount = 0;
  let allowanceAmount = 0;
  let mealAllowance = 0;
  let transportationAllowance = 0;
  let phoneAllowance = 0;

  allowance.calendar.forEach((day) => {
    allowanceItemTypes.forEach((itemType) => {
      const cell = day.cells[itemType];
      if (cell.selected) {
        applicationAmount += cell.standardAmount;
        allowanceAmount += cell.allowanceAmount;
      }
    });
    mealAllowance += day.cells.meal.allowanceAmount;
    transportationAllowance += day.cells.traffic.allowanceAmount;
    phoneAllowance += day.cells.communication.allowanceAmount;
  });

  allowance.applicationAmount = Number(applicationAmount.toFixed(2));
  allowance.allowanceAmount = Number(allowanceAmount.toFixed(2));
  allowance.mealAllowance = Number(mealAllowance.toFixed(2));
  allowance.transportationAllowance = Number(transportationAllowance.toFixed(2));
  allowance.phoneAllowance = Number(phoneAllowance.toFixed(2));
};

const openAllowanceDialog = (row: AllowanceRecord) => {
  allowanceDialog.allowanceId = row.id;
  allowanceDialog.visible = true;
};

const closeAllowanceDialog = () => {
  allowanceDialog.visible = false;
  allowanceDialog.allowanceId = '';
};

const setAllowanceCellSelected = (
  allowance: AllowanceRecord | undefined,
  dayId: string,
  itemType: AllowanceItemType,
  selected: boolean,
) => {
  if (!allowance) {
    return;
  }

  const day = allowance.calendar.find((item) => item.id === dayId);
  if (!day) {
    return;
  }

  const cell = day.cells[itemType];
  cell.selected = selected;
  cell.allowanceAmount = selected ? cell.standardAmount : 0;
  recalculateAllowance(allowance);
};

const isAllowanceRowChecked = (allowance: AllowanceRecord | undefined, dayId: string): boolean => {
  if (!allowance) {
    return false;
  }

  const day = allowance.calendar.find((item) => item.id === dayId);
  return !!day && allowanceItemTypes.every((itemType) => day.cells[itemType].selected);
};

const isAllowanceItemChecked = (allowance: AllowanceRecord | undefined, itemType: AllowanceItemType): boolean =>
  !!allowance && allowance.calendar.length > 0 && allowance.calendar.every((day) => day.cells[itemType].selected);

const isAllowanceAllChecked = (allowance: AllowanceRecord | undefined): boolean =>
  !!allowance &&
  allowance.calendar.length > 0 &&
  allowance.calendar.every((day) => allowanceItemTypes.every((itemType) => day.cells[itemType].selected));

const toggleAllowanceRow = (allowance: AllowanceRecord | undefined, dayId: string, selected: boolean) => {
  if (!allowance) {
    return;
  }

  const day = allowance.calendar.find((item) => item.id === dayId);
  if (!day) {
    return;
  }

  allowanceItemTypes.forEach((itemType) => {
    const cell = day.cells[itemType];
    cell.selected = selected;
    cell.allowanceAmount = selected ? cell.standardAmount : 0;
  });
  recalculateAllowance(allowance);
};

const toggleAllowanceItem = (allowance: AllowanceRecord | undefined, itemType: AllowanceItemType, selected: boolean) => {
  if (!allowance) {
    return;
  }

  allowance.calendar.forEach((day) => {
    const cell = day.cells[itemType];
    cell.selected = selected;
    cell.allowanceAmount = selected ? cell.standardAmount : 0;
  });
  recalculateAllowance(allowance);
};

const toggleAllowanceAll = (allowance: AllowanceRecord | undefined, selected: boolean) => {
  if (!allowance) {
    return;
  }

  allowance.calendar.forEach((day) => {
    allowanceItemTypes.forEach((itemType) => {
      const cell = day.cells[itemType];
      cell.selected = selected;
      cell.allowanceAmount = selected ? cell.standardAmount : 0;
    });
  });
  recalculateAllowance(allowance);
};

const handleAllowanceAmountChange = (
  allowance: AllowanceRecord | undefined,
  dayId: string,
  itemType: AllowanceItemType,
  value: number | undefined,
) => {
  if (!allowance) {
    return;
  }

  const day = allowance.calendar.find((item) => item.id === dayId);
  if (!day) {
    return;
  }

  const cell = day.cells[itemType];
  if (value === undefined || Number.isNaN(value)) {
    cell.allowanceAmount = 0;
  } else {
    cell.allowanceAmount = Math.min(Math.max(value, 0), cell.standardAmount);
  }
  recalculateAllowance(allowance);
};

const recalculateAllocationAmounts = () => {
  if (!detail.allocations.length) {
    return;
  }

  if (detail.allocations.length === 1) {
    detail.allocations[0].ratio = 1;
    detail.allocations[0].amount = roundAmount(amountTotals.value.total);
    return;
  }

  const otherRows = detail.allocations.slice(1);
  const otherRatioTotal = otherRows.reduce((total, allocation) => total + allocation.ratio, 0);
  detail.allocations[0].ratio = Number(Math.max(0, 1 - otherRatioTotal).toFixed(4));

  const otherAmountTotal = otherRows.reduce((total, allocation) => {
    allocation.amount = roundAmount(amountTotals.value.total * allocation.ratio);
    return total + allocation.amount;
  }, 0);

  detail.allocations[0].amount = roundAmount(amountTotals.value.total - otherAmountTotal);
};

const handleAllocationRatioChange = (index: number, value: number | undefined) => {
  if (index === 0) {
    return;
  }

  const nextPercent = value ?? 0;
  const otherPercentTotal = detail.allocations.reduce((total, allocation, currentIndex) => {
    if (currentIndex === 0 || currentIndex === index) {
      return total;
    }

    return total + allocation.ratio * 100;
  }, 0);

  if (otherPercentTotal + nextPercent > 100) {
    detail.allocations[index].ratio = 0;
    ElMessage.warning('分摊比例合计不可超过100%');
    recalculateAllocationAmounts();
    return;
  }

  detail.allocations[index].ratio = Number((Math.max(0, nextPercent) / 100).toFixed(4));
  recalculateAllocationAmounts();
};

const splitAllocationsEqually = () => {
  const count = detail.allocations.length;
  if (!count) {
    return;
  }

  if (count === 1) {
    recalculateAllocationAmounts();
    return;
  }

  const basePercent = Math.floor((100 / count) * 100) / 100;
  detail.allocations.forEach((allocation, index) => {
    const percent = index === 0 ? 100 - basePercent * (count - 1) : basePercent;
    allocation.ratio = Number((percent / 100).toFixed(4));
  });
  recalculateAllocationAmounts();
};

const addAllocation = () => {
  detail.allocations.push({
    id: `allocation-${Date.now()}`,
    companyId: '',
    projectId: '',
    ratio: 0,
    amount: 0,
  });
  recalculateAllocationAmounts();
};

const deleteAllocation = async (row: AllocationRecord) => {
  if (detail.allocations.length === 1) {
    ElMessage.warning('至少保留一条分摊信息');
    return;
  }

  try {
    await ElMessageBox.confirm('确定删除?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch {
    return;
  }

  const index = detail.allocations.findIndex((allocation) => allocation.id === row.id);
  if (index >= 0) {
    detail.allocations.splice(index, 1);
  }
  recalculateAllocationAmounts();
  ElMessage.success('删除成功');
};

const deleteRemark = async () => {
  try {
    await ElMessageBox.confirm('确认删除备注?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch {
    return;
  }

  detail.remark = '';
  ElMessage.success('删除成功');
};

watch(
  () => amountTotals.value.total,
  () => recalculateAllocationAmounts(),
  { immediate: true },
);

const openTripDialog = (mode: TripDialogMode, row?: TripRecord) => {
  tripDialog.mode = mode;
  tripDialog.editingId = mode === 'edit' && row ? row.id : '';
  resetTripForm();
  clearTripCalendarRange();
  tripCalendarMonth.value = new Date(`${todayText.value}T00:00:00`);

  if (row) {
    tripRangeStart.value = row.startDate;
    tripRangeEnd.value = row.endDate;
    tripCalendarMonth.value = new Date(`${row.startDate}T00:00:00`);
    Object.assign(tripForm, {
      id: row.id,
      travelerId: row.travelerId,
      departureCityNo: row.departureCityNo,
      arrivalCityNo: row.arrivalCityNo,
      dateRange: getTripDateRange(row),
      description: row.description,
    });
  }

  tripDialog.visible = true;
};

const closeTripDialog = () => {
  tripDialog.visible = false;
  tripCalendarVisible.value = false;
  tripFormRef.value?.clearValidate();
  resetTripForm();
  clearTripCalendarRange();
};

const saveTrip = async () => {
  const valid = await tripFormRef.value?.validate().catch(() => false);
  if (!valid || !tripForm.dateRange.length) {
    return;
  }

  const [startDate, endDate] = tripForm.dateRange;
  const excludedTripId = tripDialog.mode === 'edit' ? tripDialog.editingId : '';

  if (hasDateOverlap(tripForm.travelerId, startDate, endDate, excludedTripId)) {
    ElMessage.warning('同一出行人员的行程日期不可重复');
    return;
  }

  const trip: TripRecord = {
    id: tripDialog.mode === 'edit' ? tripDialog.editingId : `trip-${Date.now()}`,
    travelerId: tripForm.travelerId,
    departureCityNo: tripForm.departureCityNo,
    arrivalCityNo: tripForm.arrivalCityNo,
    startDate,
    endDate,
    description: tripForm.description,
  };

  if (tripDialog.mode === 'edit') {
    const tripIndex = detail.trips.findIndex((item) => item.id === tripDialog.editingId);
    if (tripIndex >= 0) {
      detail.trips.splice(tripIndex, 1, trip);
    }
  } else {
    detail.trips.push(trip);
  }

  await refreshAllowancePreview();
  ElMessage.success('保存成功');
  closeTripDialog();
};

const deleteTrip = async (row: TripRecord) => {
  try {
    await ElMessageBox.confirm('确认删除?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch {
    return;
  }

  const tripIndex = detail.trips.findIndex((trip) => trip.id === row.id);
  if (tripIndex >= 0) {
    detail.trips.splice(tripIndex, 1);
  }

  await refreshAllowancePreview();

  ElMessage.success('删除成功');
};

const nearlyEqual = (left: number, right: number): boolean =>
  Math.abs(left - right) < 0.01;

const hasRepeatedTripDates = (): boolean => {
  for (let i = 0; i < detail.trips.length; i += 1) {
    const current = detail.trips[i];
    if (hasDateOverlap(current.travelerId, current.startDate, current.endDate, current.id)) {
      return true;
    }
  }

  return false;
};

const validateBeforeSubmit = (): string[] => {
  const errors: string[] = [];

  if (!detail.title.trim()) {
    errors.push('请填写报销标题');
  }
  if (!detail.reason.trim()) {
    errors.push('请填写出差事由');
  }
  if (!detail.reimburserId) {
    errors.push('请选择报销人');
  }
  if (!detail.departmentId) {
    errors.push('请选择报销部门');
  }
  if (!detail.companyId) {
    errors.push('请选择费用归属公司');
  }
  if (!detail.businessTypeId) {
    errors.push('请选择业务类型');
  }
  if (!detail.trips.length) {
    errors.push('请至少补录一条行程');
  }
  if (hasRepeatedTripDates()) {
    errors.push('补录行程中存在同一人员日期重复');
  }

  detail.trips.forEach((trip, index) => {
    const rowNo = index + 1;
    if (!trip.travelerId || !trip.departureCityNo || !trip.arrivalCityNo || !trip.startDate || !trip.endDate || !trip.description.trim()) {
      errors.push(`第${rowNo}条补录行程未填写完整`);
    }
  });

  if (!detail.allocations.length) {
    errors.push('请至少保留一条分摊信息');
  }
  detail.allocations.forEach((allocation, index) => {
    const rowNo = index + 1;
    if (!allocation.companyId) {
      errors.push(`第${rowNo}条分摊信息请选择费用归属`);
    }
    if (!allocation.projectId) {
      errors.push(`第${rowNo}条分摊信息请选择项目`);
    }
  });

  if (!nearlyEqual(allocationRatioTotal.value, 1)) {
    errors.push('分摊比例合计必须为100%');
  }
  if (!nearlyEqual(allocationAmountTotal.value, amountTotals.value.total)) {
    errors.push('分摊金额合计必须等于费用合计中的补助总金额');
  }

  return Array.from(new Set(errors));
};

const closeDocument = async () => {
  try {
    await ElMessageBox.confirm('确认关闭当前页面?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch {
    return;
  }

  router.push('/reimburse');
};

const submitDocument = async () => {
  const errors = validateBeforeSubmit();
  if (errors.length) {
    await ElMessageBox.alert(errors.join('<br />'), '校验提示', {
      confirmButtonText: '确定',
      dangerouslyUseHTMLString: true,
      type: 'warning',
    });
    return;
  }

  await submitReimburseForm(detail);

  await ElMessageBox.alert('提交成功', '提示', {
    confirmButtonText: '确定',
    type: 'success',
  });
  router.push('/reimburse');
};

return {
  detail,
  expanded,
  tripDialog,
  tripFormRef,
  tripForm,
  allowanceDialog,
  allowanceItemLabels,
  allowanceItemTypes,
  tripDialogTitle,
  reimburserName,
  tripRows,
  allowanceRows,
  amountTotals,
  currentAllowance,
  currentAllowanceTraveler,
  currentAllowanceCity,
  businessTypeName,
  currentAllowanceSummary,
  formatAmount,
  allocationRatioTotal,
  allocationAmountTotal,
  toggleSection,
  toTime,
  tripDateRangeText,
  tripCalendarVisible,
  tripCalendarMonthLabel,
  tripCalendarDays,
  changeTripCalendarMonth,
  selectTripCalendarDate,
  clearTripCalendarRange,
  tripRangeStart,
  tripRangeEnd,
  tripRules,
  openAllowanceDialog,
  closeAllowanceDialog,
  setAllowanceCellSelected,
  isAllowanceRowChecked,
  isAllowanceItemChecked,
  isAllowanceAllChecked,
  toggleAllowanceRow,
  toggleAllowanceItem,
  toggleAllowanceAll,
  handleAllowanceAmountChange,
  handleAllocationRatioChange,
  splitAllocationsEqually,
  addAllocation,
  deleteAllocation,
  deleteRemark,
  openTripDialog,
  closeTripDialog,
  saveTrip,
  deleteTrip,
  closeDocument,
  submitDocument,
  businessTypeTreeOptions,
  cityOptions,
  projectOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
};
};
