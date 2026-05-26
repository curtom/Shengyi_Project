import { ref } from 'vue';

import { businessTypeOptions, mockReimburseForms, mockReimburseList, projectOptions, reimCompanyOptions, reimDepartmentOptions, reimburserOptions } from '@/mock';
import type { DocumentStatusCode, ReimburseForm, ReimburseListItem } from '@/types/reimburse';

const documentStatusMap: Record<DocumentStatusCode, string> = {
  0: '草稿',
  1: '已完成',
  2: '已作废',
};

const clonePlain = <T>(value: T): T => JSON.parse(JSON.stringify(value)) as T;

const reimburseForms = ref<ReimburseForm[]>(clonePlain(mockReimburseForms));
const reimburseList = ref<ReimburseListItem[]>(clonePlain(mockReimburseList));

const formatDate = (date: Date): string => {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const createReimbursementNo = (): string => {
  const datePart = formatDate(new Date()).replace(/-/g, '');
  const sequence = `${reimburseList.value.length + 1}`.padStart(3, '0');
  return `RCBX${datePart}${sequence}`;
};

const createBlankReimburseForm = (): ReimburseForm => ({
  id: `detail-${Date.now()}`,
  reimbursementNo: undefined,
  documentStatusCode: 0,
  title: '',
  reason: '',
  submitDate: formatDate(new Date()),
  reimburserId: reimburserOptions[0]?.reimburserId ?? '',
  departmentId: reimDepartmentOptions[0]?.reimDepartmentId ?? '',
  companyId: reimCompanyOptions[0]?.reimCompanyId ?? '',
  businessTypeId: businessTypeOptions.find((item) => item.thereSubordinateNode === '0')?.businessTypeId ?? businessTypeOptions[0]?.businessTypeId ?? '',
  trips: [],
  allowances: [],
  allocations: [
    {
      id: `allocation-${Date.now()}`,
      companyId: reimCompanyOptions[0]?.reimCompanyId ?? '',
      projectId: projectOptions[0]?.projectId ?? '',
      ratio: 1,
      amount: 0,
    },
  ],
  remark: '',
});

const getReimburseDetail = (id: string): ReimburseForm => {
  if (id === 'new') {
    return createBlankReimburseForm();
  }

  return clonePlain(reimburseForms.value.find((item) => item.id === id) ?? reimburseForms.value[0] ?? createBlankReimburseForm());
};

const buildListItem = (form: ReimburseForm, allowanceAmount: number): ReimburseListItem => {
  const reimburser = reimburserOptions.find((item) => item.reimburserId === form.reimburserId) ?? reimburserOptions[0];
  const department = reimDepartmentOptions.find((item) => item.reimDepartmentId === form.departmentId) ?? reimDepartmentOptions[0];
  const company = reimCompanyOptions.find((item) => item.reimCompanyId === form.companyId) ?? reimCompanyOptions[0];
  const businessType = businessTypeOptions.find((item) => item.businessTypeId === form.businessTypeId) ?? businessTypeOptions[0];

  return {
    id: form.id,
    reimbursementNo: form.reimbursementNo ?? '',
    documentStatusCode: form.documentStatusCode,
    documentStatusName: documentStatusMap[form.documentStatusCode],
    documentType: '日常报销单',
    reimburser,
    department,
    company,
    businessType,
    title: form.title,
    reason: form.reason,
    allowanceAmount,
    createdAt: form.submitDate,
  };
};

const upsertSubmittedReimburse = (form: ReimburseForm, allowanceAmount: number) => {
  const nextForm: ReimburseForm = clonePlain({
    ...form,
    reimbursementNo: form.reimbursementNo || createReimbursementNo(),
    documentStatusCode: 1,
    submitDate: formatDate(new Date()),
  });

  const formIndex = reimburseForms.value.findIndex((item) => item.id === nextForm.id);
  if (formIndex >= 0) {
    reimburseForms.value.splice(formIndex, 1, nextForm);
  } else {
    reimburseForms.value.unshift(nextForm);
  }

  const nextListItem = buildListItem(nextForm, allowanceAmount);
  const listIndex = reimburseList.value.findIndex((item) => item.id === nextForm.id);
  if (listIndex >= 0) {
    reimburseList.value.splice(listIndex, 1, nextListItem);
  } else {
    reimburseList.value.unshift(nextListItem);
  }
};

export const useReimburseStore = () => ({
  reimburseList,
  getReimburseDetail,
  upsertSubmittedReimburse,
});
