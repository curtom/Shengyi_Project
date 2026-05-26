<script setup lang="ts">
import { useReimburseDetail } from '@/composables/useReimburseDetail';

const {
  detail,
  expanded,
  tripDialog,
  tripFormRef,
  tripForm,
  allowanceDialog,
  allowanceItemLabels,
  allowanceItemTypes,
  tripDialogTitle,
  reimburserName,
  tripRows,
  allowanceRows,
  amountTotals,
  currentAllowance,
  currentAllowanceTraveler,
  currentAllowanceCity,
  businessTypeName,
  currentAllowanceSummary,
  formatAmount,
  allocationRatioTotal,
  allocationAmountTotal,
  toggleSection,
  toTime,
  tripDateRangeText,
  tripCalendarVisible,
  tripCalendarMonthLabel,
  tripCalendarDays,
  changeTripCalendarMonth,
  selectTripCalendarDate,
  clearTripCalendarRange,
  tripRangeStart,
  tripRangeEnd,
  tripRules,
  openAllowanceDialog,
  closeAllowanceDialog,
  setAllowanceCellSelected,
  isAllowanceRowChecked,
  isAllowanceItemChecked,
  isAllowanceAllChecked,
  toggleAllowanceRow,
  toggleAllowanceItem,
  toggleAllowanceAll,
  handleAllowanceAmountChange,
  handleAllocationRatioChange,
  splitAllocationsEqually,
  addAllocation,
  deleteAllocation,
  deleteRemark,
  openTripDialog,
  closeTripDialog,
  saveTrip,
  deleteTrip,
  closeDocument,
  submitDocument,
  businessTypeTreeOptions,
  cityOptions,
  projectOptions,
  reimCompanyOptions,
  reimDepartmentOptions,
  reimburserOptions,
} = useReimburseDetail();
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
              <el-input v-model="detail.title" maxlength="500" />
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
              <el-input v-model="detail.reason" maxlength="500" placeholder="请输入" />
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
            <el-table-column min-width="180" align="right">
              <template #header>
                <span>分摊金额<span class="required-mark">*</span></span>
              </template>
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
          <el-popover
            v-model:visible="tripCalendarVisible"
            trigger="click"
            placement="top-start"
            width="430px"
            popper-class="trip-calendar-popper"
          >
            <template #reference>
              <el-input
                :model-value="tripDateRangeText"
                placeholder="请选择出发到达日期"
                readonly
                clearable
                @clear="clearTripCalendarRange"
              >
                <template #prefix>
                  <el-icon><Calendar /></el-icon>
                </template>
              </el-input>
            </template>
            <div class="single-calendar">
              <div class="calendar-nav">
                <button type="button" @click="changeTripCalendarMonth(-1)">
                  <el-icon><ArrowLeft /></el-icon>
                </button>
                <strong>{{ tripCalendarMonthLabel }}</strong>
                <button type="button" @click="changeTripCalendarMonth(1)">
                  <el-icon><ArrowRight /></el-icon>
                </button>
              </div>
              <div class="calendar-weekdays">
                <span>Sun</span>
                <span>Mon</span>
                <span>Tue</span>
                <span>Wed</span>
                <span>Thu</span>
                <span>Fri</span>
                <span>Sat</span>
              </div>
              <div class="calendar-grid">
                <button
                  v-for="day in tripCalendarDays"
                  :key="day.value"
                  class="calendar-day"
                  :class="{
                    'is-muted': !day.inMonth,
                    'is-disabled': day.disabled,
                    'is-start': day.value === tripRangeStart,
                    'is-end': day.value === tripRangeEnd,
                    'is-in-range':
                      tripRangeStart &&
                      tripRangeEnd &&
                      toTime(day.value) > toTime(tripRangeStart) &&
                      toTime(day.value) < toTime(tripRangeEnd),
                  }"
                  type="button"
                  :disabled="day.disabled"
                  @click="selectTripCalendarDate(day.value, day.disabled)"
                >
                  <span>{{ day.day }}</span>
                  <em v-if="day.value === tripRangeStart">出发</em>
                  <em v-if="day.value === tripRangeEnd">到达</em>
                </button>
              </div>
            </div>
          </el-popover>
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

.single-calendar {
  padding: 8px;
}

.calendar-nav {
  display: grid;
  grid-template-columns: 36px 1fr 36px;
  align-items: center;
  height: 40px;
  color: #4b5563;
}

.calendar-nav strong {
  font-size: 18px;
  font-weight: 500;
  text-align: center;
}

.calendar-nav button {
  display: inline-flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  border: 0;
  background: transparent;
  color: #4b5563;
  cursor: pointer;
}

.calendar-weekdays,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-weekdays {
  height: 34px;
  align-items: center;
  color: #606266;
  text-align: center;
}

.calendar-day {
  position: relative;
  height: 42px;
  border: 0;
  background: #ffffff;
  color: #606266;
  cursor: pointer;
  font: inherit;
}

.calendar-day:hover:not(.is-disabled) {
  background: #ecf5ff;
}

.calendar-day.is-muted {
  color: #a8abb2;
}

.calendar-day.is-disabled {
  background: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.calendar-day.is-in-range {
  background: #ecf5ff;
}

.calendar-day.is-start,
.calendar-day.is-end {
  background: #409eff;
  color: #ffffff;
}

.calendar-day em {
  position: absolute;
  top: 3px;
  right: 4px;
  font-size: 10px;
  font-style: normal;
  line-height: 1;
  transform: scale(0.9);
  transform-origin: right top;
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
