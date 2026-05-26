# 差旅报销单前端工程

基于 Vue 3、TypeScript、Vite、Element Plus 和 Tailwind CSS 实现的差旅报销单 Mock 前端。当前版本用于还原概要设计文档中的列表页、详情页、补录行程、补助日历、费用分摊和提交校验等交互，后续可替换 Mock 数据接入真实后端接口。

## 技术栈

- Vue 3
- TypeScript
- Vite
- Vue Router
- Element Plus
- Tailwind CSS

## 本地运行

```bash
npm install
npm run dev
```

默认开发服务会监听 `0.0.0.0`，本机访问地址通常为：

```text
http://localhost:5173/
```

## 常用脚本

```bash
npm run dev
```

启动 Vite 开发服务。

```bash
npm run typecheck
```

执行 TypeScript 类型检查。

```bash
npm run build
```

执行类型检查并生成生产构建产物。

```bash
npm run preview
```

预览生产构建结果。

## 目录结构

```text
src/
  assets/                 静态资源
  components/             通用组件预留目录
  composables/            页面业务逻辑组合函数
    useReimburseDetail.ts 报销单详情页逻辑
    useReimburseList.ts   报销单列表页逻辑
  mock/                   Mock 数据与静态下拉数据
    options.ts            文档 5.3 下拉选项数据
    reimburse.ts          报销单、行程、补助 Mock 数据
  pages/                  路由页面壳组件
  router/                 路由配置
  stores/                 前端内存状态
    reimburseStore.ts     报销单 Mock 仓库，预留接口替换入口
  styles/                 全局样式
  types/                  TypeScript 类型定义
  utils/                  工具函数预留目录
  views/                  页面视图组件
```

## 页面路由

| 路径 | 说明 |
| --- | --- |
| `/` | 重定向到报销单列表 |
| `/reimburse` | 报销单列表页 |
| `/reimburse/new` | 新增报销单页 |
| `/reimburse/:id` | 报销单详情页 |

## 数据说明

- 下拉框数据写在 `src/mock/options.ts`，字段名保持概要设计文档中的命名，例如 `reimCompanyId`、`reimDepartmentId`、`reimburserId`、`businessTypeId`、`cityType`、`projectId`。
- 业务类型在 Mock 层由扁平数据转换为树形结构，供 `el-tree-select` 使用。
- 报销单列表、详情、行程和补助数据写在 `src/mock/reimburse.ts`。
- 新增、提交、列表刷新等前端内存态逻辑集中在 `src/stores/reimburseStore.ts`，后续接后端时优先替换这里的查询和提交函数。

## 已实现功能

- 报销单列表查询、清除、新增、分页、报销单号/标题跳转详情。
- 报销单详情固定头部和底部操作区。
- 基础信息、补录行程、补助信息、费用合计、费用归属及分摊、备注信息分区展示。
- 分区展开/收起。
- 补录行程新增、编辑、复制、删除确认。
- 行程日期选择、必填校验、不可晚于当前日期、同人员日期不可重复校验。
- 根据行程生成补助信息和补助日历。
- 补助日历全选、按日期选择、按补助项选择、金额限制和汇总。
- 餐费补助按城市类型计算：一线 `100/天`，二线 `80/天`，三线 `50/天`；交通和通讯均为 `40/天`。
- 费用分摊添加、删除确认、至少保留一条、均摊、首行自动反算。
- 备注 `1000` 字限制和删除确认。
- 提交前校验必填项、行程重复、分摊比例合计 `100%`、分摊金额合计等于补助总金额。
- 新增报销单提交后会写入前端 Mock 仓库并返回列表展示。

## 接口替换建议

当前没有真实后端接口，后续接入时建议保留页面和 composable 的调用方式，优先替换 `src/stores/reimburseStore.ts`：

- `reimburseList` 替换为列表查询接口返回结果。
- `getReimburseDetail(id)` 替换为详情查询接口。
- `upsertSubmittedReimburse(form, allowanceAmount)` 替换为新增/提交接口。

如果后端返回字段与当前类型不完全一致，建议在 store 或独立 adapter 中完成字段转换，避免页面组件直接依赖接口原始结构。

## 注意事项

- 当前数据仅保存在浏览器运行时内存中，刷新页面后会回到初始 Mock 数据。
- 暂未实现登录、权限、真实审批流、打印和附件上传。
- 生产构建时如果出现 Vite chunk 体积提示，一般不影响运行；后续可按路由或 Element Plus 组件做进一步拆包优化。
