export type DocumentStatusCode = 0 | 1 | 2;

export interface ReimCompanyOption {
  reimCompanyId: string;
  reimCompanyNo: string;
  reimCompanyName: string;
}

export interface ReimDepartmentOption {
  reimDepartmentId: string;
  reimDepartmentNo: string;
  reimDepartmentName: string;
}

export interface ReimburserOption {
  reimburserId: string;
  reimburserNo: string;
  reimburserName: string;
}

export interface BusinessTypeOption {
  businessTypeId: string;
  businessTypeNo: string;
  businessTypeName: string;
  thereSubordinateNode: '0' | '1';
  superiorId: string;
}

export interface BusinessTypeTreeNode extends BusinessTypeOption {
  label: string;
  value: string;
  children?: BusinessTypeTreeNode[];
}

export interface CityOption {
  cityNo: string;
  cityName: string;
  cityType: '1' | '2' | '3';
}

export interface ProjectOption {
  projectId: string;
  projectNo: string;
  projectName: string;
}

export interface ReimburseListItem {
  id: string;
  reimbursementNo: string;
  documentStatusCode: DocumentStatusCode;
  documentStatusName: string;
  documentType: string;
  reimburser: ReimburserOption;
  department: ReimDepartmentOption;
  company: ReimCompanyOption;
  businessType: BusinessTypeOption;
  title: string;
  reason: string;
  allowanceAmount: number;
  createdAt: string;
}

export interface ReimburseForm {
  id: string;
  reimbursementNo?: string;
  documentStatusCode: DocumentStatusCode;
  title: string;
  reason: string;
  submitDate: string;
  reimburserId: string;
  departmentId: string;
  companyId: string;
  businessTypeId: string;
  trips: TripRecord[];
  allowances: AllowanceRecord[];
  allocations: AllocationRecord[];
  remark: string;
}

export interface TripRecord {
  id: string;
  travelerId: string;
  departureCityNo: string;
  arrivalCityNo: string;
  startDate: string;
  endDate: string;
  description: string;
}

export interface AllowanceRecord {
  id: string;
  tripId: string;
  travelerId: string;
  startDate: string;
  endDate: string;
  days: number;
  routeText: string;
  allowanceCityNo: string;
  applicationAmount: number;
  allowanceAmount: number;
  calendar: AllowanceCalendarDay[];
}

export type AllowanceItemType = 'meal' | 'traffic' | 'communication';

export interface AllowanceAmountCell {
  itemType: AllowanceItemType;
  standardAmount: number;
  allowanceAmount: number;
  selected: boolean;
}

export interface AllowanceCalendarDay {
  id: string;
  date: string;
  weekday: string;
  allowanceCityNo: string;
  cells: Record<AllowanceItemType, AllowanceAmountCell>;
}

export interface AllocationRecord {
  id: string;
  companyId: string;
  projectId: string;
  ratio: number;
  amount: number;
}

export interface ReimburseQuery {
  reimbursementNo: string;
  title: string;
  reason: string;
  companyId: string;
  departmentId: string;
  reimburserId: string;
  businessTypeId: string;
}
