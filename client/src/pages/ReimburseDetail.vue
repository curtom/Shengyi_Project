<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';

import {
  businessTypeOptions,
  businessTypeTreeOptions,
  cityOptions,
  createAllowanceFromTrip,
  getMockReimburseDetail,
  projectOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
} from '@/mock';
import type { AllocationRecord, AllowanceItemType, AllowanceRecord, ReimburseForm, TripRecord } from '@/types/reimburse';

const route = useRoute();
const router = useRouter();
const detail = reactive<ReimburseForm>(
  structuredClone(getMockReimburseDetail(String(route.params.id ?? 'detail-001'))),
);

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

const tripDialogTitle = computed(() => {
  const titleMap: Record<TripDialogMode, string> = {
    create: '补录行程',
    edit: '编辑行程',
    copy: '复制行程',
  };
  return titleMap[tripDialog.mode];
});

const reimburserName = computed(() => {
  const reimburser = reimburserOptions.find((item) => item.reimburserId === detail.reimburserId);
  return reimburser ? `${reimburser.reimburserName}/${reimburser.reimburserNo}` : '';
});

const tripRows = computed(() =>
  detail.trips.map((trip, index) => {
    const traveler = reimburserOptions.find((item) => item.reimburserId === trip.travelerId);
    const startCity = cityOptions.find((item) => item.cityNo === trip.departureCityNo);
    const endCity = cityOptions.find((item) => item.cityNo === trip.arrivalCityNo);

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
    const traveler = reimburserOptions.find((item) => item.reimburserId === allowance.travelerId);
    const city = cityOptions.find((item) => item.cityNo === allowance.allowanceCityNo);

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
    allowance.calendar.forEach((day) => {
      meal += day.cells.meal.allowanceAmount;
      traffic += day.cells.traffic.allowanceAmount;
      communication += day.cells.communication.allowanceAmount;
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
  const traveler = reimburserOptions.find((item) => item.reimburserId === currentAllowance.value?.travelerId);
  return traveler ? `${traveler.reimburserName}/${traveler.reimburserNo}` : '';
});

const currentAllowanceCity = computed(() => {
  const city = cityOptions.find((item) => item.cityNo === currentAllowance.value?.allowanceCityNo);
  return city?.cityName ?? '';
});

const businessTypeName = computed(() => {
  const businessType = businessTypeOptions.find((item) => item.businessTypeId === detail.businessTypeId);
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

  return allowance.calendar.reduce(
    (summary, day) => {
      allowanceItemTypes.forEach((itemType) => {
        const cell = day.cells[itemType];
        if (cell.selected) {
          summary.standardAmount += cell.standardAmount;
          summary.allowanceAmount += cell.allowanceAmount;
          summary.applicationAmount += cell.standardAmount;
        }
      });
      return summary;
    },
    { standardAmount: 0, allowanceAmount: 0, applicationAmount: 0 },
  );
});

const formatAmount = (amount: number): string => amount.toFixed(2);

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

const datePickerDisabled = (date: Date): boolean => date.getTime() > toTime(todayText.value);

const syncAllowanceForTrip = (trip: TripRecord) => {
  const nextAllowance = createAllowanceFromTrip(trip);
  const allowanceIndex = detail.allowances.findIndex((allowance) => allowance.tripId === trip.id);

  if (allowanceIndex >= 0) {
    detail.allowances.splice(allowanceIndex, 1, nextAllowance);
    return;
  }

  detail.allowances.push(nextAllowance);
};

const recalculateAllowance = (allowance: AllowanceRecord) => {
  let applicationAmount = 0;
  let allowanceAmount = 0;

  allowance.calendar.forEach((day) => {
    allowanceItemTypes.forEach((itemType) => {
      const cell = day.cells[itemType];
      if (cell.selected) {
        applicationAmount += cell.standardAmount;
        allowanceAmount += cell.allowanceAmount;
      }
    });
  });

  allowance.applicationAmount = Number(applicationAmount.toFixed(2));
  allowance.allowanceAmount = Number(allowanceAmount.toFixed(2));
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

const isAllowanceRowIndeterminate = (allowance: AllowanceRecord | undefined, dayId: string): boolean => {
  if (!allowance) {
    return false;
  }

  const day = allowance.calendar.find((item) => item.id === dayId);
  if (!day) {
    return false;
  }

  const selectedCount = allowanceItemTypes.filter((itemType) => day.cells[itemType].selected).length;
  return selectedCount > 0 && selectedCount < allowanceItemTypes.length;
};

const isAllowanceItemChecked = (allowance: AllowanceRecord | undefined, itemType: AllowanceItemType): boolean =>
  !!allowance && allowance.calendar.length > 0 && allowance.calendar.every((day) => day.cells[itemType].selected);

const isAllowanceItemIndeterminate = (allowance: AllowanceRecord | undefined, itemType: AllowanceItemType): boolean => {
  if (!allowance) {
    return false;
  }

  const selectedCount = allowance.calendar.filter((day) => day.cells[itemType].selected).length;
  return selectedCount > 0 && selectedCount < allowance.calendar.length;
};

const isAllowanceAllChecked = (allowance: AllowanceRecord | undefined): boolean =>
  !!allowance &&
  allowance.calendar.length > 0 &&
  allowance.calendar.every((day) => allowanceItemTypes.every((itemType) => day.cells[itemType].selected));

const isAllowanceAllIndeterminate = (allowance: AllowanceRecord | undefined): boolean => {
  if (!allowance) {
    return false;
  }

  const total = allowance.calendar.length * allowanceItemTypes.length;
  const selectedCount = allowance.calendar.reduce(
    (count, day) => count + allowanceItemTypes.filter((itemType) => day.cells[itemType].selected).length,
    0,
  );

  return selectedCount > 0 && selectedCount < total;
};

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

  if (row) {
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
  tripFormRef.value?.clearValidate();
  resetTripForm();
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

  syncAllowanceForTrip(trip);
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

  const allowanceIndex = detail.allowances.findIndex((allowance) => allowance.tripId === row.id);
  if (allowanceIndex >= 0) {
    detail.allowances.splice(allowanceIndex, 1);
  }

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

  await ElMessageBox.alert('提交成功', '提示', {
    confirmButtonText: '确定',
    type: 'success',
  });
  router.push('/reimburse');
};
</script>

<template>
  <main class="page-shell detail-page">
    <header class="document-header">
      <div class="document-header-inner">
        <h1>差旅费用报销单</h1>
        <div class="submit-date">提单日期&nbsp;&nbsp;{{ detail.submitDate }}</div>
      </div>
    </header>

    <section class="document-body">
      <section class="document-section">
        <button class="section-title" type="button" @click="toggleSection('basic')">
          <span>基础信息</span>
          <el-icon><ArrowUp v-if="expanded.basic" /><ArrowDown v-else /></el-icon>
        </button>
        <div v-show="expanded.basic" class="section-content basic-content">
          <el-form label-width="92px" class="basic-form">
            <el-form-item label="报销标题">
              <el-input :model-value="detail.title" maxlength="500" />
            </el-form-item>
            <el-form-item label="报销人">
              <el-select :model-value="detail.reimburserId" placeholder="请选择">
                <el-option
                  v-for="item in reimburserOptions"
                  :key="item.reimburserId"
                  :label="`${item.reimburserName}/${item.reimburserNo}`"
                  :value="item.reimburserId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="报销部门">
              <el-select :model-value="detail.departmentId" placeholder="请选择">
                <el-option
                  v-for="item in reimDepartmentOptions"
                  :key="item.reimDepartmentId"
                  :label="item.reimDepartmentName"
                  :value="item.reimDepartmentId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="费用归属公司" class="required-label">
              <el-select :model-value="detail.companyId" placeholder="请选择">
                <el-option
                  v-for="item in reimCompanyOptions"
                  :key="item.reimCompanyId"
                  :label="item.reimCompanyName"
                  :value="item.reimCompanyId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="业务类型" class="required-label">
              <el-tree-select
                :model-value="detail.businessTypeId"
                :data="businessTypeTreeOptions"
                check-strictly
                :render-after-expand="false"
              />
            </el-form-item>
            <el-form-item label="出差事由" class="reason-field">
              <el-input :model-value="detail.reason" maxlength="500" placeholder="请输入" />
            </el-form-item>
          </el-form>
        </div>
      </section>

      <section class="document-section">
        <button class="section-title" type="button" @click="toggleSection('trip')">
          <span>补录行程</span>
          <span class="section-actions">
            <el-button link type="primary" @click.stop="openTripDialog('create')">
              <el-icon><CirclePlus /></el-icon>
              补录行程
            </el-button>
            <el-icon><ArrowUp v-if="expanded.trip" /><ArrowDown v-else /></el-icon>
          </span>
        </button>
        <div v-show="expanded.trip" class="section-content">
          <el-table :data="tripRows" border class="detail-table">
            <el-table-column prop="index" label="序号" width="64" align="center" />
            <el-table-column prop="travelerText" label="出行人员" min-width="150" />
            <el-table-column prop="dateText" label="出差日期" min-width="190" />
            <el-table-column prop="routeText" label="行程" min-width="180" />
            <el-table-column prop="description" label="行程说明" min-width="180" />
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-tooltip content="删除" placement="top">
                    <button class="icon-button" type="button" @click="deleteTrip(row)">
                      <el-icon><Delete /></el-icon>
                    </button>
                  </el-tooltip>
                  <el-tooltip content="编辑" placement="top">
                    <button class="icon-button" type="button" @click="openTripDialog('edit', row)">
                      <el-icon><EditPen /></el-icon>
                    </button>
                  </el-tooltip>
                  <el-tooltip content="复制" placement="top">
                    <button class="icon-button" type="button" @click="openTripDialog('copy', row)">
                      <el-icon><CopyDocument /></el-icon>
                    </button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>

      <section class="document-section">
        <button class="section-title" type="button" @click="toggleSection('allowance')">
          <span>补助信息 <span class="section-amount">{{ formatAmount(amountTotals.total) }}</span> <span class="muted">（{{ reimburserName }}:{{ detail.allowances[0]?.days ?? 0 }}天）</span></span>
          <el-icon><ArrowUp v-if="expanded.allowance" /><ArrowDown v-else /></el-icon>
        </button>
        <div v-show="expanded.allowance" class="section-content">
          <el-alert
            class="allowance-tip"
            type="warning"
            show-icon
            :closable="false"
            title="1、请根据实际出差日期选择补助2、出差期间当日有用餐安排的请自行核减当日餐补3、出差期间当日有用车的，请自行核减当日交补"
          />
          <el-table :data="allowanceRows" border class="detail-table">
            <el-table-column prop="index" label="序号" width="64" align="center" />
            <el-table-column prop="travelerText" label="出行人" min-width="120" />
            <el-table-column prop="dateText" label="出差日期" min-width="190" />
            <el-table-column prop="days" label="补助天数" width="100" />
            <el-table-column prop="routeText" label="行程" min-width="140" />
            <el-table-column prop="cityText" label="补助城市" min-width="120" />
            <el-table-column label="申请金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.applicationAmount) }}</template>
            </el-table-column>
            <el-table-column label="补助金额" width="120" align="right">
              <template #default="{ row }">{{ formatAmount(row.allowanceAmount) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90" align="center">
              <template #default="{ row }">
                <button class="icon-button" type="button" @click="openAllowanceDialog(row)">
                  <el-icon class="action-icon"><EditPen /></el-icon>
                </button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>

      <section class="document-section">
        <button class="section-title" type="button" @click="toggleSection('total')">
          <span>费用合计</span>
          <el-icon><ArrowUp v-if="expanded.total" /><ArrowDown v-else /></el-icon>
        </button>
        <div v-show="expanded.total" class="section-content total-grid">
          <div>补助总金额 <strong>{{ formatAmount(amountTotals.total) }}</strong></div>
          <div>餐费补助 <strong>{{ formatAmount(amountTotals.meal) }}</strong></div>
          <div>交通补助 <strong>{{ formatAmount(amountTotals.traffic) }}</strong></div>
          <div>通讯补助 <strong>{{ formatAmount(amountTotals.communication) }}</strong></div>
        </div>
      </section>

      <section class="document-section">
        <button class="section-title" type="button" @click="toggleSection('allocation')">
          <span>费用归属及分摊 <span class="muted">（分摊金额：{{ formatAmount(amountTotals.total) }}）</span></span>
          <el-icon><ArrowUp v-if="expanded.allocation" /><ArrowDown v-else /></el-icon>
        </button>
        <div v-show="expanded.allocation" class="section-content">
          <el-table :data="detail.allocations" border class="detail-table allocation-table">
            <el-table-column label="序号" width="64" align="center">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column label="费用归属*" min-width="220">
              <template #default="{ row }">
                <el-select v-model="row.companyId" placeholder="请选择" filterable clearable>
                  <el-option
                    v-for="item in reimCompanyOptions"
                    :key="item.reimCompanyId"
                    :label="item.reimCompanyName"
                    :value="item.reimCompanyId"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="项目" min-width="220">
              <template #default="{ row }">
                <el-select v-model="row.projectId" placeholder="请选择" filterable clearable>
                  <el-option
                    v-for="item in projectOptions"
                    :key="item.projectId"
                    :label="item.projectName"
                    :value="item.projectId"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column min-width="180" align="right">
              <template #header>
                <span class="allocation-ratio-header">
                  分摊比例
                  <el-tooltip content="均摊" placement="top" effect="dark">
                    <button class="equal-split-button" type="button" @click.stop="splitAllocationsEqually">
                      <el-icon><Refresh /></el-icon>
                    </button>
                  </el-tooltip>
                  <span class="required-mark">*</span>
                </span>
              </template>
              <template #default="{ row, $index }">
                <el-input-number
                  :model-value="Number((row.ratio * 100).toFixed(2))"
                  :disabled="$index === 0"
                  :min="0"
                  :max="100"
                  :precision="2"
                  :controls="false"
                  @change="(value: number | undefined) => handleAllocationRatioChange($index, value)"
                />
                <span class="percent-sign">%</span>
              </template>
            </el-table-column>
            <el-table-column label="分摊金额*" min-width="180" align="right">
              <template #default="{ row }">
                <el-input :model-value="formatAmount(row.amount)" disabled class="amount-input" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <button class="icon-button" type="button" @click="deleteAllocation(row)">
                  <el-icon class="action-icon"><Delete /></el-icon>
                </button>
              </template>
            </el-table-column>
          </el-table>
          <button class="add-row-button" type="button" @click="addAllocation">
            <el-icon><CirclePlus /></el-icon>
            添加一行
          </button>
          <div class="allocation-summary">
            <span>合计</span>
            <span>{{ formatAmount(allocationRatioTotal * 100) }}%</span>
            <span>CNY {{ formatAmount(allocationAmountTotal) }}</span>
          </div>
        </div>
      </section>

      <section class="document-section">
        <button class="section-title" type="button" @click="toggleSection('remark')">
          <span>备注信息</span>
          <span class="section-actions">
            <el-button link type="primary" @click.stop="deleteRemark">
              <el-icon><Delete /></el-icon>
              删除备注
            </el-button>
            <el-icon><ArrowUp v-if="expanded.remark" /><ArrowDown v-else /></el-icon>
          </span>
        </button>
        <div v-show="expanded.remark" class="section-content">
          <el-input
            v-model="detail.remark"
            type="textarea"
            maxlength="1000"
            :rows="5"
            placeholder="请输入"
            show-word-limit
          />
        </div>
      </section>
    </section>

    <footer class="document-footer">
      <div class="document-footer-inner">
        <el-button @click="closeDocument">关闭</el-button>
        <el-button type="primary" @click="submitDocument">提交</el-button>
      </div>
    </footer>

    <el-dialog
      v-model="tripDialog.visible"
      :title="tripDialogTitle"
      width="820px"
      class="trip-dialog"
      destroy-on-close
      @closed="closeTripDialog"
    >
      <el-alert
        class="trip-dialog-tip"
        type="warning"
        show-icon
        :closable="false"
        title="仅可补录未从申请单带入或未产生费用的行程信息 跨天跨城行程填写说明：出发城市-到达城市：武汉-北京; 出发日期-到达日期：1号-5号; 1号~5号补助按北京匹配;"
      />

      <el-form
        ref="tripFormRef"
        :model="tripForm"
        :rules="tripRules"
        label-width="124px"
        class="trip-form"
      >
        <el-form-item label="出行人" prop="travelerId" required>
          <el-select v-model="tripForm.travelerId" placeholder="请选择" filterable clearable>
            <el-option
              v-for="item in reimburserOptions"
              :key="item.reimburserId"
              :label="`${item.reimburserName}/${item.reimburserNo}`"
              :value="item.reimburserId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出发城市" prop="departureCityNo" required>
          <el-select v-model="tripForm.departureCityNo" placeholder="请选择" filterable clearable>
            <el-option
              v-for="item in cityOptions"
              :key="item.cityNo"
              :label="item.cityName"
              :value="item.cityNo"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="到达城市" prop="arrivalCityNo" required>
          <el-select v-model="tripForm.arrivalCityNo" placeholder="请选择" filterable clearable>
            <el-option
              v-for="item in cityOptions"
              :key="item.cityNo"
              :label="item.cityName"
              :value="item.cityNo"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出发到达日期" prop="dateRange" required>
          <el-date-picker
            v-model="tripForm.dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="-"
            start-placeholder="出发日期"
            end-placeholder="到达日期"
            :disabled-date="datePickerDisabled"
          />
        </el-form-item>
        <el-form-item label="行程说明" prop="description" required>
          <el-input
            v-model="tripForm.description"
            type="textarea"
            maxlength="500"
            :rows="3"
            placeholder="请输入"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeTripDialog">取消</el-button>
        <el-button type="primary" @click="saveTrip">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="allowanceDialog.visible"
      title="补助日历"
      width="1320px"
      class="allowance-dialog"
      destroy-on-close
      @closed="closeAllowanceDialog"
    >
      <div v-if="currentAllowance" class="allowance-dialog-layout">
        <aside class="allowance-side">
          <div class="side-row">
            <span>出差类型</span>
            <strong>{{ businessTypeName }}</strong>
          </div>
          <div class="side-row">
            <span>出行人</span>
            <strong>{{ currentAllowanceTraveler }}</strong>
          </div>
          <div class="timeline-card">
            <div class="timeline-row">
              <span>开始日期</span>
              <strong>{{ currentAllowance.startDate }}</strong>
            </div>
            <div class="timeline-route">
              <span>行程天数</span>
              <strong>{{ currentAllowance.routeText }}　{{ currentAllowance.days }}天</strong>
            </div>
            <div class="timeline-row">
              <span>结束日期</span>
              <strong>{{ currentAllowance.endDate }}</strong>
            </div>
          </div>
          <div class="amount-card">
            <div>
              <span>补助金额</span>
              <em>CNY</em>
              <strong>{{ formatAmount(currentAllowanceSummary.allowanceAmount) }}</strong>
            </div>
            <div>
              <span>标准总额</span>
              <em>CNY</em>
              <strong>{{ formatAmount(currentAllowanceSummary.standardAmount) }}</strong>
            </div>
            <div>
              <span>申请金额</span>
              <em>CNY</em>
              <strong>{{ formatAmount(currentAllowanceSummary.applicationAmount) }}</strong>
            </div>
          </div>
        </aside>

        <section class="allowance-calendar">
          <div class="calendar-heading">
            <h3>出差补助</h3>
            <el-checkbox
              :model-value="isAllowanceAllChecked(currentAllowance)"
              :indeterminate="isAllowanceAllIndeterminate(currentAllowance)"
              @change="(checked: boolean) => toggleAllowanceAll(currentAllowance, checked)"
            >
              全选
            </el-checkbox>
          </div>
          <el-table :data="currentAllowance.calendar" border class="calendar-table">
            <el-table-column label="出差日期" width="190" align="center">
              <template #default="{ row }">
                <div class="date-cell">
                  <span>{{ row.date }}</span>
                  <span>{{ row.weekday }}</span>
                  <el-checkbox
                    :model-value="isAllowanceRowChecked(currentAllowance, row.id)"
                    :indeterminate="isAllowanceRowIndeterminate(currentAllowance, row.id)"
                    @change="(checked: boolean) => toggleAllowanceRow(currentAllowance, row.id, checked)"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column label="补助城市" width="170" align="center">
              <template #default>
                {{ currentAllowanceCity }}
              </template>
            </el-table-column>
            <el-table-column
              v-for="itemType in allowanceItemTypes"
              :key="itemType"
              min-width="210"
              align="center"
            >
              <template #header>
                <div class="calendar-head-cell">
                  <span>{{ allowanceItemLabels[itemType] }}</span>
                  <el-checkbox
                    :model-value="isAllowanceItemChecked(currentAllowance, itemType)"
                    :indeterminate="isAllowanceItemIndeterminate(currentAllowance, itemType)"
                    @change="(checked: boolean) => toggleAllowanceItem(currentAllowance, itemType, checked)"
                  />
                </div>
              </template>
              <template #default="{ row }">
                <div class="amount-cell">
                  <div class="standard-text">CNY {{ formatAmount(row.cells[itemType].standardAmount) }} / 天</div>
                  <div class="amount-editor">
                    <el-checkbox
                      :model-value="row.cells[itemType].selected"
                      @change="(checked: boolean) => setAllowanceCellSelected(currentAllowance, row.id, itemType, checked)"
                    />
                    <el-input-number
                      v-model="row.cells[itemType].allowanceAmount"
                      :disabled="!row.cells[itemType].selected"
                      :min="0"
                      :max="row.cells[itemType].standardAmount"
                      :precision="2"
                      :controls="false"
                      @change="(value: number | undefined) => handleAllowanceAmountChange(currentAllowance, row.id, itemType, value)"
                    />
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </div>

      <template #footer>
        <el-button @click="closeAllowanceDialog">取消</el-button>
        <el-button type="primary" @click="closeAllowanceDialog">确认</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.detail-page {
  padding-bottom: 72px;
}

.document-header {
  position: sticky;
  top: 0;
  z-index: 20;
  height: 54px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgb(0 0 0 / 6%);
}

.document-header-inner {
  position: relative;
  display: flex;
  width: 1200px;
  height: 100%;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.document-header h1 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0;
}

.submit-date {
  position: absolute;
  right: 0;
  color: #374151;
  font-size: 14px;
}

.document-body {
  width: 1200px;
  min-height: calc(100vh - 126px);
  margin: 18px auto;
  padding: 16px 20px 56px;
  background: #ffffff;
}

.document-section + .document-section {
  margin-top: 22px;
}

.section-title {
  display: flex;
  width: 100%;
  height: 36px;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  border: 0;
  border-left: 3px solid #008dd6;
  background: #f0f2f5;
  color: #111827;
  cursor: pointer;
  font: inherit;
  font-size: 16px;
  font-weight: 600;
  text-align: left;
}

.section-actions {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.section-amount {
  margin-left: 14px;
  font-size: 14px;
  font-weight: 400;
}

.muted {
  color: #475569;
  font-size: 14px;
  font-weight: 400;
}

.section-content {
  padding: 12px 0 0;
}

.basic-content {
  padding: 16px 46px 4px;
}

.basic-form {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px 34px;
}

.basic-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.basic-form :deep(.el-input),
.basic-form :deep(.el-select),
.basic-form :deep(.el-tree-select) {
  width: 100%;
}

.basic-form :deep(.el-form-item:first-child),
.reason-field {
  grid-column: 1 / -1;
}

.required-label :deep(.el-form-item__label::after) {
  color: #f56c6c;
  content: "*";
}

.detail-table {
  width: 100%;
}

.detail-table :deep(.el-table__header th) {
  background: #f5f7fa;
  color: #1f2937;
  font-weight: 600;
}

.row-actions {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  color: #2f66ff;
}

.icon-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: #2f66ff;
  cursor: pointer;
  font: inherit;
}

.action-icon {
  color: #2f66ff;
}

.allowance-tip {
  margin-bottom: 12px;
}

.allowance-tip :deep(.el-alert__title) {
  overflow: hidden;
  max-width: 1000px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.total-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: 18px 42px;
  color: #111827;
}

.total-grid strong {
  margin-left: 16px;
  font-weight: 400;
}

.allocation-table {
  margin-top: 0;
}

.allocation-ratio-header {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  width: 100%;
}

.equal-split-button {
  display: inline-flex;
  width: 18px;
  height: 18px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: #2f66ff;
  cursor: pointer;
  font: inherit;
}

.equal-split-button:hover {
  color: #008dd6;
}

.required-mark {
  color: #f56c6c;
}

.allocation-table :deep(.el-select) {
  width: 100%;
}

.allocation-table :deep(.el-input-number) {
  width: 150px;
}

.allocation-table :deep(.el-input-number .el-input__inner),
.amount-input :deep(.el-input__inner) {
  text-align: right;
}

.percent-sign {
  margin-left: 6px;
}

.amount-input {
  width: 170px;
}

.add-row-button {
  display: flex;
  width: 100%;
  height: 40px;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 1px solid #ebeef5;
  border-top: 0;
  background: #ffffff;
  color: #2f66ff;
  cursor: pointer;
  font: inherit;
}

.allocation-summary {
  display: grid;
  grid-template-columns: 1fr 180px 280px 100px;
  align-items: center;
  height: 40px;
  border: 1px solid #ebeef5;
  border-top: 0;
  background: #fff8eb;
  color: #ff5b00;
}

.allocation-summary span:first-child {
  padding-left: 12px;
  color: #1f2937;
}

.allocation-summary span:nth-child(2),
.allocation-summary span:nth-child(3) {
  text-align: right;
}

.document-footer {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 20;
  height: 54px;
  background: #ffffff;
  box-shadow: 0 -2px 10px rgb(0 0 0 / 6%);
}

.document-footer-inner {
  display: flex;
  width: 1200px;
  height: 100%;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin: 0 auto;
}

.trip-dialog-tip {
  margin-bottom: 18px;
}

.trip-dialog-tip :deep(.el-alert__title) {
  line-height: 22px;
}

.trip-form {
  width: 690px;
  margin: 0 auto;
}

.trip-form :deep(.el-select),
.trip-form :deep(.el-date-editor),
.trip-form :deep(.el-textarea) {
  width: 100%;
}

.trip-form :deep(.el-form-item__label::after) {
  color: #f56c6c;
}

.allowance-dialog :deep(.el-dialog__body) {
  padding: 0 16px 12px;
}

.allowance-dialog-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 20px;
  min-height: 610px;
}

.allowance-side {
  border-right: 1px solid #ebeef5;
  padding: 8px 20px 0 0;
}

.side-row {
  display: grid;
  grid-template-columns: 80px 1fr;
  align-items: center;
  min-height: 32px;
  margin-bottom: 6px;
  color: #111827;
}

.side-row strong,
.timeline-card strong {
  font-weight: 400;
}

.timeline-card {
  margin-top: 10px;
  border: 1px solid #ebeef5;
  padding: 12px;
}

.timeline-row,
.timeline-route {
  display: grid;
  grid-template-columns: 78px 1fr;
  align-items: center;
  min-height: 38px;
}

.timeline-route {
  background: #008dd6;
  color: #ffffff;
  padding: 0 8px;
}

.timeline-route strong {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.amount-card {
  margin-top: 30px;
  border: 1px solid #ebeef5;
  padding: 16px;
}

.amount-card div {
  display: grid;
  grid-template-columns: 76px 44px 1fr;
  align-items: baseline;
  min-height: 40px;
}

.amount-card em {
  color: #374151;
  font-style: normal;
}

.amount-card strong {
  color: #ff5b00;
  font-size: 18px;
  font-weight: 400;
  text-align: right;
}

.calendar-heading {
  display: flex;
  height: 34px;
  align-items: center;
  justify-content: space-between;
}

.calendar-heading h3 {
  margin: 0;
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.calendar-table {
  width: 100%;
}

.calendar-table :deep(.el-table__header th) {
  background: #f5f7fa;
  color: #1f2937;
  font-weight: 600;
}

.date-cell {
  display: inline-grid;
  grid-template-columns: auto auto;
  gap: 2px 6px;
  align-items: center;
  justify-content: center;
}

.date-cell :deep(.el-checkbox) {
  grid-row: 1 / span 2;
  grid-column: 2;
}

.calendar-head-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.amount-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.standard-text {
  color: #ff5b00;
}

.amount-editor {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.amount-editor :deep(.el-input-number) {
  width: 86px;
}

.amount-editor :deep(.el-input__inner) {
  text-align: right;
}
</style>
