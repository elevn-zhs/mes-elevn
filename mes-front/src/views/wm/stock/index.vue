<template>
  <div class="wm-stock">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 ============ -->
      <template #header>
        <div class="card-header">
          <span>库存查询</span>
          <el-tag type="info" size="small" effect="plain">只读 · 库存只能由单据过账驱动</el-tag>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="物料">
          <el-select v-model="queryForm.itemId" placeholder="全部物料" clearable filterable
                     style="width: 200px">
            <el-option v-for="i in itemOptions" :key="i.itemId"
                       :label="i.itemName" :value="i.itemId" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="queryForm.warehouseId" placeholder="全部仓库" clearable
                     style="width: 170px">
            <el-option v-for="w in warehouseOptions" :key="w.warehouseId"
                       :label="w.warehouseName" :value="w.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="queryForm.batchCode" placeholder="请输入批次号" clearable />
        </el-form-item>
        <el-form-item v-if="activeTab === 'detail'">
          <el-checkbox v-model="onlyStock">只看有货的行</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 口径提示：汇总视图和明细视图的数字不是一回事，说清楚免得学生对不上 -->
      <el-alert
        v-if="activeTab !== 'detail'"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
        title="汇总视图的数量是【求和结果】，不是某一行的数量。做可用量判断请回明细视图看「可用数量」。"
      />

      <!-- ============ 四个视图 ============ -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">

        <!-- ---------- 视图一：库存明细 ---------- -->
        <el-tab-pane label="库存明细" name="detail">
          <el-table :data="detailData" border stripe v-loading="loading">
            <el-table-column prop="materialStockId" label="ID" width="70" />
            <el-table-column prop="itemCode" label="物料编码" width="130" />
            <el-table-column prop="itemName" label="物料名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="specification" label="规格" min-width="120" show-overflow-tooltip />
            <el-table-column prop="batchCode" label="批次号" width="130">
              <template #default="{ row }">{{ row.batchCode || '—' }}</template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="仓库" width="130" show-overflow-tooltip />
            <el-table-column label="库区/库位" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.areaName || '—' }}
                <span v-if="row.locationName" class="sub-text"> / {{ row.locationName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="容器" width="110">
              <template #default="{ row }">{{ row.packageCode || '—' }}</template>
            </el-table-column>
            <el-table-column label="在库数量" width="110" align="right">
              <template #default="{ row }">
                <span class="num-onhand">{{ formatQty(row.quantityOnhand) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="保留数量" width="100" align="right">
              <template #default="{ row }">{{ formatQty(row.quantityReserved) }}</template>
            </el-table-column>
            <el-table-column label="可用数量" width="110" align="right">
              <template #default="{ row }">
                <span class="num-available">{{ formatQty(row.quantityAvailable) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="unitName" label="单位" width="80" align="center">
              <template #default="{ row }">{{ row.unitName || row.unitOfMeasure || '—' }}</template>
            </el-table-column>
            <el-table-column label="冻结" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.frozenFlag === 'Y'" type="danger" size="small">冻结</el-tag>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column prop="recptDate" label="入库时间" width="160" />
            <el-table-column label="安全库存" width="130" align="center">
              <template #default="{ row }">
                <span v-if="row.minStock == null && row.maxStock == null" class="sub-text">未设置</span>
                <span v-else>{{ formatQty(row.minStock) }} ~ {{ formatQty(row.maxStock) }}</span>
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
              @change="loadDetail"
            />
          </div>
        </el-tab-pane>

        <!-- ---------- 视图二：按物料汇总 ---------- -->
        <el-tab-pane label="按物料汇总" name="byItem">
          <el-table :data="summaryByItemData" border stripe v-loading="loading">
            <el-table-column prop="itemCode" label="物料编码" width="140" />
            <el-table-column prop="itemName" label="物料名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="specification" label="规格" min-width="130" show-overflow-tooltip />
            <el-table-column label="在库合计" width="130" align="right">
              <template #default="{ row }">
                <span class="num-onhand">{{ formatQty(row.quantityOnhand) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="保留合计" width="120" align="right">
              <template #default="{ row }">{{ formatQty(row.quantityReserved) }}</template>
            </el-table-column>
            <el-table-column prop="unitName" label="单位" width="90" align="center">
              <template #default="{ row }">{{ row.unitName || row.unitOfMeasure || '—' }}</template>
            </el-table-column>
            <el-table-column label="最低库存" width="110" align="right">
              <template #default="{ row }">{{ formatQty(row.minStock) }}</template>
            </el-table-column>
            <el-table-column label="最高库存" width="110" align="right">
              <template #default="{ row }">{{ formatQty(row.maxStock) }}</template>
            </el-table-column>
            <el-table-column label="库存状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="warningTagType(row.warningType)" size="small" effect="plain">
                  {{ warningLabel(row.warningType) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="empty-tip" v-if="!summaryByItemData.length && !loading">暂无库存数据</div>
        </el-tab-pane>

        <!-- ---------- 视图三：按仓库汇总 ---------- -->
        <el-tab-pane label="按仓库汇总" name="byWarehouse">
          <el-table :data="summaryByWarehouseData" border stripe v-loading="loading">
            <el-table-column prop="warehouseCode" label="仓库编码" width="140" />
            <el-table-column prop="warehouseName" label="仓库名称" min-width="180" show-overflow-tooltip />
            <el-table-column label="物料种数" width="120" align="right">
              <template #default="{ row }">
                <span class="num-onhand">{{ row.itemKindCount ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="在库总量" width="140" align="right">
              <template #default="{ row }">
                <span class="num-onhand">{{ formatQty(row.quantityOnhand) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="保留总量" width="130" align="right">
              <template #default="{ row }">{{ formatQty(row.quantityReserved) }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
          </el-table>
          <div class="empty-tip" v-if="!summaryByWarehouseData.length && !loading">暂无库存数据</div>
        </el-tab-pane>

        <!-- ---------- 视图四：库存预警 ---------- -->
        <el-tab-pane name="warning">
          <template #label>
            <span>库存预警
              <el-badge v-if="warningData.length" :value="warningData.length" type="danger" />
            </span>
          </template>

          <el-alert
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 12px"
            title="安全库存取自物料档案（md_item 的最低/最高库存）。这里比较的是【按物料汇总后】的数量：一个物料散在 3 个库位各 10，逐行看着都不低，加起来只有 30。"
          />

          <el-table :data="warningData" border stripe v-loading="loading"
                    :row-class-name="warningRowClass">
            <el-table-column prop="itemCode" label="物料编码" width="140" />
            <el-table-column prop="itemName" label="物料名称" min-width="180" show-overflow-tooltip />
            <el-table-column label="在库合计" width="120" align="right">
              <template #default="{ row }">
                <span class="num-onhand">{{ formatQty(row.quantityOnhand) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="最低库存" width="110" align="right">
              <template #default="{ row }">{{ formatQty(row.minStock) }}</template>
            </el-table-column>
            <el-table-column label="最高库存" width="110" align="right">
              <template #default="{ row }">{{ formatQty(row.maxStock) }}</template>
            </el-table-column>
            <el-table-column label="预警类型" width="150" align="center">
              <template #default="{ row }">
                <el-tag :type="warningTagType(row.warningType)" size="small">
                  {{ warningLabel(row.warningType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="建议" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.warningType === 'LOW'">
                  低于安全库存，建议补货（缺口 {{ formatQty(gapToMin(row)) }}）
                </span>
                <span v-else-if="row.warningType === 'HIGH'">
                  超过最高库存，压了资金，建议暂停采购
                </span>
                <span v-else>—</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="empty-tip" v-if="!warningData.length && !loading">库存都很健康，没有预警</div>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

import {
  getStockByPage,
  getStockSummaryByItem,
  getStockSummaryByWarehouse,
  getStockWarningList
} from '@/api/wm/stock.js'
import { getAllWarehouseList } from '@/api/wm/warehouse.js'
import { getItemByPage } from '@/api/md/item.js'

// ==================== 视图切换 ====================
// 四个视图共用同一套查询条件，但只有明细视图分页。
// 汇总与预警接口不分页（一次全带回来），数据量大的话后面再加导出。
const activeTab = ref('detail')

// ==================== 查询条件 ====================
const queryForm = reactive({
  itemId: null,
  warehouseId: null,
  batchCode: ''
})

const onlyStock = ref(false)
const loading = ref(false)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ==================== 数据源 ====================
const detailData = ref([])
const summaryByItemData = ref([])
const summaryByWarehouseData = ref([])
const warningData = ref([])

const warehouseOptions = ref([])
const itemOptions = ref([])

// ==================== 工具函数 ====================

/** 数量格式化：后端给的是 BigDecimal（可能是字符串），空值统一显示为 — */
function formatQty(v) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  if (Number.isNaN(n)) return v
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 6 })
}

/** LOW 低于最低库存 / HIGH 高于最高库存 / 空串 正常 */
function warningLabel(type) {
  if (type === 'LOW') return '低于最低库存'
  if (type === 'HIGH') return '高于最高库存'
  return '正常'
}

function warningTagType(type) {
  if (type === 'LOW') return 'danger'
  if (type === 'HIGH') return 'warning'
  return 'success'
}

/** 预警行整行标色，扫一眼就能看见 */
function warningRowClass({ row }) {
  if (row.warningType === 'LOW') return 'row-low'
  if (row.warningType === 'HIGH') return 'row-high'
  return ''
}

/** 缺口 = 最低库存 - 在库合计 */
function gapToMin(row) {
  const min = Number(row.minStock || 0)
  const onhand = Number(row.quantityOnhand || 0)
  const gap = min - onhand
  return gap > 0 ? gap : 0
}

// 组装查询参数：空串/null 一律不传，避免后端拿空串去 like
function buildParams() {
  const params = {}
  if (queryForm.itemId) params.itemId = queryForm.itemId
  if (queryForm.warehouseId) params.warehouseId = queryForm.warehouseId
  if (queryForm.batchCode) params.batchCode = queryForm.batchCode
  return params
}

// ==================== 加载各视图 ====================

async function loadDetail() {
  loading.value = true
  try {
    const params = {
      ...buildParams(),
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    if (onlyStock.value) params.onlyStock = 'Y'
    const result = await getStockByPage(params)
    detailData.value = result.data?.list || []
    pagination.total = result.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadSummaryByItem() {
  loading.value = true
  try {
    const result = await getStockSummaryByItem(buildParams())
    summaryByItemData.value = result.data || []
  } finally {
    loading.value = false
  }
}

async function loadSummaryByWarehouse() {
  loading.value = true
  try {
    const result = await getStockSummaryByWarehouse(buildParams())
    summaryByWarehouseData.value = result.data || []
  } finally {
    loading.value = false
  }
}

async function loadWarning() {
  loading.value = true
  try {
    const result = await getStockWarningList()
    warningData.value = result.data || []
  } finally {
    loading.value = false
  }
}

/** 按当前 tab 拉对应数据 */
function loadCurrent() {
  if (activeTab.value === 'detail') return loadDetail()
  if (activeTab.value === 'byItem') return loadSummaryByItem()
  if (activeTab.value === 'byWarehouse') return loadSummaryByWarehouse()
  if (activeTab.value === 'warning') return loadWarning()
}

// ==================== 交互 ====================

function handleSearch() {
  pagination.page = 1
  loadCurrent()
}

function handleReset() {
  queryForm.itemId = null
  queryForm.warehouseId = null
  queryForm.batchCode = ''
  onlyStock.value = false
  pagination.page = 1
  loadCurrent()
}

function handleTabChange() {
  pagination.page = 1
  loadCurrent()
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

onMounted(() => {
  loadDetail()
  loadWarehouseOptions()
  loadItemOptions()
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

.num-onhand {
  font-weight: 600;
  color: #303133;
}

.num-available {
  font-weight: 600;
  color: #67c23a;
}

:deep(.row-low) {
  background-color: #fef0f0 !important;
}

:deep(.row-high) {
  background-color: #fdf6ec !important;
}
</style>
