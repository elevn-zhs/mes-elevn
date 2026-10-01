<template>
  <div class="pro-card">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 ============ -->
      <template #header>
        <div class="card-header">
          <span>工序流转卡</span>
          <div class="header-actions">
            <el-button :icon="Refresh" size="small" @click="loadCardList">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- ================================================================
           说明条：流转卡不是"操作"出来的，是排产和报工"顺带"产生的
           ================================================================ -->
      <div class="page-tip">
        流转卡是跟着物料走的那张"随工单"：排产确认时自动建卡，每报一次工自动推进一道过站，
        末道产出达到流转数量则卡完工。本页只提供查询与追溯，不提供手动操作。
      </div>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="流转卡号">
          <el-input v-model="queryForm.cardCode" placeholder="请输入流转卡号" clearable style="width: 170px" />
        </el-form-item>
        <el-form-item label="工单编码">
          <el-input v-model="queryForm.workorderCode" placeholder="请输入工单编码" clearable style="width: 170px" />
        </el-form-item>
        <el-form-item label="产品">
          <el-input v-model="queryForm.itemName" placeholder="产品名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="d in statusOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- ============ 数据表格 ============ -->
      <el-table :data="tableData" border stripe>
        <el-table-column prop="cardCode" label="流转卡号" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="handleShowDetail(row)">{{ row.cardCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="workorderCode" label="工单编码" min-width="150" show-overflow-tooltip />
        <el-table-column label="产品" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.itemName }}
            <span class="text-muted">{{ row.specification || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="batchCode" label="批次号" min-width="130" show-overflow-tooltip />
        <el-table-column label="流转数量" width="100" align="right">
          <template #default="{ row }">
            <b>{{ formatQty(row.quantityTransferred) }}</b>
            <span class="text-muted"> {{ row.unitOfMeasure || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="dictTagType(statusOptions, row.status)" size="small">
              {{ dictLabel(statusOptions, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="建卡时间" width="160">
          <template #default="{ row }">{{ shortTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleShowDetail(row)">详情</el-button>
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
         流转卡详情弹窗：卡信息 + 过站明细
         过站行是这张卡的灵魂 —— 哪道工序进了、出了、产出多少、谁干的，
         一眼看到整批物料现在"走到哪了"。
         ================================================================ -->
    <el-dialog
      v-model="detailVisible"
      :title="detailCard ? `流转卡 ${detailCard.cardCode || ''}` : '流转卡详情'"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="detailCard" :column="2" border size="small">
        <el-descriptions-item label="流转卡号">{{ detailCard.cardCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="dictTagType(statusOptions, detailCard.status)" size="small">
            {{ dictLabel(statusOptions, detailCard.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生产工单">
          {{ detailCard.workorderCode }} {{ detailCard.workorderName }}
        </el-descriptions-item>
        <el-descriptions-item label="批次号">{{ detailCard.batchCode || '—' }}</el-descriptions-item>
        <el-descriptions-item label="产品">
          {{ detailCard.itemName }}
          <span class="text-muted">{{ detailCard.specification || '' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="流转数量">
          <b class="qty-total">
            {{ formatQty(detailCard.quantityTransferred) }}
            {{ detailCard.unitOfMeasure || '' }}
          </b>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="detailProcesses.length" class="process-title">过站明细（按工序顺序）</div>
      <el-table v-if="detailProcesses.length" :data="detailProcesses" border size="small"
                :row-class-name="processRowClass">
        <el-table-column prop="seqNum" label="#" width="50" align="center" />
        <el-table-column label="工序" min-width="130">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">{{ row.processCode }}</el-tag>
            <span class="ml4">{{ row.processName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="workstationName" label="工作站" min-width="120" show-overflow-tooltip />
        <el-table-column label="进站时间" width="150">
          <template #default="{ row }">{{ shortTime(row.inputTime) }}</template>
        </el-table-column>
        <el-table-column label="出站时间" width="150">
          <template #default="{ row }">{{ shortTime(row.outputTime) }}</template>
        </el-table-column>
        <el-table-column label="投入" width="90" align="right">
          <template #default="{ row }">{{ formatQty(row.quantityInput) }}</template>
        </el-table-column>
        <el-table-column label="产出" width="90" align="right">
          <template #default="{ row }">
            <span class="qty-ok">{{ formatQty(row.quantityOutput) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="不良" width="80" align="right">
          <template #default="{ row }">
            <span :class="Number(row.quantityUnqualified) > 0 ? 'qty-bad' : ''">
              {{ formatQty(row.quantityUnqualified) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作工" width="100">
          <template #default="{ row }">{{ row.nickName || row.userName || '—' }}</template>
        </el-table-column>
      </el-table>

      <div v-if="currentProcessName" class="current-tip">
        <el-alert type="info" :closable="false"
                  :title="`当前位置：${currentProcessName}（高亮行）—— 已进站未出站`" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref } from 'vue'

import { getCardByPage, getCardById } from '@/api/pro/card.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'
import { Search, Refresh, View } from '@element-plus/icons-vue'

// ==================== 字典与下拉数据源 ====================

const statusOptions = ref([])

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
  cardCode: '',
  workorderCode: '',
  itemName: '',
  status: null
})

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

async function loadCardList() {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getCardByPage(params)
  tableData.value = result.data.list || []
  pagination.total = result.data.total
}

function handleSearch() {
  pagination.page = 1
  loadCardList()
}

function handleReset() {
  queryForm.cardCode = ''
  queryForm.workorderCode = ''
  queryForm.itemName = ''
  queryForm.status = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadCardList()
}

// ==================== 详情 ====================

const detailVisible = ref(false)
const detailCard = ref(null)
const detailProcesses = ref([])

async function handleShowDetail(row) {
  const result = await getCardById(row.cardId)
  detailCard.value = result.data?.card || null
  detailProcesses.value = result.data?.processes || []
  detailVisible.value = true
}

/** 已进站未出站的工序 = 整批物料当前所在位置，整行高亮 */
function processRowClass({ row }) {
  if (row.inputTime && !row.outputTime) return 'current-process-row'
  return ''
}

const currentProcessName = computed(() => {
  const row = detailProcesses.value.find(p => p.inputTime && !p.outputTime)
  return row ? `${row.processCode} ${row.processName}` : ''
})

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
  await loadDict('pro_card_status', statusOptions)
  loadCardList()
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
  padding: 10px 14px;
  margin-bottom: 16px;
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  border-radius: 4px;
  color: #b88230;
  font-size: 13px;
  line-height: 1.7;
}

.search-form {
  margin-bottom: 12px;
}

.ml4 {
  margin-left: 4px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

.qty-ok {
  color: #67C23A;
}

.qty-bad {
  color: #F56C6C;
}

.qty-total {
  color: #409EFF;
}

.process-title {
  margin: 16px 0 8px;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.current-tip {
  margin-top: 12px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

<style>
/* 高亮"当前工序"行要作用到 el-table 内部，不能用 scoped */
.current-process-row td {
  background: #ecf5ff !important;
}
</style>
