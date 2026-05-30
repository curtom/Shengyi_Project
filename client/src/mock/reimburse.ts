import type {
  AllowanceCalendarDay,
  AllowanceItemType,
  AllowanceRecord,
  ReimburseForm,
  ReimburseListItem,
  TripRecord,
} from '@/types/reimburse';
import {
  businessTypeOptions,
  cityOptions,
  projectOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
} from './options';

const documentStatusMap = {
  0: '草稿',
  1: '已完成',
  2: '已作废',
} as const;

const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];

const getDateRange = (startDate: string, endDate: string): string[] => {
  const result: string[] = [];
  const current = new Date(`${startDate}T00:00:00`);
  const end = new Date(`${endDate}T00:00:00`);

  while (current <= end) {
    const year = current.getFullYear();
    const month = `${current.getMonth() + 1}`.padStart(2, '0');
    const day = `${current.getDate()}`.padStart(2, '0');
    result.push(`${year}-${month}-${day}`);
    current.setDate(current.getDate() + 1);
  }

  return result;
};

const mealStandardByCityType = {
  1: 100,
  2: 80,
  3: 50,
} as const;

export const getAllowanceStandard = (
  cityNo: string,
  itemType: AllowanceItemType,
): number => {
  if (itemType === 'traffic' || itemType === 'communication') {
    return 40;
  }

  const city = cityOptions.find((item) => item.cityNo === cityNo);
  return mealStandardByCityType[city?.cityType ?? '3'];
};

export const createAllowanceCalendar = (
  tripId: string,
  startDate: string,
  endDate: string,
  allowanceCityNo: string,
): AllowanceCalendarDay[] =>
  getDateRange(startDate, endDate).map((date) => {
    const itemTypes: AllowanceItemType[] = ['meal', 'traffic', 'communication'];

    return {
      id: `${tripId}-${date}`,
      date,
      weekday: weekdays[new Date(`${date}T00:00:00`).getDay()],
      allowanceCityNo,
      cells: itemTypes.reduce(
        (cells, itemType) => {
          const standardAmount = getAllowanceStandard(allowanceCityNo, itemType);
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
    };
  });

export const createAllowanceFromTrip = (trip: TripRecord): AllowanceRecord => {
  const startCity = cityOptions.find((item) => item.cityNo === trip.departureCityNo);
  const endCity = cityOptions.find((item) => item.cityNo === trip.arrivalCityNo);
  const calendar = createAllowanceCalendar(trip.id, trip.startDate, trip.endDate, trip.arrivalCityNo);

  return {
    id: `allowance-${trip.id}`,
    tripId: trip.id,
    travelerId: trip.travelerId,
    startDate: trip.startDate,
    endDate: trip.endDate,
    days: calendar.length,
    routeText: `${startCity?.cityName ?? ''}-${endCity?.cityName ?? ''}`,
    allowanceCityNo: trip.arrivalCityNo,
    applicationAmount: 0,
    allowanceAmount: 0,
    calendar,
  };
};

const mockTrips: TripRecord[] = [
  {
    id: 'trip-001',
    travelerId: reimburserOptions[0].reimburserId,
    departureCityNo: '10458',
    arrivalCityNo: '10119',
    startDate: '2026-04-13',
    endDate: '2026-04-17',
    description: '行程说明',
  },
];

export const mockReimburseForms: ReimburseForm[] = [
  {
    id: 'detail-001',
    reimbursementNo: 'RCBX20260515002',
    documentStatusCode: 0,
    title: '徐年年项目出差',
    reason: '项目实施现场沟通与客户培训',
    submitDate: '2026-04-23',
    reimburserId: reimburserOptions[0].reimburserId,
    departmentId: reimDepartmentOptions[0].reimDepartmentId,
    companyId: reimCompanyOptions[2].reimCompanyId,
    businessTypeId: businessTypeOptions[2].businessTypeId,
    trips: mockTrips,
    allowances: mockTrips.map(createAllowanceFromTrip),
    allocations: [
      {
        id: 'allocation-001',
        companyId: reimCompanyOptions[2].reimCompanyId,
        projectId: projectOptions[0].projectId,
        ratio: 1,
        amount: 0,
      },
    ],
    remark: '',
  },
];

const createListItem = (
  id: string,
  reimbursementNo: string,
  title: string,
  reason: string,
  statusCode: 0 | 1 | 2,
  createdAt: string,
  allowanceAmount = 0,
): ReimburseListItem => ({
  id,
  reimbursementNo,
  documentStatusCode: statusCode,
  documentStatusName: documentStatusMap[statusCode],
  documentType: '日常报销单',
  reimburser: reimburserOptions[0],
  department: reimDepartmentOptions[0],
  company: reimCompanyOptions[2],
  businessType: businessTypeOptions[2],
  title,
  reason,
  allowanceAmount,
  createdAt,
});

export const mockReimburseList: ReimburseListItem[] = [
  createListItem('detail-001', 'RCBX20260515002', '日常报销单模板 - 副本，40.00CNY，...', '项目实施现场沟通与客户培训', 1, '2026-05-15'),
  createListItem('detail-002', 'RCBX20260513014', '日常报销单模板 - 副本，123.00CNY...', '客户拜访', 1, '2026-05-13'),
  createListItem('detail-003', 'RCBX20260513013', '日常报销单模板 - 副本，333.00CNY...', '市场拓展', 1, '2026-05-13'),
  createListItem('detail-004', 'RCBX20260512003', '日常报销单模板 - 副本，3.00CNY，...', '售后维护', 1, '2026-05-12'),
  createListItem('detail-005', 'RCBX20260429076', '测试', '日常办公', 0, '2026-04-29'),
  createListItem('detail-006', 'RCBX20260429075', '测试', '日常办公', 0, '2026-04-29'),
  createListItem('detail-007', 'RCBX20260429074', '差旅补助补录测试', '会议差旅', 0, '2026-04-29'),
  createListItem('detail-008', 'RCBX20260429073', '这个法人公司的名字可能会有点长是我...', '客户招待', 0, '2026-04-29'),
  createListItem('detail-009', 'RCBX20260429072', '已作废测试单据', '客户招待', 2, '2026-04-29'),
  createListItem('detail-010', 'RCBX20260429071', '已作废差旅单据', '客户招待', 2, '2026-04-29'),
];

export const getMockReimburseDetail = (id: string): ReimburseForm =>
  mockReimburseForms.find((item) => item.id === id) ?? mockReimburseForms[0];
