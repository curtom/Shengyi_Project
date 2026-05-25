<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

import {
  businessTypeTreeOptions,
  mockReimburseList,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
} from '@/mock';
import type { ReimburseListItem, ReimburseQuery } from '@/types/reimburse';

const router = useRouter();

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
  mockReimburseList.filter((item) => {
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
</script>

<template>
  <main class="page-shell reimburse-list-page">
    <section class="list-panel">
      <el-form :model="searchForm" label-width="96px" class="query-form">
        <div class="query-grid">
          <el-form-item label="报销单号">
            <el-input v-model="searchForm.reimbursementNo" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="事由">
            <el-input v-model="searchForm.reason" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="费用归属公司">
            <el-select v-model="searchForm.companyId" placeholder="请选择" clearable filterable>
              <el-option
                v-for="item in reimCompanyOptions"
                :key="item.reimCompanyId"
                :label="item.reimCompanyName"
                :value="item.reimCompanyId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="报销部门">
            <el-select v-model="searchForm.departmentId" placeholder="请选择" clearable filterable>
              <el-option
                v-for="item in reimDepartmentOptions"
                :key="item.reimDepartmentId"
                :label="item.reimDepartmentName"
                :value="item.reimDepartmentId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="报销人">
            <el-select v-model="searchForm.reimburserId" placeholder="请选择" clearable filterable>
              <el-option
                v-for="item in reimburserOptions"
                :key="item.reimburserId"
                :label="`${item.reimburserName}/${item.reimburserNo}`"
                :value="item.reimburserId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="业务类型">
            <el-tree-select
              v-model="searchForm.businessTypeId"
              :data="businessTypeTreeOptions"
              placeholder="请选择"
              clearable
              filterable
              check-strictly
              :render-after-expand="false"
            />
          </el-form-item>
          <div class="query-actions">
            <el-button type="primary" plain @click="createDocument">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
            <el-button plain @click="clearSearch">清除</el-button>
            <el-button type="primary" @click="applySearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </el-form>

      <el-table :data="pagedList" border class="reimburse-table" empty-text="暂无数据">
        <el-table-column type="index" width="54" align="center" />
        <el-table-column label="操作" width="120" fixed="left" align="center">
          <template #header>
            <el-icon class="table-tool-icon"><Operation /></el-icon>
          </template>
          <template #default="{ row }">
            <div class="operation-cell">
              <el-tooltip content="删除" placement="top">
                <el-button link class="muted-icon" @click="notifyAction('删除', row)">
                  <el-icon><DocumentRemove /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button link @click="goDetail(row)">
                  <el-icon><EditPen /></el-icon>
                </el-button>
              </el-tooltip>
              <el-dropdown trigger="hover" placement="bottom-start" @command="(command: string) => notifyAction(command, row)">
                <el-button link>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="删除">删除</el-dropdown-item>
                    <el-dropdown-item command="手工推送">手工推送</el-dropdown-item>
                    <el-dropdown-item command="复制">复制</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="报销单号" min-width="145">
          <template #default="{ row }">
            <button class="theme-link inline-button" type="button" @click="goDetail(row)">
              {{ row.reimbursementNo }}
            </button>
          </template>
        </el-table-column>
        <el-table-column prop="documentStatusName" label="单据状态" width="96" />
        <el-table-column prop="documentType" label="单据类型" width="112" />
        <el-table-column label="报销人" min-width="145">
          <template #default="{ row }">
            {{ row.reimburser.reimburserName }}/{{ row.reimburser.reimburserNo }}
          </template>
        </el-table-column>
        <el-table-column label="报销部门" min-width="155">
          <template #default="{ row }">
            [{{ row.department.reimDepartmentNo }}]{{ row.department.reimDepartmentName }}
          </template>
        </el-table-column>
        <el-table-column label="费用归属公司" min-width="160">
          <template #default="{ row }">
            {{ row.company.reimCompanyName }}
          </template>
        </el-table-column>
        <el-table-column label="业务类型" min-width="110">
          <template #default="{ row }">
            {{ row.businessType.businessTypeName }}
          </template>
        </el-table-column>
        <el-table-column label="报销标题" min-width="230" show-overflow-tooltip>
          <template #default="{ row }">
            <button class="theme-link inline-button title-link" type="button" @click="goDetail(row)">
              {{ row.title }}
            </button>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="报销事由" min-width="150" show-overflow-tooltip />
        <el-table-column label="补助金额" width="110" align="right" header-align="right">
          <template #default="{ row }">
            {{ formatAmount(row.allowanceAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="120" />
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredList.length"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
        />
      </div>
    </section>
  </main>
</template>

<style scoped>
.reimburse-list-page {
  padding: 20px;
}

.list-panel {
  min-height: calc(100vh - 40px);
  background: #ffffff;
  padding: 22px 20px 18px;
}

.query-form {
  margin-bottom: 18px;
}

.query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px 34px;
  align-items: start;
}

.query-grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.query-grid :deep(.el-select),
.query-grid :deep(.el-tree-select) {
  width: 100%;
}

.query-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  min-width: 0;
}

.reimburse-table {
  width: 100%;
}

.reimburse-table :deep(.el-table__header th) {
  background: #f5f7fa;
  color: #1f2937;
  font-weight: 600;
}

.table-tool-icon {
  color: #2f66ff;
  font-size: 20px;
  vertical-align: middle;
}

.operation-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.operation-cell :deep(.el-button) {
  color: #2f66ff;
  font-size: 16px;
}

.operation-cell :deep(.muted-icon) {
  color: #c5cbd3;
}

.inline-button {
  max-width: 100%;
  overflow: hidden;
  padding: 0;
  border: 0;
  background: transparent;
  font: inherit;
  text-align: left;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.title-link {
  display: block;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 180px;
}

@media (max-width: 1280px) {
  .query-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
