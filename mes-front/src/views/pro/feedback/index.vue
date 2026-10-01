<template>
  <div class="pro-feedback">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 ============ -->
      <template #header>
        <div class="card-header">
          <span>生产报工记录</span>
          <div class="header-actions">
            <el-button :icon="Refresh" size="small" @click="loadFeedbackList">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- ================================================================
           说明条：把这张表的定位讲清楚
           报工是"报过去发生了什么"，属于统计与追溯视图，
           真正的录入动作在"生产任务"页的报工按钮上。
           ================================================================ -->
      <div class="page-tip">
        本页是报工记录的查询与追溯视图（报过几次、每次多少、合格多少）。
        实际录入报工请到「生产任务」页面，点任务行上的「报工」按钮，
        由后端统一回写任务与工单的已生产数量。<br />
        <b>报错了请用「冲销」而不是删除</b>：报工一旦提交就连带改过任务数量、工单产量、
        流转卡过站和物料消耗，冲销会把这四样一并回退，并把这条报工标记为「已冲销」留痕。
      </div>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="报工单号">
          <el-input v-model="queryForm.feedbackCode" placeholder="请输入报工单号" clearable style="width: 170px" />
        </el-form-item>
        <el-form-item label="工单编码">
          <el-input v-model="queryForm.workorderCode" placeholder="请输入工单编码" clearable style="width: 170px" />
        </el-form-item>
        <el-form-item label="任务编号">
          <el-input v-model="queryForm.taskCode" placeholder="请输入任务编号" clearable style="width: 170px" />
        </el-form-item>
        <el-form-item label="工序">
          <el-select v-model="queryForm.processId" placeholder="全部" clearable filterable style="width: 150px">
            <el-option v-for="p in processOptions" :key="p.processId"
                       :label="p.processCode + ' ' + p.processName" :value="p.processId" />
          </el-select>
        </el-form-item>
        <el-form-item label="报工类型">
          <el-select v-model="queryForm.feedbackType" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="d in feedbackTypeOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="报工途径">
          <el-select v-model="queryForm.feedbackChannel" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="d in feedbackChannelOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="报工时间从">
          <el-date-picker
            v-model="queryForm.feedbackTime"
            type="datetime"
            placeholder="起始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 190px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- ============ 汇总条：本次筛选结果的合计（让列表一眼看出量级） ============ -->
      <div v-if="tableData.length" class="summary-bar">
        <span>本页合计：</span>
        <span class="summary-item">报工 <b>{{ formatQty(pageSummary.feedback) }}</b></span>
        <span class="summary-item success">合格 <b>{{ formatQty(pageSummary.qualified) }}</b></span>
        <span class="summary-item danger">不良 <b>{{ formatQty(pageSummary.unqualified) }}</b></span>
        <span class="summary-item warning">待检 <b>{{ formatQty(pageSummary.uncheck) }}</b></span>
      </div>

      <!-- ============ 数据表格 ============ -->
      <!-- row-class-name：已冲销的行整行置灰，一眼能看出这条不计入进度 -->
      <el-table :data="tableData" border stripe :row-class-name="rowClassName">
        <el-table-column prop="feedbackCode" label="报工单号" min-width="170">
          <template #default="{ row }">
            <el-link type="primary" @click="handleShowDetail(row)">{{ row.feedbackCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="workorderCode" label="工单编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="taskCode" label="任务编号" min-width="170" show-overflow-tooltip />
        <el-table-column label="工序" width="140">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">{{ row.processCode }}</el-tag>
            <span class="ml4">{{ row.processName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="产品" min-width="150" show-overflow-tooltip />
        <el-table-column label="报工类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="dictTagType(feedbackTypeOptions, row.feedbackType)" size="small">
              {{ dictLabel(feedbackTypeOptions, row.feedbackType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报工数量" width="110" align="right">
          <template #default="{ row }">
            <b :class="{ 'qty-reversed': isReversed(row) }">{{ formatQty(row.quantityFeedback) }}</b>
            <span class="text-muted"> {{ row.unitName || row.unitOfMeasure || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="合格 / 不良" width="130" align="right">
          <template #default="{ row }">
            <span class="qty-ok">{{ formatQty(row.quantityQualified) }}</span>
            <span class="text-muted"> / </span>
            <span class="qty-bad">{{ formatQty(row.quantityUnqualified) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="待检" width="90" align="right">
          <template #default="{ row }">
            <span class="qty-warn">{{ formatQty(row.quantityUncheck) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lotNumber" label="生产批号" min-width="130" show-overflow-tooltip />
        <el-table-column label="报工途径" width="110" align="center">
          <template #default="{ row }">
            {{ dictLabel(feedbackChannelOptions, row.feedbackChannel) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="dictTagType(statusOptions, row.status)" size="small" effect="plain">
              {{ dictLabel(statusOptions, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报工时间" width="160">
          <template #default="{ row }">{{ shortTime(row.feedbackTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleShowDetail(row)">详情</el-button>
            <!-- 已冲销的不再给冲销入口，避免"点了必报错"的按钮 -->
            <el-button
              v-if="!isReversed(row)"
              link
              type="danger"
              :icon="RefreshLeft"
              @click="handleReverse(row)"
            >冲销</el-button>
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
         报工详情弹窗
         报工记录是真实业务事实，字段多且都是冗余快照（工单/产品/工序/工作站），
         用 el-descriptions 平铺比表格列更好读。
         ================================================================ -->
    <el-dialog
      v-model="detailVisible"
      title="报工记录详情"
      width="760px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="detailRow" :column="2" border size="small">
        <el-descriptions-item label="报工单号">{{ detailRow.feedbackCode }}</el-descriptions-item>
        <el-descriptions-item label="报工类型">
          {{ dictLabel(feedbackTypeOptions, detailRow.feedbackType) }}
        </el-descriptions-item>
        <el-descriptions-item label="工单编码">{{ detailRow.workorderCode }}</el-descriptions-item>
        <el-descriptions-item label="任务编号">{{ detailRow.taskCode }}</el-descriptions-item>
        <el-descriptions-item label="工序">
          {{ detailRow.processCode }} {{ detailRow.processName }}
        </el-descriptions-item>
        <el-descriptions-item label="工作站">
          {{ detailRow.workstationCode }} {{ detailRow.workstationName }}
        </el-descriptions-item>
        <el-descriptions-item label="产品">{{ detailRow.itemName }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ detailRow.specification || '—' }}</el-descriptions-item>
        <el-descriptions-item label="排产数量">
          {{ formatQty(detailRow.quantity) }}
          {{ detailRow.unitName || detailRow.unitOfMeasure || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="本次报工数量">
          <b class="qty-scheduled">
            {{ formatQty(detailRow.quantityFeedback) }}
            {{ detailRow.unitName || detailRow.unitOfMeasure || '' }}
          </b>
        </el-descriptions-item>
        <el-descriptions-item label="合格品数量">
          <span class="qty-ok">{{ formatQty(detailRow.quantityQualified) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="不良品数量">
          <span class="qty-bad">{{ formatQty(detailRow.quantityUnqualified) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="待检测数量">
          <span class="qty-warn">{{ formatQty(detailRow.quantityUncheck) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="报工状态">
          {{ dictLabel(statusOptions, detailRow.status) }}
        </el-descriptions-item>
        <el-descriptions-item label="生产批号">{{ detailRow.lotNumber || '—' }}</el-descriptions-item>
        <el-descriptions-item label="报工途径">
          {{ dictLabel(feedbackChannelOptions, detailRow.feedbackChannel) }}
        </el-descriptions-item>
        <el-descriptions-item label="报工人">
          {{ detailRow.nickName || detailRow.userName || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="报工时间">{{ detailRow.feedbackTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || '—' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 冲销留痕：只在已冲销时显示，让"谁在什么时候为什么撤的"随时可查 -->
      <template v-if="isReversed(detailRow)">
        <el-divider content-position="left">
          <span class="qty-bad">冲销留痕</span>
        </el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="冲销人">{{ detailRow.reverseBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="冲销时间">
            {{ detailRow.reverseTime || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="冲销原因" :span="2">
            {{ detailRow.reverseReason || '—' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- ================================================================
         冲销弹窗
         冲销会连带回退任务数量、工单产量、流转卡过站与物料消耗，
         所以原因必须填 —— 事后要能回答"这批为什么撤了"。
         ================================================================ -->
    <el-dialog
      v-model="reverseVisible"
      title="冲销报工"
      width="560px"
      :close-on-click-modal="false"
      @closed="handleReverseClosed"
    >
      <template v-if="reverseRow">
        <el-alert type="warning" :closable="false" show-icon class="mb16">
          <template #title>冲销会一并回退，不只是改个状态</template>
          <div class="alert-body">
            这条报工回写过的 <b>任务数量</b>、<b>工单产量与状态</b>、<b>流转卡过站进度</b>
            以及倒冲出的 <b>物料消耗</b>，都会在同一次操作里一起回退，
            回退后的账面等同于这条报工从没发生过。报工本身会保留并标记为「已冲销」。
          </div>
        </el-alert>

        <el-descriptions :column="2" border size="small" class="mb16">
          <el-descriptions-item label="报工单号">{{ reverseRow.feedbackCode }}</el-descriptions-item>
          <el-descriptions-item label="工序">
            {{ reverseRow.processCode }} {{ reverseRow.processName }}
          </el-descriptions-item>
          <el-descriptions-item label="本次报工数量">
            <b>{{ formatQty(reverseRow.quantityFeedback) }}</b>
          </el-descriptions-item>
          <el-descriptions-item label="合格 / 不良">
            <span class="qty-ok">{{ formatQty(reverseRow.quantityQualified) }}</span>
            <span class="text-muted"> / </span>
            <span class="qty-bad">{{ formatQty(reverseRow.quantityUnqualified) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-form label-width="90px">
          <el-form-item label="冲销原因" required>
            <el-input
              v-model="reverseReason"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="例如：报工数量填错，实际只做了 3 件；或操作工误报到了错误的工序"
            />
          </el-form-item>
        </el-form>
      </template>

      <template #footer>
        <el-button @click="reverseVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="reverseSubmitting"
          @click="handleReverseSubmit"
        >确认冲销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View, RefreshLeft } from '@element-plus/icons-vue'

import { getFeedbackByPage, getFeedbackById, reverseFeedback } from '@/api/pro/feedback.js'
import { getAllProcessList } from '@/api/pro/process.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'

// ==================== 字典与下拉数据源 ====================

const feedbackTypeOptions = ref([])
const feedbackChannelOptions = ref([])
const statusOptions = ref([])
const processOptions = ref([])

/** 按字典类型拉数据，只取启用的 */
async function loadDict(type, target) {
  const result = await getDictDataListByType(type)
  target.value = (result.data || []).filter(d => d.status === '0')
}

/**
 * 字典值转标签。
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
  feedbackCode: '',
  workorderCode: '',
  taskCode: '',
  processId: null,
  feedbackType: null,
  feedbackChannel: null,
  feedbackTime: null
})

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

/**
 * 本页汇总：把当前页的报工/合格/不良/待检加起来。
 * 只算当前页是刻意的 —— 全量汇总要单独走后端聚合接口，
 * 否则前端要为了一个数字把全表拉下来。
 */
const pageSummary = computed(() => {
  const sum = (key) => tableData.value.reduce((acc, r) => acc + (Number(r[key]) || 0), 0)
  return {
    feedback: sum('quantityFeedback'),
    qualified: sum('quantityQualified'),
    unqualified: sum('quantityUnqualified'),
    uncheck: sum('quantityUncheck')
  }
})

// ==================== 数据加载 ====================

async function loadFeedbackList() {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getFeedbackByPage(params)
  tableData.value = result.data.list || []
  pagination.total = result.data.total
}

function handleSearch() {
  pagination.page = 1
  loadFeedbackList()
}

function handleReset() {
  queryForm.feedbackCode = ''
  queryForm.workorderCode = ''
  queryForm.taskCode = ''
  queryForm.processId = null
  queryForm.feedbackType = null
  queryForm.feedbackChannel = null
  queryForm.feedbackTime = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadFeedbackList()
}

// ==================== 详情 ====================

const detailVisible = ref(false)
const detailRow = ref(null)

async function handleShowDetail(row) {
  // 重新查一次，保证详情字段完整（列表接口可能没带全）
  const result = await getFeedbackById(row.recordId)
  detailRow.value = result.data || row
  detailVisible.value = true
}

// ==================== 冲销 ====================

/** 报工状态：已冲销（数量已从任务与工单扣回，只留痕迹） */
const STATUS_REVERSED = 'REVERSED'

const reverseVisible = ref(false)
const reverseSubmitting = ref(false)
const reverseRow = ref(null)
const reverseReason = ref('')

/** 是否已冲销：决定行是否置灰、是否还给冲销入口 */
function isReversed(row) {
  return row && row.status === STATUS_REVERSED
}

/** 已冲销的行整行置灰 */
function rowClassName({ row }) {
  return isReversed(row) ? 'row-reversed' : ''
}

function handleReverse(row) {
  reverseRow.value = row
  reverseReason.value = ''
  reverseVisible.value = true
}

function handleReverseClosed() {
  reverseRow.value = null
  reverseReason.value = ''
}

async function handleReverseSubmit() {
  const reason = (reverseReason.value || '').trim()
  // 原因必填：后端也会拦，但先在前端挡住，避免白跑一次请求
  if (!reason) {
    ElMessage.warning('请填写冲销原因（冲销要留痕，事后得说得清为什么撤）')
    return
  }
  reverseSubmitting.value = true
  try {
    const result = await reverseFeedback(reverseRow.value.recordId, reason)
    const data = result.data || {}
    // 把回退结果说清楚，让人知道这次冲销连带动了什么
    let msg = `已冲销，回退数量 ${formatQty(data.reversedQuantity)}`
    if (data.revertConsumeRows) {
      msg += `，同时回退用料 ${data.revertConsumeRows} 条`
    }
    if (data.workorderReverted) {
      msg += '；工单因本次冲销不再完工，已退回「已下达」'
    }
    ElMessage.success(msg)
    reverseVisible.value = false
    loadFeedbackList()
  } finally {
    reverseSubmitting.value = false
  }
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
  await Promise.all([
    loadDict('feedback_type', feedbackTypeOptions),
    loadDict('feedback_channel', feedbackChannelOptions),
    loadDict('pro_feedback_status', statusOptions),
    getAllProcessList().then(r => { processOptions.value = r.data || [] })
  ])
  loadFeedbackList()
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

.summary-bar {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 8px 14px;
  margin-bottom: 12px;
  background: #fafafa;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}

.summary-item b {
  color: #303133;
}

.summary-item.success b {
  color: #67C23A;
}

.summary-item.danger b {
  color: #F56C6C;
}

.summary-item.warning b {
  color: #E6A23C;
}

.ml4 {
  margin-left: 4px;
}

.text-muted {
  color: #909399;
}

.qty-ok {
  color: #67C23A;
}

.qty-bad {
  color: #F56C6C;
}

.qty-warn {
  color: #E6A23C;
}

.qty-scheduled {
  color: #409EFF;
}

/* 已冲销的报工数量画删除线：提示这条不计入任务进度 */
.qty-reversed {
  text-decoration: line-through;
  color: #A8ABB2;
}

/* 已冲销的行整行置灰。
   el-table 的 tr 是子组件渲染出来的，scoped 样式要 :deep 才能穿透进去 */
:deep(.row-reversed) {
  color: #A8ABB2;
}

:deep(.row-reversed) td {
  background-color: #FBFBFB;
}

.mb16 {
  margin-bottom: 16px;
}

.alert-body {
  margin-top: 6px;
  line-height: 1.7;
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
