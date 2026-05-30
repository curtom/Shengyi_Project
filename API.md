# 差旅报销系统 - 后端接口文档

基础路径: `http://localhost:8081/api/reimbursement`

## 统一响应格式

```json
{
  "code": 200,
  "message": "成功",
  "data": ...
}
```

---

## 1. 分页查询报销单列表

**POST** `/api/reimbursement/page`

Content-Type: `application/json`

### 请求体 (ReimbursementQueryReq)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10 |
| reimbursementNo | String | 否 | 报销单号（模糊查询） |
| title | String | 否 | 报销标题（模糊查询） |
| reason | String | 否 | 出差事由（模糊查询） |
| companyId | String | 否 | 费用归属公司 ID |
| departmentId | String | 否 | 报销部门 ID |
| reimburserId | String | 否 | 报销人 ID |
| businessTypeId | String | 否 | 业务类型 ID |

### 响应 data (PageResult)

```json
{
  "total": 100,
  "pages": 10,
  "current": 1,
  "size": 10,
  "records": [ReimbursementDTO]
}
```

records 中每条为 ReimbursementDTO（列表简要字段）:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 报销单 ID |
| reimbursementNo | String | 报销单号 |
| documentStatusCode | Integer | 单据状态：0=草稿, 1=已提交, 2=已作废 |
| title | String | 报销标题 |
| reason | String | 出差事由 |
| reimburserId | String | 报销人 ID |
| reimburserNo | String | 报销人工号 |
| reimburserName | String | 报销人姓名 |
| departmentId | String | 部门 ID |
| reimDepartmentNo | String | 部门编号 |
| reimDepartmentName | String | 部门名称 |
| companyId | String | 公司 ID |
| reimCompanyNo | String | 公司编号 |
| reimCompanyName | String | 公司名称 |
| businessTypeId | String | 业务类型 ID |
| businessTypeNo | String | 业务类型编号 |
| businessTypeName | String | 业务类型名称 |
| subsidyTotal | BigDecimal | 补助合计 |

---

## 2. 查询报销单详情

**GET** `/api/reimbursement/{id}`

### 路径参数

- `id` - 报销单 ID

### 响应 data

完整 ReimbursementDTO，除列表字段外还包含：

| 字段 | 类型 | 说明 |
|------|------|------|
| submitDate | String | 提交日期 |
| remark | String | 备注 |
| mealAllowance | BigDecimal | 餐补合计 |
| transportationAllowance | BigDecimal | 交通补贴合计 |
| phoneAllowance | BigDecimal | 通讯补贴合计 |
| trips | List\<ItineraryDTO\> | 行程列表 |
| allowances | List\<SubsidyDTO\> | 补助列表 |
| allocations | List\<AllocationDTO\> | 费用分摊列表 |

---

## 3. 保存草稿

**POST** `/api/reimbursement/save`

Content-Type: `application/json`

### 请求体

ReimbursementDTO（同详情结构，id 为空则新建，有值则更新）

必填校验: title, reason, reimburserId, departmentId, companyId, businessTypeId

行程中 travelerId, departureCityNo, arrivalCityNo, startDate, endDate, description 均为必填。

### 响应 data

`String` - 报销单 ID

---

## 4. 提交报销单

**POST** `/api/reimbursement/submit`

Content-Type: `application/json`

### 请求体

ReimbursementDTO（同保存草稿，但 trips 不允许为空）

### 额外校验

- 至少一条行程
- 同一出行人行程日期不能重叠
- 补助金额不能超过标准
- 费用分摊比例之和必须为 1
- 费用分摊金额之和必须等于补助合计

### 响应 data

`String` - 报销单 ID

---

## 5. 作废报销单

**POST** `/api/reimbursement/void/{id}`

### 路径参数

- `id` - 报销单 ID

### 响应 data

`null`

---

## 6. 预览补助日历

**POST** `/api/reimbursement/subsidy/preview`

Content-Type: `application/json`

### 请求体

`List<ItineraryDTO>`（行程列表）

### 响应 data

`List<SubsidyCalendarDTO>` - 按行程计算出的补助日历明细

---

## 7. 获取主数据（下拉选项）

**GET** `/api/reimbursement/master-data`

### 响应 data

`Map<String, Object>`，包含以下 key：

| Key | 类型 | 说明 |
|-----|------|------|
| reimCompanyOptions | List\<Map\> | 公司选项（reimCompanyId, reimCompanyNo, reimCompanyName） |
| reimDepartmentOptions | List\<Map\> | 部门选项（reimDepartmentId, reimDepartmentNo, reimDepartmentName） |
| reimburserOptions | List\<Map\> | 报销人选项（reimburserId, reimburserNo, reimburserName） |
| businessTypeOptions | List\<Map\> | 业务类型平铺选项 |
| businessTypeTreeOptions | List\<Map\> | 业务类型树形结构（含 label, value, children） |
| cityOptions | List\<Map\> | 城市选项（cityNo, cityName, cityType: 1=一线/2=二线/3=三线） |
| projectOptions | List\<Map\> | 项目选项（projectId, projectNo, projectName） |

---

## 附录: DTO 结构定义

### ItineraryDTO（行程）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 否 | 行程 ID（新建时不传） |
| travelerId | String | 是 | 出行人 ID |
| travelerNo | String | 否 | 出行人工号 |
| travelerName | String | 否 | 出行人姓名 |
| departureCityNo | String | 是 | 出发城市编号 |
| arrivalCityNo | String | 是 | 到达城市编号 |
| startDate | String | 是 | 出发日期 (yyyy-MM-dd) |
| endDate | String | 是 | 到达日期 (yyyy-MM-dd) |
| description | String | 是 | 行程说明 |

### SubsidyDTO（补助）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 补助 ID |
| tripId | String | 关联行程 ID |
| travelerId | String | 出行人 ID |
| travelerNo | String | 出行人工号 |
| travelerName | String | 出行人姓名 |
| startDate | String | 起始日期 |
| endDate | String | 截止日期 |
| days | Integer | 补助天数 |
| allowanceCityNo | String | 补助城市编号 |
| applicationAmount | BigDecimal | 申请金额 |
| allowanceAmount | BigDecimal | 补助金额 |
| mealAllowance | BigDecimal | 餐补 |
| transportationAllowance | BigDecimal | 交通补贴 |
| phoneAllowance | BigDecimal | 通讯补贴 |
| calendar | List\<SubsidyCalendarDTO\> | 补助日历明细 |

### SubsidyCalendarDTO（补助日历）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | ID |
| date | String | 日期 |
| weekday | String | 星期 |
| allowanceCityNo | String | 补助城市编号 |
| cells | Map\<String, AllowanceAmountCell\> | key 为 meal / traffic / communication |

#### AllowanceAmountCell

| 字段 | 类型 | 说明 |
|------|------|------|
| itemType | String | 类型：meal / traffic / communication |
| standardAmount | BigDecimal | 标准金额 |
| allowanceAmount | BigDecimal | 实际补助金额 |
| selected | Boolean | 是否选中 |

### AllocationDTO（费用分摊）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 分摊 ID |
| companyId | String | 分摊公司 ID |
| companyNo | String | 分摊公司编号 |
| companyName | String | 分摊公司名称 |
| projectId | String | 项目 ID |
| projectNo | String | 项目编号 |
| projectName | String | 项目名称 |
| ratio | BigDecimal | 分摊比例 (0~1) |
| amount | BigDecimal | 分摊金额 |

---
