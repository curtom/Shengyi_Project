<script setup lang="ts">
import { useReimburseList } from '@/composables/useReimburseList';

const {
  searchForm,
  currentPage,
  pageSize,
  listTotal,
  listLoading,
  pagedList,
  applySearch,
  clearSearch,
  goDetail,
  createDocument,
  deleteDocument,
  handleDropdownCommand,
  formatAmount,
  handleSizeChange,
  handleCurrentChange,
  businessTypeTreeOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
} = useReimburseList();
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
            <el-button class="query-btn-outline" @click="createDocument">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
            <el-button class="query-btn-outline" @click="clearSearch">清除</el-button>
            <el-button class="query-btn-solid" @click="applySearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </el-form>

      <el-table v-loading="listLoading" :data="pagedList" border class="reimburse-table" empty-text="暂无数据">
        <el-table-column type="index" width="54" align="center">
          <template #header>
            <el-icon class="table-tool-icon"><Operation /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="118" align="center" header-align="center">
          <template #default="{ row }">
            <div class="operation-cell">
              <el-tooltip content="删除" placement="top">
                <el-button link @click="deleteDocument(row)">
                  <el-icon><DocumentRemove /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-button
                  link
                  :class="{ 'muted-icon': row.documentStatusCode !== 0 && row.documentStatusCode !== 1 }"
                  :disabled="row.documentStatusCode !== 0 && row.documentStatusCode !== 1"
                  @click="goDetail(row)"
                >
                  <el-icon><EditPen /></el-icon>
                </el-button>
              </el-tooltip>
              <el-dropdown trigger="hover" placement="bottom-start" @command="(command: string) => handleDropdownCommand(command, row)">
                <el-button link>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="作废">作废</el-dropdown-item>
                    <el-dropdown-item command="手工推送">手工推送</el-dropdown-item>
                    <el-dropdown-item command="复制">复制</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="报销单号" width="168" align="center" header-align="center">
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
          :total="listTotal"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
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

.query-actions :deep(.el-button) {
  min-width: 72px;
  height: 32px;
  padding: 0 16px;
  border-radius: 4px;
  font-size: 14px;
}

.query-actions :deep(.el-button.query-btn-outline) {
  color: #2f66ff;
  border: 1px solid #2f66ff;
  background: #ffffff;
}

.query-actions :deep(.el-button.query-btn-outline:hover),
.query-actions :deep(.el-button.query-btn-outline:focus) {
  color: #2f66ff;
  border-color: #2f66ff;
  background: #eef3ff;
}

.query-actions :deep(.el-button.query-btn-solid) {
  color: #ffffff;
  border: 1px solid #2f66ff;
  background: #2f66ff;
}

.query-actions :deep(.el-button.query-btn-solid:hover),
.query-actions :deep(.el-button.query-btn-solid:focus) {
  color: #ffffff;
  border-color: #2452cc;
  background: #2452cc;
}

.reimburse-table {
  width: 100%;
}

.reimburse-table :deep(.el-table__cell) {
  height: 39px;
  padding: 0;
}

.reimburse-table :deep(.el-table__header th) {
  background: #f7f8fa;
  color: #1f2937;
  font-weight: 600;
}

.table-tool-icon {
  color: #2f66ff;
  font-size: 19px;
  vertical-align: middle;
}

.operation-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.operation-cell :deep(.el-button) {
  color: #2f66ff;
  width: 16px;
  height: 16px;
  font-size: 16px;
  padding: 0;
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
  text-align: center;
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
