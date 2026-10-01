<template>
  <div class="system-workorder">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>生产工单</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增工单</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="工单编码">
          <el-input v-model="queryForm.workorderCode" placeholder="请输入工单编码" clearable />
        </el-form-item>
        <el-form-item label="工单名称">
          <el-input v-model="queryForm.workorderName" placeholder="请输入工单名称" clearable />
        </el-form-item>
        <el-form-item label="工单类型">
          <el-select v-model="queryForm.workorderType" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in workorderTypeOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="queryForm.orderSource" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in orderSourceOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in statusOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- ============ 批量操作工具栏（选中行时才显示） ============ -->
      <div v-show="selectedRows.length" class="batch-toolbar">
        <span class="batch-tip">已选中 <b>{{ selectedRows.length }}</b> 项</span>
        <el-button type="danger" :icon="Delete" size="small" @click="handleBatchDelete">批量删除</el-button>
        <el-button :icon="Close" size="small" plain @click="clearSelection">取消选择</el-button>
      </div>

      <!-- ============ 数据表格 ============ -->
      <el-table
        ref="tableRef"
        :data="tableData"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="workorderCode" label="工单编码" min-width="150">
          <template #default="scope">
            <el-link @click="handleDetail(scope.row)" type="primary">{{ scope.row.workorderCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="workorderName" label="工单名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="工单类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.workorderType)" effect="plain">
              {{ typeLabel(row.workorderType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.orderSource === 'ORDER' ? 'primary' : 'success'" effect="plain">
              {{ sourceLabel(row.orderSource) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="客户/供应商" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.clientName">{{ row.clientName }}</span>
            <span v-else-if="row.vendorName">{{ row.vendorName }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="产品" min-width="170" show-overflow-tooltip />
        <el-table-column label="数量" width="110" align="right">
          <template #default="{ row }">
            {{ formatQty(row.quantity) }} {{ row.unitOfMeasure }}
          </template>
        </el-table-column>
        <el-table-column label="已排产/已生产" width="130" align="right">
          <template #default="{ row }">
            <span class="text-muted">{{ formatQty(row.quantityScheduled) }}</span>
            <span class="text-muted"> / </span>
            <span>{{ formatQty(row.quantityProduced) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestDate" label="需求日期" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <!-- 状态流转按钮按当前状态显示，避免出现点了必报错的按钮 -->
            <el-button
              v-if="row.status === 'PREPARE'"
              link
              type="success"
              :icon="Check"
              @click="handleConfirm(row)"
            >下达</el-button>
            <el-button
              v-if="row.status === 'CONFIRMED' && row.workorderType === 'SELF'"
              link
              type="primary"
              :icon="Calendar"
              :disabled="isFullyScheduled(row)"
              :title="isFullyScheduled(row) ? '已排满，没有可排产的数量' : ''"
              @click="handleSchedule(row)"
            >排产</el-button>
            <el-button
              v-if="row.status === 'CONFIRMED' && row.workorderType === 'SELF' && Number(row.quantityScheduled) > 0"
              link
              type="warning"
              :icon="RefreshLeft"
              @click="handleCancelSchedule(row)"
            >撤销排产</el-button>
            <el-button
              v-if="row.status === 'CONFIRMED'"
              link
              type="success"
              :icon="CircleCheck"
              @click="handleFinish(row)"
            >完工</el-button>
            <el-button
              v-if="row.status === 'PREPARE' || row.status === 'CONFIRMED'"
              link
              type="warning"
              :icon="CircleClose"
              @click="handleCancel(row)"
            >取消</el-button>
            <el-button
              v-if="row.status === 'PREPARE'"
              link
              type="primary"
              :icon="Edit"
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'PREPARE'"
              link
              type="danger"
              :icon="Delete"
              @click="handleDelete(row)"
            >删除</el-button>
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

    <!-- ============ 新增/编辑工单弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="workorderFormRef"
        :model="workorderForm"
        :rules="workorderRules"
        label-width="110px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工单编码" prop="workorderCode">
              <el-input v-model="workorderForm.workorderCode" placeholder="如 WO202609230001">
                <template #append>
                  <el-button :loading="codeLoading" @click="handleGenCode">生成</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单名称" prop="workorderName">
              <el-input v-model="workorderForm.workorderName" placeholder="如 A100柜体生产工单" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工单类型" prop="workorderType">
              <el-select v-model="workorderForm.workorderType" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in workorderTypeOptions"
                  :key="item.dictValue"
                  :label="item.dictLabel"
                  :value="item.dictValue"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源类型" prop="orderSource">
              <el-select
                v-model="workorderForm.orderSource"
                placeholder="请选择"
                style="width: 100%"
                @change="handleSourceTypeChange"
              >
                <el-option
                  v-for="item in orderSourceOptions"
                  :key="item.dictValue"
                  :label="item.dictLabel"
                  :value="item.dictValue"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 客户订单来源才显示订单编号与客户，避免填了却被后端拒绝 -->
        <el-row v-if="isClientOrder" :gutter="16">
          <el-col :span="12">
            <el-form-item label="订单编号" prop="sourceCode">
              <el-input v-model="workorderForm.sourceCode" placeholder="客户订单号，如 SO-2026-0912" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户" prop="clientId">
              <el-select
                v-model="workorderForm.clientId"
                placeholder="请选择客户"
                filterable
                style="width: 100%"
                @change="handleClientChange"
              >
                <el-option
                  v-for="c in clientOptions"
                  :key="c.clientId"
                  :label="c.clientName"
                  :value="c.clientId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 外协/外购才显示供应商 -->
        <el-row v-if="isOutsourced" :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商" prop="vendorId">
              <el-select
                v-model="workorderForm.vendorId"
                placeholder="请选择供应商"
                filterable
                style="width: 100%"
                @change="handleVendorChange"
              >
                <el-option
                  v-for="v in vendorOptions"
                  :key="v.vendorId"
                  :label="v.vendorName"
                  :value="v.vendorId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchCode">
              <el-input v-model="workorderForm.batchCode" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="!isOutsourced" :gutter="16">
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchCode">
              <el-input v-model="workorderForm.batchCode" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">产品信息</el-divider>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="生产产品" prop="productId">
              <el-select
                v-model="workorderForm.productId"
                placeholder="请选择产品"
                filterable
                style="width: 100%"
                @change="handleProductChange"
              >
                <el-option
                  v-for="i in productOptions"
                  :key="i.itemId"
                  :label="`${i.itemCode} ${i.itemName}`"
                  :value="i.itemId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产数量" prop="quantity">
              <el-input-number
                v-model="workorderForm.quantity"
                :min="0.000001"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="需求日期" prop="requestDate">
              <el-date-picker
                v-model="workorderForm.requestDate"
                type="datetime"
                placeholder="请选择需求日期"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号">
              <el-input :model-value="selectedProductSpc" disabled placeholder="选择产品后自动带出" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="workorderForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 排产弹窗 ============
         核心：让用户为每道工序选工作站。
         工序与候选工作站由后端 preview 接口给出 —— 同一道工序可能有多个工作站，
         到底用哪个是排产员的决策（看设备负荷、换型成本），系统不该替他决定。 -->
    <el-dialog
      v-model="scheduleVisible"
      title="生产工单排产"
      width="900px"
      :close-on-click-modal="false"
      @close="handleScheduleClose"
    >
      <template v-if="schedulePreview">
        <!-- 工单与路线摘要 -->
        <el-descriptions :column="3" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="工单">{{ schedulePreview.workorder.workorderCode }}</el-descriptions-item>
          <el-descriptions-item label="产品">
            {{ schedulePreview.workorder.productName }}
          </el-descriptions-item>
          <el-descriptions-item label="工单数量">
            {{ formatQty(schedulePreview.workorder.quantity) }}
            {{ schedulePreview.workorder.unitOfMeasure }}
          </el-descriptions-item>
          <el-descriptions-item label="工艺路线">
            {{ schedulePreview.routeCode }} {{ schedulePreview.routeName }}
          </el-descriptions-item>
          <el-descriptions-item label="已排产">
            <span class="qty-scheduled">{{ formatQty(schedulePreview.quantityScheduled) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="还可排">
            <b>{{ formatQty(schedulePreview.quantityRemain) }}</b>
            {{ schedulePreview.workorder.unitOfMeasure }}
          </el-descriptions-item>
        </el-descriptions>

        <el-form :model="scheduleForm" label-width="110px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="本次排产数量" required>
                <el-input-number
                  v-model="scheduleForm.quantity"
                  :min="0.000001"
                  :max="scheduleMaxQuantity"
                  :precision="2"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划开工时间" required>
                <el-date-picker
                  v-model="scheduleForm.planStartTime"
                  type="datetime"
                  placeholder="请选择计划开工时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <el-divider content-position="left">工序与工作站（逐道指定）</el-divider>

        <el-table :data="scheduleForm.items" border size="small" max-height="320">
          <el-table-column label="顺序" width="60" align="center">
            <template #default="{ $index }"><b>{{ $index + 1 }}</b></template>
          </el-table-column>
          <el-table-column label="工序" min-width="140">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.processCode }}</el-tag>
              <span class="ml4">{{ row.processName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="衔接" width="90" align="center">
            <template #default="{ row }">
              <!-- SS 表示与上一道并行开工，其余按顺序接上一道结束 -->
              <el-tag v-if="row.linkType === 'SS'" type="warning" size="small">并行</el-tag>
              <span v-else class="text-muted">顺序</span>
            </template>
          </el-table-column>
          <el-table-column label="单件工时" width="100" align="right">
            <template #default="{ row }">
              {{ row.unitTime ? row.unitTime + ' 分' : '—' }}
            </template>
          </el-table-column>
          <el-table-column label="准备/等待" width="110" align="right">
            <template #default="{ row }">
              <span class="text-muted">{{ row.defaultPreTime || 0 }} / {{ row.defaultSufTime || 0 }} 分</span>
            </template>
          </el-table-column>
          <el-table-column label="工作站" min-width="220">
            <template #default="{ row }">
              <!-- 没有候选工作站的工序必须挡住，否则排出来没人干 -->
              <el-select
                v-if="row.workstations && row.workstations.length"
                v-model="row.workstationId"
                placeholder="请选择工作站"
                filterable
                style="width: 100%"
              >
                <el-option v-for="w in row.workstations" :key="w.workstationId"
                           :label="w.workstationCode + ' ' + w.workstationName" :value="w.workstationId" />
              </el-select>
              <el-tag v-else type="danger" size="small">
                该工序没有可用工作站，请先到工作站管理里配置
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="关键/检验" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.keyFlag === 'KEY_PROCESS'" type="danger" size="small">关键</el-tag>
              <el-tag v-if="row.isCheck === 'Y'" type="warning" size="small" class="ml4">检</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="schedule-tip">
          排产后会按上表逐道工序生成生产任务，时间从「计划开工时间」开始，
          按每道工序的「准备时间 + 单件工时 × 数量 + 等待时间」依次推算。
        </div>
      </template>

      <template #footer>
        <el-button @click="scheduleVisible = false">取消</el-button>
        <el-button type="primary" :loading="scheduleLoading" @click="handleScheduleSubmit">
          执行排产
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Edit, Delete, Close, View,
  Check, CircleCheck, CircleClose, Calendar, RefreshLeft
} from '@element-plus/icons-vue'

import {
  getWorkorderByPage,
  getWorkorderById,
  createWorkorder,
  updateWorkorder,
  deleteWorkorder,
  deleteWorkorderBatch,
  confirmWorkorder,
  finishWorkorder,
  cancelWorkorder
} from '@/api/pro/workorder.js'

import { getSchedulePreview, scheduleTask, cancelSchedule } from '@/api/pro/task.js'

// 下拉数据源走别的模块已有的接口，不重复造
import { getClientByPage } from '@/api/md/client.js'
import { getVendorByPage } from '@/api/md/vendor.js'
import { getItemByPage } from '@/api/md/item.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'
import { autoCode } from '@/api/sys/codingRule.js'

const router = useRouter()

// ==================== 字典与下拉数据源 ====================

// 三个字典都从后端拉，不在前端硬编码中文 —— 以后改字典不用改代码
const workorderTypeOptions = ref([])
const orderSourceOptions = ref([])
const statusOptions = ref([])
const clientOptions = ref([])
const vendorOptions = ref([])
const productOptions = ref([])

/** 按字典类型拉数据 */
async function loadDict(type, target) {
  const result = await getDictDataListByType(type)
  target.value = (result.data || []).filter(d => d.status === '0')
}

/** 拉客户（只取启用且是产品的不能当客户，这里客户表本身就是客户） */
async function loadClients() {
  const result = await getClientByPage({ pageNum: 1, pageSize: 500 })
  clientOptions.value = (result.data?.list || []).filter(c => c.enableFlag === 'Y')
}

/** 拉供应商 */
async function loadVendors() {
  const result = await getVendorByPage({ pageNum: 1, pageSize: 500 })
  vendorOptions.value = (result.data?.list || []).filter(v => v.enableFlag === 'Y')
}

/** 拉产品（item_or_product = PRODUCT 才是能开工单的产品） */
async function loadProducts() {
  const result = await getItemByPage({ pageNum: 1, pageSize: 500, itemOrProduct: 'PRODUCT' })
  productOptions.value = (result.data?.list || []).filter(i => i.enableFlag === 'Y')
}

/** 字典值转标签 */
function dictLabel(options, value, fallback) {
  const hit = options.value.find(d => d.dictValue === value)
  return hit ? hit.dictLabel : (fallback || value || '—')
}

const typeLabel = (v) => dictLabel(workorderTypeOptions, v)
const sourceLabel = (v) => dictLabel(orderSourceOptions, v)
const statusLabel = (v) => dictLabel(statusOptions, v)

// 标签颜色也跟字典的 list_class 走，字典里配了 warning 就是橙色
function dictTagType(options, value) {
  const hit = options.value.find(d => d.dictValue === value)
  const cls = hit?.listClass
  // el-tag 认的 type 里没有 primary，用空字符串表示默认蓝色
  if (cls === 'primary' || !cls) return ''
  if (['success', 'info', 'warning', 'danger'].includes(cls)) return cls
  return ''
}

const typeTagType = (v) => dictTagType(workorderTypeOptions, v)
const statusTagType = (v) => dictTagType(statusOptions, v)

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  workorderCode: '',
  workorderName: '',
  workorderType: null,
  orderSource: null,
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ==================== 数据源 ====================

const tableData = ref([])
const selectedRows = ref([])
const tableRef = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const codeLoading = ref(false)
const workorderFormRef = ref(null)

const defaultForm = () => ({
  workorderId: null,
  workorderCode: '',
  workorderName: '',
  workorderType: 'SELF',
  orderSource: 'ORDER',
  sourceCode: '',
  productId: null,
  quantity: 1,
  clientId: null,
  clientCode: '',
  clientName: '',
  vendorId: null,
  vendorCode: '',
  vendorName: '',
  batchCode: '',
  requestDate: '',
  remark: ''
})

const workorderForm = reactive(defaultForm())

// 联动开关：控制哪些字段显示、哪些字段校验
const isClientOrder = computed(() => workorderForm.orderSource === 'ORDER')
const isOutsourced = computed(() =>
  workorderForm.workorderType === 'OUTSOURCE' || workorderForm.workorderType === 'PURCHASE'
)

/** 选了产品后回显规格，让用户确认选对了 */
const selectedProductSpc = computed(() => {
  const hit = productOptions.value.find(i => i.itemId === workorderForm.productId)
  return hit ? hit.specification : ''
})

// 校验规则刻意做成函数：字段是否必填取决于另外两个字段的选择，静态规则表达不了
const workorderRules = computed(() => ({
  workorderCode: [
    { required: true, message: '请输入工单编码', trigger: 'blur' },
    { max: 64, message: '工单编码长度不能超过 64 个字符', trigger: 'blur' }
  ],
  workorderName: [
    { required: true, message: '请输入工单名称', trigger: 'blur' },
    { max: 255, message: '工单名称长度不能超过 255 个字符', trigger: 'blur' }
  ],
  workorderType: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
  orderSource: [{ required: true, message: '请选择来源类型', trigger: 'change' }],
  // 客户订单来源时订单编号与客户必填，其他来源连字段都不显示
  sourceCode: isClientOrder.value
    ? [{ required: true, message: '客户订单来源必须填写订单编号', trigger: 'blur' }]
    : [],
  clientId: isClientOrder.value
    ? [{ required: true, message: '客户订单来源必须选择客户', trigger: 'change' }]
    : [],
  // 外协/外购时必须选供应商
  vendorId: isOutsourced.value
    ? [{ required: true, message: '外协/外购工单必须选择供应商', trigger: 'change' }]
    : [],
  productId: [{ required: true, message: '请选择生产产品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入生产数量', trigger: 'blur' }],
  requestDate: [{ required: true, message: '请选择需求日期', trigger: 'change' }]
}))

const dialogTitle = computed(() => isEdit.value ? '编辑生产工单' : '新增生产工单')

// ==================== 数据加载 ====================

/** 分页查询工单列表 */
const loadWorkorderList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getWorkorderByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

// ==================== 表格多选回调 ====================

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function clearSelection() {
  tableRef.value?.clearSelection()
}

// ==================== 搜索区操作 ====================

function handleSearch() {
  pagination.page = 1
  loadWorkorderList()
}

function handleReset() {
  queryForm.workorderCode = ''
  queryForm.workorderName = ''
  queryForm.workorderType = null
  queryForm.orderSource = null
  queryForm.status = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadWorkorderList()
}

// ==================== 表单内的联动 ====================

/**
 * 切换来源类型时清掉另一套字段
 * 不清的话会出现"改成库存备货了，但客户ID还带着"，
 * 后端交叉校验会直接拒绝，用户还不知道为什么
 */
function handleSourceTypeChange() {
  if (workorderForm.orderSource === 'STORE') {
    workorderForm.sourceCode = ''
    workorderForm.clientId = null
    workorderForm.clientCode = ''
    workorderForm.clientName = ''
  }
}

function handleClientChange(clientId) {
  const hit = clientOptions.value.find(c => c.clientId === clientId)
  workorderForm.clientCode = hit ? hit.clientCode : ''
  workorderForm.clientName = hit ? hit.clientName : ''
}

function handleVendorChange(vendorId) {
  const hit = vendorOptions.value.find(v => v.vendorId === vendorId)
  workorderForm.vendorCode = hit ? hit.vendorCode : ''
  workorderForm.vendorName = hit ? hit.vendorName : ''
}

function handleProductChange(productId) {
  const hit = productOptions.value.find(i => i.itemId === productId)
  // 工单名称没填时，用"产品名+生产工单"给个默认值，省一次手输
  if (hit && !workorderForm.workorderName) {
    workorderForm.workorderName = hit.itemName + '生产工单'
  }
}

/**
 * 调编码规则接口生成工单编码
 * 后端编码规则 rule_code = work_order，前缀 WKORDER + yyyyMMdd + 6位流水
 */
async function handleGenCode() {
  codeLoading.value = true
  try {
    const result = await autoCode('work_order')
    if (result.data) {
      workorderForm.workorderCode = result.data
    }
  } finally {
    codeLoading.value = false
  }
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(workorderForm, defaultForm())
  dialogVisible.value = true
}

async function handleEdit(row) {
  const result = await getWorkorderById(row.workorderId)
  if (result.data) {
    Object.assign(workorderForm, defaultForm())
    Object.assign(workorderForm, result.data)
    isEdit.value = true
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

/**
 * 跳转详情页：工单用料在详情页 Tab 里看
 * @param {Object} row 当前行
 */
function handleDetail(row) {
  router.push('/pro/workorderDetail/' + row.workorderId)
}

// ==================== 状态流转 ====================

async function handleConfirm(row) {
  ElMessageBox.confirm(
    `确定下达工单「${row.workorderName}」吗？下达后会按制程BOM展开工单用料（没挂制程的工单按产品BOM展开），且不能再修改产品与数量。`,
    '下达确认',
    { confirmButtonText: '确定下达', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const result = await confirmWorkorder(row.workorderId)
    ElMessage.success(`下达成功，已展开 ${result.data} 行用料`)
    loadWorkorderList()
  }).catch(() => {})
}

function handleFinish(row) {
  ElMessageBox.confirm(
    `确定将工单「${row.workorderName}」标记为已完工吗？`,
    '完工确认',
    { confirmButtonText: '确定完工', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await finishWorkorder(row.workorderId)
    ElMessage.success('已完工')
    loadWorkorderList()
  }).catch(() => {})
}

function handleCancel(row) {
  ElMessageBox.confirm(
    `确定取消工单「${row.workorderName}」吗？取消后不能再下达。`,
    '取消确认',
    { confirmButtonText: '确定取消', cancelButtonText: '再想想', type: 'warning' }
  ).then(async () => {
    await cancelWorkorder(row.workorderId)
    ElMessage.success('已取消')
    loadWorkorderList()
  }).catch(() => {})
}

// ==================== 排产 ====================

const scheduleVisible = ref(false)
const scheduleLoading = ref(false)
const schedulePreview = ref(null)

/** 排产表单：数量 + 计划开工时间 + 每道工序选的工作站 */
const scheduleForm = reactive({
  workorderId: null,
  quantity: 1,
  planStartTime: '',
  items: []
})

/**
 * el-input-number 的 max。必须保证 >= min(0.000001)，
 * 否则 Element Plus 在组件初始化时会直接抛 "min should not be greater than max"。
 * 这里读取的就绪值恒定大于 0，是个防御性兜底。
 */
const scheduleMaxQuantity = computed(() => {
  const remain = Number(schedulePreview.value?.quantityRemain)
  return Number.isFinite(remain) && remain > 0 ? remain : 1
})

/** 默认开工时间给"明天早上 8 点"，省一次手选 */
function defaultPlanStart() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  d.setHours(8, 0, 0, 0)
  const p = n => (n < 10 ? '0' + n : n)
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate())
    + ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':00'
}

/**
 * 是否已排满：已排产数量 >= 生产数量。
 * 排满后不该再让用户点「排产」——进去也排不了，只会白跑一次接口。
 */
function isFullyScheduled(row) {
  const qty = Number(row.quantity)
  const scheduled = Number(row.quantityScheduled) || 0
  return Number.isFinite(qty) && scheduled >= qty
}

/**
 * 打开排产弹窗：先调 preview 拿到要拆的工序与每道工序的候选工作站，
 * 让用户按实际产能选，而不是盲填工作站ID
 */
async function handleSchedule(row) {
  const result = await getSchedulePreview(row.workorderId)
  const data = result.data
  if (!data) {
    ElMessage.warning('取排产信息失败，请刷新页面重试')
    return
  }
  // 剩余可排数量为 0 时直接拦住。此时不能再打开弹窗：
  // el-input-number 的 min(0.000001) > max(0) 会在组件初始化时抛异常，弹窗根本渲染不出来。
  if (!(Number(data.quantityRemain) > 0)) {
    ElMessage.warning('该工单已排产数量已达生产数量，没有可排产的数量了')
    return
  }
  schedulePreview.value = data
  scheduleForm.workorderId = row.workorderId
  // 默认把剩余可排数量全排掉，用户可以改小做分批排产
  scheduleForm.quantity = Number(data.quantityRemain) || 1
  scheduleForm.planStartTime = defaultPlanStart()
  // 每道工序一行，工作站默认选第一个候选，用户可改
  scheduleForm.items = (data.processList || []).map(p => ({
    processId: p.processId,
    processCode: p.processCode,
    processName: p.processName,
    linkType: p.linkType,
    unitTime: p.unitTime,
    defaultPreTime: p.defaultPreTime,
    defaultSufTime: p.defaultSufTime,
    keyFlag: p.keyFlag,
    isCheck: p.isCheck,
    workstations: p.workstations || [],
    workstationId: (p.workstations && p.workstations.length) ? p.workstations[0].workstationId : null
  }))
  scheduleVisible.value = true
}

function handleScheduleClose() {
  schedulePreview.value = null
  scheduleForm.items = []
}

async function handleScheduleSubmit() {
  // 前端先拦一道，把明显的漏填直接指出来，省一次往返
  if (!scheduleForm.quantity || Number(scheduleForm.quantity) <= 0) {
    ElMessage.warning('本次排产数量必须大于 0')
    return
  }
  if (!scheduleForm.planStartTime) {
    ElMessage.warning('请选择计划开工时间')
    return
  }
  const missing = scheduleForm.items.find(i => !i.workstationId)
  if (missing) {
    ElMessage.warning(`工序「${missing.processName}」还没有选择工作站`)
    return
  }

  scheduleLoading.value = true
  try {
    const result = await scheduleTask({
      workorderId: scheduleForm.workorderId,
      quantity: scheduleForm.quantity,
      planStartTime: scheduleForm.planStartTime,
      items: scheduleForm.items.map(i => ({
        processId: i.processId,
        workstationId: i.workstationId
      }))
    })
    ElMessage.success(`排产成功，已拆出 ${result.data} 道工序任务`)
    scheduleVisible.value = false
    loadWorkorderList()
  } finally {
    scheduleLoading.value = false
  }
}

/** 撤销排产：物理删除该工单全部任务，已排产数量归零 */
function handleCancelSchedule(row) {
  ElMessageBox.confirm(
    `确定撤销工单「${row.workorderName}」的排产吗？该工单下的全部生产任务会被删除，已排产数量归零。`
    + `若已有工序报过工，后端会拒绝。`,
    '撤销排产确认',
    { confirmButtonText: '确定撤销', cancelButtonText: '再想想', type: 'warning' }
  ).then(async () => {
    const result = await cancelSchedule(row.workorderId)
    ElMessage.success(`已撤销 ${result.data} 道工序任务`)
    loadWorkorderList()
  }).catch(() => {})
}

// ==================== 删除 ====================

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除工单「${row.workorderName}」吗？只有待下达的工单可以删除。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteWorkorder(row.workorderId)
    ElMessage.success('删除成功')
    loadWorkorderList()
  }).catch(() => {})
}

function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条工单吗？若其中含有已下达的工单，后端会拒绝。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.workorderId)
    await deleteWorkorderBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadWorkorderList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  workorderFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateWorkorder(workorderForm)
        ElMessage.success('编辑成功')
      } else {
        await createWorkorder(workorderForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadWorkorderList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  workorderFormRef.value?.resetFields()
  Object.assign(workorderForm, defaultForm())
}

// ==================== 小工具 ====================

/** 数量去掉无意义的尾部 0，1.000000 显示成 1 */
function formatQty(v) {
  if (v === null || v === undefined || v === '') return '0'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return String(Number(n.toFixed(6)))
}

onMounted(async () => {
  await Promise.all([
    loadDict('workorder_type', workorderTypeOptions),
    loadDict('order_source', orderSourceOptions),
    loadDict('production_order_status', statusOptions),
    loadClients(),
    loadVendors(),
    loadProducts()
  ])
  loadWorkorderList()
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
}

.search-form {
  margin-bottom: 16px;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  margin-bottom: 12px;
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
}

.batch-tip {
  color: #409EFF;
  font-size: 14px;
}

.batch-tip b {
  color: #f56c6c;
  font-size: 16px;
  margin: 0 2px;
}

.text-muted {
  color: #909399;
}

.ml4 {
  margin-left: 4px;
}

.qty-scheduled {
  color: #409EFF;
}

.schedule-tip {
  margin-top: 12px;
  padding: 10px 14px;
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  color: #409EFF;
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
