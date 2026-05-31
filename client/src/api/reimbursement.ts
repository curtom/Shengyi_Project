import { request } from '@/utils/request';
import type {
  AllocationRecord,
  AllowanceCalendarDay,
  AllowanceItemType,
  AllowanceRecord,
  BusinessTypeOption,
  BusinessTypeTreeNode,
  CityOption,
  DocumentStatusCode,
  ProjectOption,
  ReimCompanyOption,
  ReimDepartmentOption,
  ReimburseForm,
  ReimburseListItem,
  ReimburseQuery,
  ReimburserOption,
  TripRecord,
} from '@/types/reimburse';

export interface MasterDataResponse {
  reimCompanyOptions: ReimCompanyOption[];
  reimDepartmentOptions: ReimDepartmentOption[];
  reimburserOptions: ReimburserOption[];
  businessTypeOptions: BusinessTypeOption[];
  businessTypeTreeOptions: BusinessTypeTreeNode[];
  cityOptions: CityOption[];
  projectOptions: ProjectOption[];
}

export interface ReimbursementPageRequest extends ReimburseQuery {
  current: number;
  size: number;
}

interface PageResult<T> {
  total: number;
  pages: number;
  current: number;
  size: number;
  records: T[];
}

interface ReimbursementDTO {
  id?: string;
  reimbursementNo?: string;
  documentStatusCode?: DocumentStatusCode;
  title?: string;
  reason?: string;
  submitDate?: string;
  remark?: string;
  reimburserId?: string;
  reimburserNo?: string;
  reimburserName?: string;
  departmentId?: string;
  reimDepartmentNo?: string;
  reimDepartmentName?: string;
  companyId?: string;
  reimCompanyNo?: string;
  reimCompanyName?: string;
  businessTypeId?: string;
  businessTypeNo?: string;
  businessTypeName?: string;
  subsidyTotal?: number;
  mealAllowance?: number;
  transportationAllowance?: number;
  phoneAllowance?: number;
  trips?: TripRecord[];
  allowances?: AllowanceRecord[];
  allocations?: AllocationRecord[];
}

const statusTextMap: Record<DocumentStatusCode, string> = {
  0: '草稿',
  1: '已完成',
  2: '已作废',
  3: '已提交',
};

const normalizeStatus = (statusCode?: DocumentStatusCode): DocumentStatusCode => statusCode ?? 0;
const allowanceItemTypes: AllowanceItemType[] = ['meal', 'traffic', 'communication'];

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

const normalizeAllowance = (allowance: AllowanceRecord): AllowanceRecord => ({
  ...allowance,
  routeText: allowance.routeText ?? '',
  applicationAmount: Number(allowance.applicationAmount ?? 0),
  allowanceAmount: Number(allowance.allowanceAmount ?? 0),
  mealAllowance: Number(allowance.mealAllowance ?? 0),
  transportationAllowance: Number(allowance.transportationAllowance ?? 0),
  phoneAllowance: Number(allowance.phoneAllowance ?? 0),
  days: Number(allowance.days ?? allowance.calendar?.length ?? 0),
  calendar: (allowance.calendar ?? []).map(normalizeCalendarDay),
});

const getAllowanceTotals = (allowances: AllowanceRecord[]) =>
  allowances.reduce(
    (totals, allowance) => {
      const calendar = allowance.calendar ?? [];
      calendar.forEach((day) => {
        totals.mealAllowance += day.cells?.meal?.allowanceAmount ?? 0;
        totals.transportationAllowance += day.cells?.traffic?.allowanceAmount ?? 0;
        totals.phoneAllowance += day.cells?.communication?.allowanceAmount ?? 0;
      });
      return totals;
    },
    {
      mealAllowance: 0,
      transportationAllowance: 0,
      phoneAllowance: 0,
    },
  );

const roundAmount = (amount: number): number => Number(amount.toFixed(2));

const normalizeSubmitAllowance = (allowance: AllowanceRecord): AllowanceRecord => {
  const totals = getAllowanceTotals([allowance]);
  const allowanceAmount = roundAmount(totals.mealAllowance + totals.transportationAllowance + totals.phoneAllowance);

  return {
    ...allowance,
    applicationAmount: allowanceAmount,
    allowanceAmount,
    mealAllowance: roundAmount(totals.mealAllowance),
    transportationAllowance: roundAmount(totals.transportationAllowance),
    phoneAllowance: roundAmount(totals.phoneAllowance),
  };
};

const toListItem = (dto: ReimbursementDTO): ReimburseListItem => {
  const documentStatusCode = normalizeStatus(dto.documentStatusCode);

  return {
    id: dto.id ?? '',
    reimbursementNo: dto.reimbursementNo ?? '',
    documentStatusCode,
    documentStatusName: statusTextMap[documentStatusCode],
    documentType: '日常报销单',
    reimburser: {
      reimburserId: dto.reimburserId ?? '',
      reimburserNo: dto.reimburserNo ?? '',
      reimburserName: dto.reimburserName ?? '',
    },
    department: {
      reimDepartmentId: dto.departmentId ?? '',
      reimDepartmentNo: dto.reimDepartmentNo ?? '',
      reimDepartmentName: dto.reimDepartmentName ?? '',
    },
    company: {
      reimCompanyId: dto.companyId ?? '',
      reimCompanyNo: dto.reimCompanyNo ?? '',
      reimCompanyName: dto.reimCompanyName ?? '',
    },
    businessType: {
      businessTypeId: dto.businessTypeId ?? '',
      businessTypeNo: dto.businessTypeNo ?? '',
      businessTypeName: dto.businessTypeName ?? '',
      superiorId: '',
      thereSubordinateNode: '0',
    },
    title: dto.title ?? '',
    reason: dto.reason ?? '',
    allowanceAmount: Number(dto.subsidyTotal ?? (Number(dto.mealAllowance ?? 0) + Number(dto.transportationAllowance ?? 0) + Number(dto.phoneAllowance ?? 0))),
    createdAt: dto.submitDate ?? '',
  };
};

const toForm = (dto: ReimbursementDTO): ReimburseForm => ({
  id: dto.id ?? '',
  reimbursementNo: dto.reimbursementNo,
  documentStatusCode: normalizeStatus(dto.documentStatusCode),
  title: dto.title ?? '',
  reason: dto.reason ?? '',
  submitDate: dto.submitDate ?? '',
  reimburserId: dto.reimburserId ?? '',
  departmentId: dto.departmentId ?? '',
  companyId: dto.companyId ?? '',
  businessTypeId: dto.businessTypeId ?? '',
  trips: dto.trips ?? [],
  allowances: (dto.allowances ?? []).map(normalizeAllowance),
  allocations: dto.allocations ?? [],
  remark: dto.remark ?? '',
});

const toDto = (form: ReimburseForm): ReimbursementDTO => {
  const allowances = form.allowances.map(normalizeSubmitAllowance);
  const totals = getAllowanceTotals(allowances);
  const subsidyTotal = roundAmount(totals.mealAllowance + totals.transportationAllowance + totals.phoneAllowance);

  return {
    id: form.id || undefined,
    reimbursementNo: form.reimbursementNo,
    documentStatusCode: form.documentStatusCode,
    title: form.title,
    reason: form.reason,
    submitDate: form.submitDate,
    reimburserId: form.reimburserId,
    departmentId: form.departmentId,
    companyId: form.companyId,
    businessTypeId: form.businessTypeId,
    subsidyTotal,
    mealAllowance: roundAmount(totals.mealAllowance),
    transportationAllowance: roundAmount(totals.transportationAllowance),
    phoneAllowance: roundAmount(totals.phoneAllowance),
    trips: form.trips,
    allowances,
    allocations: form.allocations.map((allocation) => ({
      ...allocation,
      ratio: Number(allocation.ratio.toFixed(4)),
      amount: roundAmount(allocation.amount),
    })),
    remark: form.remark,
  };
};

export const fetchMasterData = () => request<MasterDataResponse>('/master-data');

export const fetchReimbursementPage = async (params: ReimbursementPageRequest) => {
  const result = await request<PageResult<ReimbursementDTO>>('/page', {
    method: 'POST',
    body: JSON.stringify(params),
  });

  return {
    ...result,
    records: result.records.map(toListItem),
  };
};

export const fetchReimbursementDetail = async (id: string) => {
  const result = await request<ReimbursementDTO>(`/${id}`);
  return toForm(result);
};

export const saveReimbursement = (form: ReimburseForm) =>
  request<string>('/save', {
    method: 'POST',
    body: JSON.stringify(toDto(form)),
  });

export const submitReimbursement = (form: ReimburseForm) =>
  request<string>('/submit', {
    method: 'POST',
    body: JSON.stringify(toDto(form)),
  });

export const voidReimbursement = (id: string) =>
  request<null>(`/void/${id}`, {
    method: 'POST',
  });

export const deleteReimbursement = (id: string) =>
  request<null>(`/delete/${id}`, {
    method: 'POST',
  });

export const pushReimbursement = (id: string) =>
  request<null>(`/push/${id}`, {
    method: 'POST',
  });

export const withdrawReimbursement = (id: string) =>
  request<null>(`/withdraw/${id}`, {
    method: 'POST',
  });

export const previewSubsidy = (trips: TripRecord[]) =>
  request<AllowanceCalendarDay[]>('/subsidy/preview', {
    method: 'POST',
    body: JSON.stringify(trips),
  });
