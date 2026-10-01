<template>
  <div class="wm-transaction">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 ============ -->
      <template #header>
        <div class="card-header">
          <span>库存事务（流水账）</span>
          <el-tag type="info" size="small" effect="plain">只读 · 流水只能由单据过账生成</el-tag>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="物料">
          <el-select v-model="queryForm.itemId" placeholder="全部物料" clearable filterable
                     style="width: 190px">
            <el-option v-for="i in itemOptions" :key="i.itemId"
                       :label="i.itemName" :value="i.itemId" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="queryForm.warehouseId" placeholder="全部仓库" clearable
                     style="width: 160px">
            <el-option v-for="w in warehouseOptions" :key="w.warehouseId"
                       :label="w.warehouseName" :value="w.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="单据编号">
          <el-input v-model="queryForm.sourceDocCode" placeholder="来源单据编号" clearable />
        </el-form-item>
        <el-form-item label="事务类型">
          <el-select v-model="queryForm.transactionType" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="d in docTypeOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="事务日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="[new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 方向口径提示：数量永远是正数，方向看 flag -->
      <el-alert
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
        title="数量字段永远是正数，进出方向记在「方向」列（1 入库 / -1 出库）。调拨会生成一进一出配对的两条，用「配对事务」互相指向。"
      />

      <!-- ============ 三个视图 ============ -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">

        <!-- ---------- 视图一：流水列表（按主键倒序，看最近发生了什么） ---------- -->
        <el-tab-pane label="流水列表" name="list">
          <el-table :data="listData" border stripe v-loading="loading">
            <el-table-column prop="transactionId" label="ID" width="70" />
            <el-table-column label="方向" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.transactionFlag === 1 ? 'success' : 'danger'" size="small">
                  {{ row.transactionFlag === 1 ? '入库' : '出库' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="事务类型" width="130">
              <template #default="{ row }">
                {{ dictLabel(docTypeOptions, row.transactionType) }}
              </template>
            </el-table-column>
            <el-table-column prop="itemCode" label="物料编码" width="130" />
            <el-table-column prop="itemName" label="物料名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="batchCode" label="批次号" width="130">
              <template #default="{ row }">{{ row.batchCode || '—' }}</template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="仓库" width="130" show-overflow-tooltip />
            <el-table-column label="库位" width="130" show-overflow-tooltip>
              <template #default="{ row }">{{ row.locationName || '—' }}</template>
            </el-table-column>
            <el-table-column label="数量" width="110" align="right">
              <template #default="{ row }">
                <span :class="row.transactionFlag === 1 ? 'num-in' : 'num-out'">
                  {{ row.transactionFlag === 1 ? '+' : '-' }}{{ formatQty(row.transactionQuantity) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="unitName" label="单位" width="80" align="center">
              <template #default="{ row }">{{ row.unitName || row.unitOfMeasure || '—' }}</template>
            </el-table-column>
            <el-table-column label="来源单据" width="170" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.sourceDocCode">{{ row.sourceDocCode }}</span>
                <span v-else class="sub-text">—</span>
              </template>
            </el-table-column>
            <el-table-column label="配对事务" width="100" align="center">
              <template #default="{ row }">
                <el-link v-if="row.relatedTransactionId" type="primary"
                         @click="jumpToTransaction(row.relatedTransactionId)">
                  #{{ row.relatedTransactionId }}
                </el-link>
                <span v-else class="sub-text">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="transactionDate" label="事务日期" width="160" />
            <el-table-column label="详情" width="80" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="handleDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @change="loadList"
            />
          </div>
        </el-tab-pane>

        <!-- ---------- 视图二：批次追溯（按时间正序，看这批货一路怎么走过来的） ---------- -->
        <el-tab-pane label="批次追溯" name="trace">
          <el-form :inline="true" class="search-form">
            <el-form-item label="物料" required>
              <el-select v-model="traceForm.itemId" placeholder="请选择物料" filterable
                         style="width: 220px">
                <el-option v-for="i in itemOptions" :key="i.itemId"
                           :label="i.itemName" :value="i.itemId" />
              </el-select>
            </el-form-item>
            <el-form-item label="批次">
              <el-select v-model="traceForm.batchId" placeholder="全部批次" clearable filterable
                         style="width: 200px" :disabled="!traceForm.itemId">
                <el-option v-for="b in traceBatchOptions" :key="b.batchId"
                           :label="b.batchCode" :value="b.batchId" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" :disabled="!traceForm.itemId"
                         @click="loadTrace">追溯</el-button>
              <el-button :icon="Refresh" @click="handleTraceReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-alert
            v-if="!traceForm.itemId"
            type="info"
            :closable="false"
            show-icon
            title="先选一个物料。追溯视图按时间【正序】排列 —— 什么时候入库、被谁领走、有没有退回来，一路看下来。"
          />
          <template v-else>
            <!-- 结存小计：方便一眼对账 -->
            <div class="trace-summary" v-if="traceData.length">
              <span>共 <b>{{ traceData.length }}</b> 条流水</span>
              <span class="sep">|</span>
              <span>入库合计 <b class="num-in">+{{ formatQty(traceInTotal) }}</b></span>
              <span class="sep">|</span>
              <span>出库合计 <b class="num-out">-{{ formatQty(traceOutTotal) }}</b></span>
              <span class="sep">|</span>
              <span>净结存 <b :class="traceNet >= 0 ? 'num-in' : 'num-out'">{{ formatQty(traceNet) }}</b></span>
            </div>

            <el-table :data="traceData" border stripe v-loading="loading">
              <el-table-column prop="transactionDate" label="时间" width="170" />
              <el-table-column label="方向" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.transactionFlag === 1 ? 'success' : 'danger'" size="small">
                    {{ row.transactionFlag === 1 ? '入库' : '出库' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="事务类型" width="130">
                <template #default="{ row }">
                  {{ dictLabel(docTypeOptions, row.transactionType) }}
                </template>
              </el-table-column>
              <el-table-column prop="itemName" label="物料" min-width="150" show-overflow-tooltip />
              <el-table-column prop="batchCode" label="批次号" width="130">
                <template #default="{ row }">{{ row.batchCode || '—' }}</template>
              </el-table-column>
              <el-table-column prop="warehouseName" label="仓库" width="130" show-overflow-tooltip />
              <el-table-column label="数量" width="110" align="right">
                <template #default="{ row }">
                  <span :class="row.transactionFlag === 1 ? 'num-in' : 'num-out'">
                    {{ row.transactionFlag === 1 ? '+' : '-' }}{{ formatQty(row.transactionQuantity) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="来源单据" width="170" show-overflow-tooltip>
                <template #default="{ row }">{{ row.sourceDocCode || '—' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="90">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleDetail(row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="empty-tip" v-if="!traceData.length && !loading">该物料还没有流水记录</div>
          </template>
        </el-tab-pane>

        <!-- ---------- 视图三：类型统计 ---------- -->
        <el-tab-pane label="类型统计" name="stat">
          <el-table :data="statData" border stripe v-loading="loading" show-summary
                    :summary-method="statSummary">
            <el-table-column label="事务类型" min-width="170">
              <template #default="{ row }">
                {{ dictLabel(docTypeOptions, row.transactionType) }}
              </template>
            </el-table-column>
            <el-table-column label="流水条数" width="120" align="right">
              <template #default="{ row }">{{ row.recordCount ?? 0 }}</template>
            </el-table-column>
            <el-table-column label="入库合计" width="150" align="right">
              <template #default="{ row }">
                <span class="num-in">+{{ formatQty(row.inQuantity) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="出库合计" width="150" align="right">
              <template #default="{ row }">
                <span class="num-out">-{{ formatQty(row.outQuantity) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="净变动" width="150" align="right">
              <template #default="{ row }">
                <span :class="Number(row.netQuantity || 0) >= 0 ? 'num-in' : 'num-out'">
                  {{ Number(row.netQuantity || 0) >= 0 ? '+' : '' }}{{ formatQty(row.netQuantity) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
          <div class="empty-tip" v-if="!statData.length && !loading">还没有任何流水</div>
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <!-- ============ 流水详情弹窗 ============ -->
    <el-dialog v-model="detailVisible" title="流水详情" width="760px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="事务ID">{{ currentRow.transactionId }}</el-descriptions-item>
        <el-descriptions-item label="事务日期">{{ currentRow.transactionDate }}</el-descriptions-item>
        <el-descriptions-item label="方向">
          <el-tag :type="currentRow.transactionFlag === 1 ? 'success' : 'danger'" size="small">
            {{ currentRow.transactionFlag === 1 ? '入库 (+)' : '出库 (-)' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="事务类型">
          {{ dictLabel(docTypeOptions, currentRow.transactionType) }}
        </el-descriptions-item>
        <el-descriptions-item label="物料编码">{{ currentRow.itemCode || '—' }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ currentRow.itemName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentRow.specification || '—' }}</el-descriptions-item>
        <el-descriptions-item label="单位">
          {{ currentRow.unitName || currentRow.unitOfMeasure || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="批次号">{{ currentRow.batchCode || '—' }}</el-descriptions-item>
        <el-descriptions-item label="数量">
          <span :class="currentRow.transactionFlag === 1 ? 'num-in' : 'num-out'">
            {{ currentRow.transactionFlag === 1 ? '+' : '-' }}{{ formatQty(currentRow.transactionQuantity) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentRow.warehouseName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="库区/库位">
          {{ currentRow.areaName || '—' }}
          <span v-if="currentRow.locationName"> / {{ currentRow.locationName }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="来源单据类型">
          {{ currentRow.sourceDocType ? dictLabel(docTypeOptions, currentRow.sourceDocType) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="来源单据编号">{{ currentRow.sourceDocCode || '—' }}</el-descriptions-item>
        <el-descriptions-item label="来源单据行ID">{{ currentRow.sourceDocLineId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="库存记录ID">{{ currentRow.materialStockId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="配对事务ID" :span="2">
          {{ currentRow.relatedTransactionId || '—（非调拨流水没有配对行）' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ currentRow.createBy || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref, watch } from 'vue'
import { Search, Refresh, View } from '@element-plus/icons-vue'

import {
  getTransactionByPage,
  getTransactionByItemAndBatch,
  getTransactionStatByType,
  getTransactionById
} from '@/api/wm/transaction.js'
import { getAllWarehouseList } from '@/api/wm/warehouse.js'
import { getItemByPage } from '@/api/md/item.js'
import { getBatchByPage } from '@/api/wm/batch.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'

// ==================== 视图切换 ====================
const activeTab = ref('list')

// ==================== 查询条件 ====================
const queryForm = reactive({
  itemId: null,
  warehouseId: null,
  sourceDocCode: '',
  transactionType: null
})

const dateRange = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ==================== 数据源 ====================
const listData = ref([])
const traceData = ref([])
const statData = ref([])
const loading = ref(false)

const warehouseOptions = ref([])
const itemOptions = ref([])
const traceBatchOptions = ref([])
const docTypeOptions = ref([])

const traceForm = reactive({
  itemId: null,
  batchId: null
})

// ==================== 字典工具 ====================

async function loadDict(type, target) {
  const result = await getDictDataListByType(type)
  target.value = result.data || []
}

function dictLabel(options, value, fallback) {
  const hit = unref(options).find(d => d.dictValue === value)
  return hit ? hit.dictLabel : (fallback || value || '—')
}

function formatQty(v) {
  if (v === null || v === undefined || v === '') return '0'
  const n = Number(v)
  if (Number.isNaN(n)) return v
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 6 })
}

// ==================== 参数组装 ====================
function buildParams() {
  const params = {}
  if (queryForm.itemId) params.itemId = queryForm.itemId
  if (queryForm.warehouseId) params.warehouseId = queryForm.warehouseId
  if (queryForm.sourceDocCode) params.sourceDocCode = queryForm.sourceDocCode
  if (queryForm.transactionType) params.transactionType = queryForm.transactionType
  if (dateRange.value && dateRange.value.length === 2) {
    params.transactionDateStart = dateRange.value[0]
    params.transactionDateEnd = dateRange.value[1]
  }
  return params
}

// ==================== 加载 ====================

async function loadList() {
  loading.value = true
  try {
    const params = {
      ...buildParams(),
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    const result = await getTransactionByPage(params)
    listData.value = result.data?.list || []
    pagination.total = result.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadTrace() {
  if (!traceForm.itemId) return
  loading.value = true
  try {
    const params = { itemId: traceForm.itemId }
    if (traceForm.batchId) params.batchId = traceForm.batchId
    const result = await getTransactionByItemAndBatch(params)
    traceData.value = result.data || []
  } finally {
    loading.value = false
  }
}

async function loadStat() {
  loading.value = true
  try {
    const result = await getTransactionStatByType(buildParams())
    statData.value = result.data || []
  } finally {
    loading.value = false
  }
}

function loadCurrent() {
  if (activeTab.value === 'list') return loadList()
  if (activeTab.value === 'trace') return loadTrace()
  if (activeTab.value === 'stat') return loadStat()
}

// ==================== 追溯小计 ====================
// 前端自己算一遍：入库相加、出库相加、净结存。
// 和后端库存对不上就说明有流水没入账，是个很直观的交叉校验。
const traceInTotal = computed(() =>
  traceData.value
    .filter(r => r.transactionFlag === 1)
    .reduce((sum, r) => sum + Number(r.transactionQuantity || 0), 0)
)

const traceOutTotal = computed(() =>
  traceData.value
    .filter(r => r.transactionFlag === -1)
    .reduce((sum, r) => sum + Number(r.transactionQuantity || 0), 0)
)

const traceNet = computed(() => traceInTotal.value - traceOutTotal.value)

// ==================== 统计表合计行 ====================
function statSummary({ columns, data }) {
  const sums = []
  columns.forEach((col, idx) => {
    if (idx === 0) {
      sums[idx] = '合计'
      return
    }
    if (col.label === '流水条数') {
      sums[idx] = data.reduce((s, r) => s + Number(r.recordCount || 0), 0)
    } else if (col.label === '入库合计') {
      sums[idx] = '+' + formatQty(data.reduce((s, r) => s + Number(r.inQuantity || 0), 0))
    } else if (col.label === '出库合计') {
      sums[idx] = '-' + formatQty(data.reduce((s, r) => s + Number(r.outQuantity || 0), 0))
    } else if (col.label === '净变动') {
      const net = data.reduce((s, r) => s + Number(r.netQuantity || 0), 0)
      sums[idx] = (net >= 0 ? '+' : '') + formatQty(net)
    } else {
      sums[idx] = ''
    }
  })
  return sums
}

// ==================== 交互 ====================

function handleSearch() {
  pagination.page = 1
  if (activeTab.value === 'list') loadList()
  else if (activeTab.value === 'stat') loadStat()
  else loadCurrent()
}

function handleReset() {
  queryForm.itemId = null
  queryForm.warehouseId = null
  queryForm.sourceDocCode = ''
  queryForm.transactionType = null
  dateRange.value = null
  pagination.page = 1
  loadCurrent()
}

function handleTabChange() {
  pagination.page = 1
  loadCurrent()
}

function handleTraceReset() {
  traceForm.itemId = null
  traceForm.batchId = null
  traceBatchOptions.value = []
  traceData.value = []
}

/** 点配对事务：不能直接跳行（列表是分页的），就把它的主键塞进筛选条件查出来 */
function jumpToTransaction(transactionId) {
  activeTab.value = 'list'
  queryForm.itemId = null
  queryForm.warehouseId = null
  queryForm.sourceDocCode = ''
  queryForm.transactionType = null
  dateRange.value = null
  pagination.page = 1
  loadList().then(() => {
    const hit = listData.value.find(r => r.transactionId === transactionId)
    // 配对行和当前行几乎同时生成，同页找不到就提示去下一页看
    if (hit) {
      handleDetail(hit)
    } else {
      handleDetailById(transactionId)
    }
  })
}

async function handleDetailById(id) {
  const result = await getTransactionById(id)
  if (result.data) {
    currentRow.value = result.data
    detailVisible.value = true
  }
}

// ==================== 详情弹窗 ====================
const detailVisible = ref(false)
const currentRow = ref(null)

function handleDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}

// ==================== 下拉数据 ====================

async function loadWarehouseOptions() {
  const result = await getAllWarehouseList()
  warehouseOptions.value = result.data || []
}

async function loadItemOptions() {
  const result = await getItemByPage({ pageNum: 1, pageSize: 500 })
  itemOptions.value = result.data?.list || []
}

// 追溯视图的批次下拉要跟着物料走：物料一换，旧批次就作废了
watch(() => traceForm.itemId, async (itemId) => {
  traceForm.batchId = null
  traceBatchOptions.value = []
  if (!itemId) return
  const result = await getBatchByPage({ itemId, pageNum: 1, pageSize: 200 })
  traceBatchOptions.value = result.data?.list || []
})

onMounted(() => {
  loadList()
  loadWarehouseOptions()
  loadItemOptions()
  loadDict('wm_doc_type', docTypeOptions)
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.empty-tip {
  padding: 32px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.sub-text {
  color: #909399;
  font-size: 12px;
}

.num-in {
  font-weight: 600;
  color: #67c23a;
}

.num-out {
  font-weight: 600;
  color: #f56c6c;
}

.trace-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  margin-bottom: 12px;
  background: #f4f4f5;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.trace-summary .sep {
  color: #dcdfe6;
}
</style>
