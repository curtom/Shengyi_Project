import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
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
  deleteReimburseForm,
  voidReimburseForm,
  pushReimburseForm,
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

const deleteDocument = async (row: ReimburseListItem) => {
  try {
    await ElMessageBox.confirm(`确认删除报销单 ${row.reimbursementNo}？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch {
    return;
  }

  await deleteReimburseForm(row.id);
  ElMessage.success('删除成功');

  if (reimburseList.value.length === 1 && currentPage.value > 1) {
    currentPage.value -= 1;
  }
  await loadList();
};

const voidDocument = async (row: ReimburseListItem) => {
  try {
    await ElMessageBox.confirm(`确认作废报销单 ${row.reimbursementNo}？`, '作废确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
  } catch {
    return;
  }

  await voidReimburseForm(row.id);
  ElMessage.success('作废成功');
  await loadList();
};

const pushDocument = async (row: ReimburseListItem) => {
  if (row.documentStatusCode !== 1) {
    ElMessage.warning('仅已完成状态的报销单可执行手工推送');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确认将报销单 ${row.reimbursementNo} 推送为已提交状态？`,
      '手工推送',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    );
  } catch {
    return;
  }

  await pushReimburseForm(row.id);
  ElMessage.success('推送成功');
  await loadList();
};

const handleDropdownCommand = async (command: string, row: ReimburseListItem) => {
  if (command === '删除') {
    await deleteDocument(row);
    return;
  }

  if (command === '作废') {
    await voidDocument(row);
    return;
  }

  if (command === '手工推送') {
    await pushDocument(row);
    return;
  }

  await notifyAction(command, row);
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
  deleteDocument,
  voidDocument,
  handleDropdownCommand,
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
