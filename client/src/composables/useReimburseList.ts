import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

import { useReimburseStore } from '@/stores/reimburseStore';
import type { ReimburseListItem, ReimburseQuery } from '@/types/reimburse';

export const useReimburseList = () => {
const router = useRouter();
const {
  reimburseList,
  listTotal,
  listLoading,
  businessTypeTreeOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
  loadMasterData,
  fetchReimburseList,
} = useReimburseStore();

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

const filteredList = computed(() => reimburseList.value);

const pagedList = computed(() => reimburseList.value);

const loadList = async () => {
  await fetchReimburseList(appliedQuery, currentPage.value, pageSize.value);
};

const applySearch = async () => {
  Object.assign(appliedQuery, searchForm);
  currentPage.value = 1;
  await loadList();
};

const clearSearch = async () => {
  Object.assign(searchForm, createEmptyQuery());
  Object.assign(appliedQuery, createEmptyQuery());
  currentPage.value = 1;
  await loadList();
};

const goDetail = (row: ReimburseListItem) => {
  router.push(`/reimburse/${row.id}`);
};

const createDocument = () => {
  router.push('/reimburse/new');
};

const notifyAction = async (label: string, row: ReimburseListItem) => {
  ElMessage.info(`${label}：${row.reimbursementNo}`);
};

const formatAmount = (amount: number): string => amount.toFixed(2);

const handleSizeChange = async (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  await loadList();
};

const handleCurrentChange = async () => {
  await loadList();
};

onMounted(async () => {
  await loadMasterData();
  await loadList();
});

return {
  searchForm,
  appliedQuery,
  currentPage,
  pageSize,
  listTotal,
  listLoading,
  filteredList,
  pagedList,
  applySearch,
  clearSearch,
  goDetail,
  createDocument,
  notifyAction,
  formatAmount,
  handleSizeChange,
  handleCurrentChange,
  businessTypeTreeOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
};
};
