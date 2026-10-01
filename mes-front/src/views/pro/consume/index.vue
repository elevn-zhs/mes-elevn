<template>
  <div class="pro-consume">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 ============ -->
      <template #header>
        <div class="card-header">
          <span>工序物料消耗</span>
          <div class="header-actions">
            <el-button :icon="Refresh" size="small" @click="loadConsumeList">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- ================================================================
           说明条：消耗记录不是"录"出来的，是报工倒冲出来的
           ================================================================ -->
      <div class="page-tip">
        消耗记录由报工自动倒冲生成：报工数量 × 制程BOM 单位用量 = 本次消耗。
        报工成功的那一刻，料账就已经落好，不需要车间单独开领料单。
        本页只提供查询、用料比对与单条误录删除，不提供新增入口。
      </div>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="工单编码">
          <el-input v-model="queryForm.workorderCode" placeholder="工单编码" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="任务编号">
          <el-input v-model="queryForm.taskCode" placeholder="任务编号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="流转卡号">
          <el-input v-model="queryForm.transOrderCode" placeholder="流转卡号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="物料">
          <el-input v-model="queryForm.itemCode" placeholder="物料编码" clearable style="width: 130px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="queryForm.itemName" placeholder="物料名称" clearable style="width: 130px" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="queryForm.batchCode" placeholder="批次号" clearable style="width: 130px" />
        </el-form-item>
        <el-form-item label="消耗时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 340px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- ============ 数据表格 ============ -->
      <el-table :data="tableData" border stripe>
        <el-table-column prop="transOrderCode" label="流转卡号" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.transOrderCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="workorderCode" label="工单编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="taskCode" label="任务编号" min-width="170" show-overflow-tooltip />
        <el-table-column label="工序" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag effect="plain" size="small">{{ row.processCode }}</el-tag>
            <span class="ml4">{{ row.processName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="workstationName" label="工作站" min-width="110" show-overflow-tooltip />
        <el-table-column label="物料" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">
            <div>{{ row.itemCode }} {{ row.itemName }}</div>
            <div class="text-muted">{{ row.specification || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="消耗数量" width="120" align="right">
          <template #default="{ row }">
            <b class="qty-consume">{{ formatQty(row.quantityConsumed) }}</b>
            <span class="text-muted"> {{ row.unitOfMeasure || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="dictTagType(sourceOptions, row.sourceDocType)" size="small">
              {{ dictLabel(sourceOptions, row.sourceDocType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消耗时间" width="160">
          <template #default="{ row }">{{ shortTime(row.consumeDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleShowTaskRequire(row)">本道用料</el-button>
            <el-button link type="primary" @click="handleShowWorkorderCompare(row)">工单比对</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ============ 分页组件 ============ -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- ================================================================
         用料比对弹窗：应耗 vs 已耗 vs 差额
         两种口径共用一个弹窗 —— 列一样，只是"应耗"的来源不同：
           本道用料：应耗 = 本道任务数量 × 单位用量
           工单比对：应耗 = 工单BOM 展开的预计使用量
         ================================================================ -->
    <el-dialog
      v-model="requireVisible"
      :title="requireTitle"
      width="860px"
      :close-on-click-modal="false"
    >
      <el-alert
        v-if="requireMode === 'task'"
        type="info"
        :closable="false"
        show-icon
        title="本道工序用料：应耗 = 本道排产数量 × 单位用量；已耗 = 该工序已发生的实际消耗"
        class="mb12"
      />
      <el-alert
        v-else
        type="info"
        :closable="false"
        show-icon
        title="工单用料比对：应耗 = 工单下达时按 BOM 展开的预计使用量；已耗 = 该工单实际累计消耗"
        class="mb12"
      />

      <el-table :data="requireRows" border size="small" :row-class-name="requireRowClass">
        <el-table-column prop="itemCode" label="物料编码" width="120" />
        <el-table-column prop="itemName" label="物料名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格型号" min-width="130" show-overflow-tooltip />
        <el-table-column label="单位用量" width="100" align="right">
          <template #default="{ row }">{{ formatQty(row.unitQty) }}</template>
        </el-table-column>
        <el-table-column label="应耗" width="110" align="right">
          <template #default="{ row }">
            <b>{{ formatQty(row.planQty) }}</b>
            <span class="text-muted"> {{ row.unitOfMeasure || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已耗" width="110" align="right">
          <template #default="{ row }">
            <span class="qty-consume">{{ formatQty(row.consumedQty) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="差额" width="100" align="right">
          <template #default="{ row }">
            <span :class="diffClass(row)">{{ formatQty(row.diffQty) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="diffTagType(row)" size="small">{{ diffLabel(row) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!requireRows.length" class="empty-tip">
        该工序在制程BOM 上没有配置用料（如测试、老化这类不投料的工序），或者这单还没产生消耗。
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, unref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { getConsumeByPage, getMaterialRequire, getWorkorderMaterialCompare, deleteConsume } from '@/api/pro/consume.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'
import { Search, Refresh } from '@element-plus/icons-vue'

// ==================== 字典与下拉数据源 ====================

const sourceOptions = ref([])

/** 按字典类型拉数据，只取启用的 */
async function loadDict(type, target) {
  const result = await getDictDataListByType(type)
  target.value = (result.data || []).filter(d => d.status === '0')
}

/**
 * 字典值转标签 / 标签颜色。
 * 参数用 unref 兜底：模板里传 ref 会自动解包成数组，脚本里传的是 ref 本体，
 * unref 对两者都兼容 —— 直接读 options.value 在模板调用场景下是 undefined。
 */
function dictLabel(options, value, fallback) {
  const hit = unref(options).find(d => d.dictValue === value)
  return hit ? hit.dictLabel : (fallback || value || '—')
}

/** 标签颜色跟字典的 list_class 走 */
function dictTagType(options, value) {
  const hit = unref(options).find(d => d.dictValue === value)
  const cls = hit?.listClass
  if (cls === 'primary' || !cls) return ''
  if (['success', 'info', 'warning', 'danger'].includes(cls)) return cls
  return ''
}

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  workorderCode: '',
  taskCode: '',
  transOrderCode: '',
  itemCode: '',
  itemName: '',
  batchCode: '',
  consumeDateFrom: null,
  consumeDateTo: null
})

/** 时间区间单独用数组绑，提交前拆成 from / to 两个参数 */
const dateRange = ref([])

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

async function loadConsumeList() {
  queryForm.consumeDateFrom = dateRange.value?.[0] || null
  queryForm.consumeDateTo = dateRange.value?.[1] || null
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getConsumeByPage(params)
  tableData.value = result.data.list || []
  pagination.total = result.data.total
}

function handleSearch() {
  pagination.page = 1
  loadConsumeList()
}

function handleReset() {
  queryForm.workorderCode = ''
  queryForm.taskCode = ''
  queryForm.transOrderCode = ''
  queryForm.itemCode = ''
  queryForm.itemName = ''
  queryForm.batchCode = ''
  dateRange.value = []
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadConsumeList()
}

// ==================== 用料比对 ====================

const requireVisible = ref(false)
const requireTitle = ref('')
const requireMode = ref('task')
const requireRows = ref([])

/** 本道用料：应耗 = 本道任务数量 × 单位用量 */
async function handleShowTaskRequire(row) {
  requireMode.value = 'task'
  requireTitle.value = `本道用料 — ${row.processCode || ''} ${row.processName || ''}`
  const result = await getMaterialRequire(row.taskId)
  requireRows.value = result.data || []
  requireVisible.value = true
}

/** 工单比对：应耗 = 工单BOM 预计使用量 */
async function handleShowWorkorderCompare(row) {
  requireMode.value = 'workorder'
  requireTitle.value = `工单用料比对 — ${row.workorderCode || ''}`
  const result = await getWorkorderMaterialCompare(row.workorderId)
  requireRows.value = result.data || []
  requireVisible.value = true
}

/** 差额 = 已耗 - 应耗：正数超耗、负数未用足 */
function diffClass(row) {
  const d = Number(row.diffQty)
  if (!Number.isFinite(d) || d === 0) return ''
  return d > 0 ? 'qty-bad' : 'text-muted'
}

function diffLabel(row) {
  const d = Number(row.diffQty)
  if (!Number.isFinite(d) || d === 0) return '齐平'
  return d > 0 ? '超耗' : '未用足'
}

function diffTagType(row) {
  const d = Number(row.diffQty)
  if (!Number.isFinite(d) || d === 0) return 'success'
  return d > 0 ? 'danger' : 'info'
}

/** 超耗行整行标出来，一眼看到哪样料用超了 */
function requireRowClass({ row }) {
  return Number(row.diffQty) > 0 ? 'over-consume-row' : ''
}

// ==================== 删除 ====================

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定删除这条消耗记录吗？<br>物料：<b>${row.itemCode} ${row.itemName}</b><br>数量：<b>${formatQty(row.quantityConsumed)} ${row.unitOfMeasure || ''}</b>`,
    '删除警告',
    { confirmButtonText: '确定', cancelButtonText: '点错了', type: 'warning', dangerouslyUseHTMLString: true }
  ).then(async () => {
    await deleteConsume(row.recordId)
    ElMessage.success('删除成功（逻辑删除，只影响这一条记录）')
    loadConsumeList()
  }).catch(() => {})
}

// ==================== 小工具 ====================

/** 数量去掉无意义的尾部 0 */
function formatQty(v) {
  if (v === null || v === undefined || v === '') return '0'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return String(Number(n.toFixed(6)))
}

/** 只显示到分钟的短时间 */
function shortTime(s) {
  if (!s) return '—'
  return String(s).length > 16 ? String(s).substring(0, 16) : String(s)
}

onMounted(async () => {
  await loadDict('pro_consume_source', sourceOptions)
  loadConsumeList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.page-tip {
  margin-bottom: 12px;
  padding: 8px 12px;
  border-left: 3px solid var(--el-color-primary);
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-size: 13px;
  line-height: 1.6;
  border-radius: 4px;
}

.search-form {
  margin-bottom: 8px;
}

.text-muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.ml4 {
  margin-left: 4px;
}

.mb12 {
  margin-bottom: 12px;
}

.qty-consume {
  color: var(--el-color-primary);
}

.qty-bad {
  color: #F56C6C;
  font-weight: 500;
}

.empty-tip {
  padding: 16px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

:deep(.over-consume-row) {
  background: #FEF0F0;
}
</style>
