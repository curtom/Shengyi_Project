import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

import {
  businessTypeTreeOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
} from '@/mock';
import { useReimburseStore } from '@/stores/reimburseStore';
import type { ReimburseListItem, ReimburseQuery } from '@/types/reimburse';

export const useReimburseList = () => {
const router = useRouter();
const { reimburseList } = useReimburseStore();

const createEmptyQuery = (): ReimburseQuery => ({
  reimbursementNo: '',
  title: '',
  reason: '',
  companyId: '',
  departmentId: '',
  reimburserId: '',
  businessTypeId: '',
});

const searchForm = reactive<ReimburseQuery>(createEmptyQuery());
const appliedQuery = reactive<ReimburseQuery>(createEmptyQuery());
const currentPage = ref(1);
const pageSize = ref(10);

const matchesText = (source: string, keyword: string): boolean =>
  !keyword || source.toLowerCase().includes(keyword.trim().toLowerCase());

const filteredList = computed(() =>
  reimburseList.value.filter((item) => {
    const query = appliedQuery;

    return (
      matchesText(item.reimbursementNo, query.reimbursementNo) &&
      matchesText(item.title, query.title) &&
      matchesText(item.reason, query.reason) &&
      (!query.companyId || item.company.reimCompanyId === query.companyId) &&
      (!query.departmentId || item.department.reimDepartmentId === query.departmentId) &&
      (!query.reimburserId || item.reimburser.reimburserId === query.reimburserId) &&
      (!query.businessTypeId || item.businessType.businessTypeId === query.businessTypeId)
    );
  }),
);

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

const applySearch = () => {
  Object.assign(appliedQuery, searchForm);
  currentPage.value = 1;
};

const clearSearch = () => {
  Object.assign(searchForm, createEmptyQuery());
  Object.assign(appliedQuery, createEmptyQuery());
  currentPage.value = 1;
};

const goDetail = (row: ReimburseListItem) => {
  router.push(`/reimburse/${row.id}`);
};

const createDocument = () => {
  router.push('/reimburse/new');
};

const notifyAction = (label: string, row: ReimburseListItem) => {
  ElMessage.info(`${label}：${row.reimbursementNo}`);
};

const formatAmount = (amount: number): string => amount.toFixed(2);

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

return {
  searchForm,
  appliedQuery,
  currentPage,
  pageSize,
  filteredList,
  pagedList,
  applySearch,
  clearSearch,
  goDetail,
  createDocument,
  notifyAction,
  formatAmount,
  handleSizeChange,
  businessTypeTreeOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
};
};
