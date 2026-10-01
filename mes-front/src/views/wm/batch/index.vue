<template>
  <div class="wm-batch">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>批次管理</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增批次</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="物料编码">
          <el-input v-model="queryForm.itemCode" placeholder="请输入物料编码" clearable />
        </el-form-item>
        <el-form-item label="批次编号">
          <el-input v-model="queryForm.batchCode" placeholder="请输入批次编号" clearable />
        </el-form-item>
        <el-form-item label="生产批号">
          <el-input v-model="queryForm.lotNumber" placeholder="请输入生产批号" clearable />
        </el-form-item>
        <el-form-item label="质量状态">
          <el-select v-model="queryForm.qualityStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="d in qualityStatusOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="生产日期">
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

      <!-- ============ 批量操作工具栏 ============ -->
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
        <el-table-column prop="batchId" label="ID" width="70" />
        <el-table-column prop="batchCode" label="批次编号" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="handleDetail(row)">{{ row.batchCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="itemCode" label="物料编码" min-width="130" />
        <el-table-column prop="itemName" label="物料名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格型号" min-width="130" show-overflow-tooltip />
        <el-table-column prop="lotNumber" label="生产批号" width="130" show-overflow-tooltip />
        <el-table-column prop="produceDate" label="生产日期" width="160" />
        <el-table-column prop="expireDate" label="有效期" width="160">
          <template #default="{ row }">{{ row.expireDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="供应商" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.vendorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="质量状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="dictTagType(qualityStatusOptions, row.qualityStatus)" size="small">
              {{ dictLabel(qualityStatusOptions, row.qualityStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
          @change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- ============ 新增/编辑批次弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <!-- 批次属性提示：告诉用户"该填哪些"是物料配置说了算，不是页面随便定的 -->
      <el-alert
        v-if="configLoadedText"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 14px"
        :title="configLoadedText"
      />

      <el-form
        ref="batchFormRef"
        :model="batchForm"
        :rules="batchRules"
        label-width="110px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="批次编号" prop="batchCode">
              <el-input v-model="batchForm.batchCode" disabled placeholder="保存后由系统自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料" prop="itemId">
              <el-select v-model="batchForm.itemId" placeholder="请选择物料" filterable
                         :disabled="isEdit" style="width: 100%" @change="handleItemChange">
                <el-option v-for="i in itemOptions" :key="i.itemId"
                           :label="`${i.itemCode} ${i.itemName}`" :value="i.itemId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规格型号">
              <el-input :model-value="selectedItemSpec" disabled placeholder="选择物料后自动带出" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input :model-value="selectedItemUnit" disabled placeholder="选择物料后自动带出" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="生产日期" prop="produceDate"
                          :required="isAttrRequired('produceDateFlag')">
              <el-date-picker v-model="batchForm.produceDate" type="datetime"
                              value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择生产日期"
                              style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期" prop="expireDate"
                          :required="isAttrRequired('expireDateFlag')">
              <el-date-picker v-model="batchForm.expireDate" type="datetime"
                              value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择有效期"
                              style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入库日期" prop="recptDate"
                          :required="isAttrRequired('recptDateFlag')">
              <el-date-picker v-model="batchForm.recptDate" type="datetime"
                              value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择入库日期"
                              style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产批号" prop="lotNumber"
                          :required="isAttrRequired('lotNumberFlag')">
              <el-input v-model="batchForm.lotNumber" placeholder="如 LOT-20260923-01" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商" prop="vendorId"
                          :required="isAttrRequired('vendorFlag')">
              <el-select v-model="batchForm.vendorId" placeholder="请选择供应商" clearable
                         filterable style="width: 100%" @change="handleVendorChange">
                <el-option v-for="v in vendorOptions" :key="v.vendorId"
                           :label="v.vendorName" :value="v.vendorId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户" prop="clientId"
                          :required="isAttrRequired('clientFlag')">
              <el-select v-model="batchForm.clientId" placeholder="请选择客户" clearable
                         filterable style="width: 100%" @change="handleClientChange">
                <el-option v-for="c in clientOptions" :key="c.clientId"
                           :label="c.clientName" :value="c.clientId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="销售订单号" prop="soCode"
                          :required="isAttrRequired('coCodeFlag')">
              <el-input v-model="batchForm.soCode" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购订单号" prop="poCode"
                          :required="isAttrRequired('poCodeFlag')">
              <el-input v-model="batchForm.poCode" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="质量状态" prop="qualityStatus"
                      :required="isAttrRequired('qualityStatusFlag')">
          <el-radio-group v-model="batchForm.qualityStatus">
            <el-radio v-for="d in qualityStatusOptions" :key="d.dictValue" :value="d.dictValue">
              {{ d.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 生产追溯三件套：配置要求哪个填哪个。
             任务跟着工单走（先选工单，任务的候选列表才会加载出来）；
             工作站独立选择。工具/模具等 D 线工具管理交付后再加，后端字段已留好。 -->
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="生产工单" prop="workorderId" label-width="90px"
                          :required="isAttrRequired('workorderFlag')">
              <el-select v-model="batchForm.workorderId" placeholder="请选择工单" clearable
                         filterable style="width: 100%" @change="handleWorkorderChange">
                <el-option v-for="w in workorderOptions" :key="w.workorderId"
                           :label="w.workorderCode" :value="w.workorderId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="生产任务" prop="taskId" label-width="90px"
                          :required="isAttrRequired('taskFlag')">
              <el-select v-model="batchForm.taskId" placeholder="先选工单" clearable
                         filterable :disabled="!batchForm.workorderId" style="width: 100%"
                         @change="handleTaskChange">
                <el-option v-for="t in taskOptions" :key="t.taskId"
                           :label="t.taskCode" :value="t.taskId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="工作站" prop="workstationId" label-width="90px"
                          :required="isAttrRequired('workstationFlag')">
              <el-select v-model="batchForm.workstationId" placeholder="请选择工作站" clearable
                         filterable style="width: 100%" @change="handleWorkstationChange">
                <el-option v-for="s in workstationOptions" :key="s.workstationId"
                           :label="`${s.workstationCode} ${s.workstationName}`" :value="s.workstationId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="batchForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="工具 / 模具字段由 D 线工具管理模块交付后再开放，当前由入库单、生产报工自动带入；其余字段按上面的批次属性配置按需填写"
      />

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 详情弹窗 ============ -->
    <el-dialog v-model="detailVisible" title="批次详情" width="820px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="批次编号" :span="2">{{ detailData.batchCode }}</el-descriptions-item>
        <el-descriptions-item label="质量状态">
          <el-tag :type="dictTagType(qualityStatusOptions, detailData.qualityStatus)" size="small">
            {{ dictLabel(qualityStatusOptions, detailData.qualityStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="物料编码">{{ detailData.itemCode }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ detailData.itemName }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ detailData.specification || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产日期">{{ detailData.produceDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ detailData.expireDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入库日期">{{ detailData.recptDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产批号">{{ detailData.lotNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detailData.vendorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detailData.clientName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销售订单号">{{ detailData.soCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采购订单号">{{ detailData.poCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产工单">{{ detailData.workorderCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产任务">{{ detailData.taskCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作站">{{ detailData.workstationCode || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">
        库存分布（共 {{ detailData.totalQuantity ?? 0 }}）
      </el-divider>

      <el-table :data="detailData.stockList || []" border size="small" max-height="280">
        <el-table-column prop="warehouseName" label="仓库" min-width="140" />
        <el-table-column prop="areaName" label="库区" min-width="120" />
        <el-table-column prop="locationName" label="库位" min-width="120" />
        <el-table-column prop="quantityOnhand" label="在库数" width="90" align="right" />
        <el-table-column prop="quantityReserved" label="保留数" width="90" align="right" />
        <el-table-column prop="quantityAvailable" label="可用数" width="90" align="right" />
        <el-table-column prop="expireDate" label="有效期" width="160" />
      </el-table>
      <el-empty v-if="!(detailData.stockList || []).length"
                description="该批次当前没有库存" :image-size="60" />

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Close, View } from '@element-plus/icons-vue'

import {
  getBatchByPage,
  getBatchById,
  createBatch,
  updateBatch,
  deleteBatchById,
  deleteBatchBatch
} from '@/api/wm/batch.js'
import { getItemByPage } from '@/api/md/item.js'
import { getItemBatchConfigByItemId } from '@/api/md/itemBatchConfig.js'
import { getVendorByPage } from '@/api/md/vendor.js'
import { getClientByPage } from '@/api/md/client.js'
import { getWorkorderByPage } from '@/api/pro/workorder.js'
import { getTaskListByWorkorder } from '@/api/pro/task.js'
import { getWorkstationByPage } from '@/api/md/workstation.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  itemCode: '',
  batchCode: '',
  lotNumber: '',
  qualityStatus: null
})

// 生产日期区间，提交时拆成 produceDateStart / produceDateEnd
const dateRange = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ==================== 数据源 ====================

const tableData = ref([])
const selectedRows = ref([])
const tableRef = ref(null)

// 下拉数据源
const itemOptions = ref([])
const vendorOptions = ref([])
const clientOptions = ref([])
// 生产追溯三件套候选（工单全量 / 任务跟工单走 / 工作站全量）
const workorderOptions = ref([])
const taskOptions = ref([])
const workstationOptions = ref([])

// 字典
const qualityStatusOptions = ref([])

async function loadDict(type, target) {
  const result = await getDictDataListByType(type)
  target.value = result.data || []
}

function dictLabel(options, value, fallback) {
  const hit = unref(options).find(d => d.dictValue === value)
  return hit ? hit.dictLabel : (fallback || value || '—')
}

function dictTagType(options, value) {
  const hit = unref(options).find(d => d.dictValue === value)
  return hit ? (hit.listClass || 'info') : 'info'
}

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const batchFormRef = ref(null)

// 当前物料的批次属性配置（决定哪些字段必填）
const batchConfig = ref(null)

const defaultForm = () => ({
  batchId: null,
  batchCode: '',
  itemId: null,
  produceDate: null,
  expireDate: null,
  recptDate: null,
  lotNumber: '',
  vendorId: null,
  vendorCode: '',
  vendorName: '',
  vendorNick: '',
  clientId: null,
  clientCode: '',
  clientName: '',
  clientNick: '',
  soCode: '',
  poCode: '',
  workorderId: null,
  workorderCode: '',
  taskId: null,
  taskCode: '',
  workstationId: null,
  workstationCode: '',
  qualityStatus: 'PENDING',
  remark: ''
})

const batchForm = reactive(defaultForm())

/** 选中的物料（用来显示规格与单位） */
const selectedItem = computed(() => itemOptions.value.find(i => i.itemId === batchForm.itemId))

const selectedItemSpec = computed(() => selectedItem.value?.specification || '')
const selectedItemUnit = computed(() => selectedItem.value?.unitName || selectedItem.value?.unitOfMeasure || '')

/** 物料批次配置是否生效 */
const configActive = computed(() =>
  !!(batchConfig.value && batchConfig.value.enableFlag === 'Y'))

/** 某个属性是否被配置为必填 */
const isAttrRequired = (flag) => configActive.value && batchConfig.value[flag] === 'Y'

/** 弹窗顶部的提示文案 */
const configLoadedText = computed(() => {
  if (!batchForm.itemId) return ''
  if (!batchConfig.value) {
    return '该物料没有配置批次属性规则，所有属性都可以按需填写'
  }
  if (!configActive.value) {
    return '该物料的批次属性规则未启用，所有属性都可以按需填写'
  }
  const required = []
  if (isAttrRequired('produceDateFlag')) required.push('生产日期')
  if (isAttrRequired('expireDateFlag')) required.push('有效期')
  if (isAttrRequired('recptDateFlag')) required.push('入库日期')
  if (isAttrRequired('vendorFlag')) required.push('供应商')
  if (isAttrRequired('clientFlag')) required.push('客户')
  if (isAttrRequired('coCodeFlag')) required.push('销售订单号')
  if (isAttrRequired('poCodeFlag')) required.push('采购订单号')
  if (isAttrRequired('lotNumberFlag')) required.push('生产批号')
  if (isAttrRequired('qualityStatusFlag')) required.push('质量状态')
  if (isAttrRequired('workorderFlag')) required.push('生产工单')
  if (isAttrRequired('taskFlag')) required.push('生产任务')
  if (isAttrRequired('workstationFlag')) required.push('工作站')
  return required.length
    ? `按物料批次属性配置，本批必须填写：${required.join('、')}`
    : '该物料的批次属性规则没有要求必填项'
})

/** 动态校验规则：必填项跟着物料配置走 */
const batchRules = computed(() => {
  const attrRule = (flag, label) => isAttrRequired(flag)
    ? [{ required: true, message: `该物料要求批次携带${label}，请填写`, trigger: 'change' }]
    : []
  return {
    itemId: [{ required: true, message: '请选择物料', trigger: 'change' }],
    produceDate: attrRule('produceDateFlag', '生产日期'),
    expireDate: attrRule('expireDateFlag', '有效期'),
    recptDate: attrRule('recptDateFlag', '入库日期'),
    lotNumber: attrRule('lotNumberFlag', '生产批号'),
    vendorId: attrRule('vendorFlag', '供应商'),
    clientId: attrRule('clientFlag', '客户'),
    soCode: attrRule('coCodeFlag', '销售订单号'),
    poCode: attrRule('poCodeFlag', '采购订单号'),
    qualityStatus: attrRule('qualityStatusFlag', '质量状态'),
    workorderId: attrRule('workorderFlag', '生产工单'),
    taskId: attrRule('taskFlag', '生产任务'),
    workstationId: attrRule('workstationFlag', '工作站')
  }
})

const dialogTitle = computed(() => isEdit.value ? '编辑批次' : '新增批次')

// ==================== 详情弹窗 ====================

const detailVisible = ref(false)
const detailData = ref({})

// ==================== 数据加载 ====================

const loadBatchList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  // 日期区间拆成起止两个参数
  if (dateRange.value && dateRange.value.length === 2) {
    params.produceDateStart = dateRange.value[0]
    params.produceDateEnd = dateRange.value[1]
  }
  const result = await getBatchByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

const loadItemOptions = async function () {
  const result = await getItemByPage({ pageNum: 1, pageSize: 1000 })
  // 只保留开启了批次管理的物料 —— 没开批次管理的物料建不了批次
  itemOptions.value = (result.data.list || []).filter(i => i.batchFlag === 'Y')
}

const loadVendorOptions = async function () {
  const result = await getVendorByPage({ pageNum: 1, pageSize: 500 })
  vendorOptions.value = result.data.list || []
}

const loadClientOptions = async function () {
  const result = await getClientByPage({ pageNum: 1, pageSize: 500 })
  clientOptions.value = result.data.list || []
}

/** 工单候选：批次追溯挂的是生产工单，全量加载（数据量小） */
const loadWorkorderOptions = async function () {
  const result = await getWorkorderByPage({ pageNum: 1, pageSize: 500 })
  workorderOptions.value = result.data.list || []
}

/** 任务候选：跟工单走，选了工单才加载 */
const loadTaskOptions = async function (workorderId) {
  if (!workorderId) {
    taskOptions.value = []
    return
  }
  const result = await getTaskListByWorkorder(workorderId)
  taskOptions.value = result.data || []
}

/** 工作站候选：全量加载 */
const loadWorkstationOptions = async function () {
  const result = await getWorkstationByPage({ pageNum: 1, pageSize: 500 })
  workstationOptions.value = result.data.list || []
}

// ==================== 表格多选 ====================

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function clearSelection() {
  tableRef.value?.clearSelection()
}

// ==================== 搜索区操作 ====================

function handleSearch() {
  pagination.page = 1
  loadBatchList()
}

function handleReset() {
  queryForm.itemCode = ''
  queryForm.batchCode = ''
  queryForm.lotNumber = ''
  queryForm.qualityStatus = null
  dateRange.value = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadBatchList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  batchConfig.value = null
  Object.assign(batchForm, defaultForm())
  dialogVisible.value = true
}

async function handleEdit(row) {
  const result = await getBatchById(row.batchId)
  if (!result.data) {
    ElMessage.info('数据有误，请刷新页面重试')
    return
  }
  Object.assign(batchForm, defaultForm(), result.data)
  isEdit.value = true
  dialogVisible.value = true
  // 编辑时也要拉一次配置，页面上的星号才准
  await loadBatchConfig(batchForm.itemId)
  // 编辑的批次带了工单/任务时，把任务候选补加载出来，下拉才能显示任务编号
  if (batchForm.workorderId) {
    await loadTaskOptions(batchForm.workorderId)
  }
}

/**
 * 选中物料：拉该物料的批次属性配置，决定页面上哪些字段必填
 * 这是"批次属性取值规则来自 E 线 md_item_batch_config"的落地
 */
async function handleItemChange(itemId) {
  await loadBatchConfig(itemId)
}

async function loadBatchConfig(itemId) {
  if (!itemId) {
    batchConfig.value = null
    return
  }
  const result = await getItemBatchConfigByItemId(itemId)
  batchConfig.value = result.data || null
}

/** 选中供应商：把 id / 编码 / 名称 / 简称一起带上（后端只认这几个冗余字段） */
function handleVendorChange(vendorId) {
  const vendor = vendorOptions.value.find(v => v.vendorId === vendorId)
  if (vendor) {
    batchForm.vendorCode = vendor.vendorCode
    batchForm.vendorName = vendor.vendorName
    batchForm.vendorNick = vendor.vendorNick
  } else {
    batchForm.vendorCode = ''
    batchForm.vendorName = ''
    batchForm.vendorNick = ''
  }
}

/** 选中客户：同理 */
function handleClientChange(clientId) {
  const client = clientOptions.value.find(c => c.clientId === clientId)
  if (client) {
    batchForm.clientCode = client.clientCode
    batchForm.clientName = client.clientName
    batchForm.clientNick = client.clientNick
  } else {
    batchForm.clientCode = ''
    batchForm.clientName = ''
    batchForm.clientNick = ''
  }
}

/** 选中工单：带上工单编码冗余字段，并按工单加载任务候选 */
function handleWorkorderChange(workorderId) {
  const wo = workorderOptions.value.find(w => w.workorderId === workorderId)
  batchForm.workorderCode = wo ? wo.workorderCode : ''
  // 换工单后旧任务不再属于新工单，任务连带清空
  batchForm.taskId = null
  batchForm.taskCode = ''
  taskOptions.value = []
  if (workorderId) {
    loadTaskOptions(workorderId)
  }
}

function handleTaskChange(taskId) {
  const task = taskOptions.value.find(t => t.taskId === taskId)
  batchForm.taskCode = task ? task.taskCode : ''
}

/** 选中工作站：带上工作站编码冗余字段 */
function handleWorkstationChange(workstationId) {
  const ws = workstationOptions.value.find(s => s.workstationId === workstationId)
  batchForm.workstationCode = ws ? ws.workstationCode : ''
}

// ==================== 详情 ====================

async function handleDetail(row) {
  const result = await getBatchById(row.batchId)
  if (result.data) {
    detailData.value = result.data
    detailVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 删除 ====================

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除批次「${row.batchCode}」吗？若该批次下还有库存，后端会拒绝删除。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteBatchById(row.batchId)
    ElMessage.success('删除成功')
    loadBatchList()
  }).catch(() => {})
}

function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个批次吗？`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.batchId)
    await deleteBatchBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadBatchList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  batchFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateBatch(batchForm)
        ElMessage.success('编辑成功')
      } else {
        const result = await createBatch(batchForm)
        ElMessage.success(`新增成功，批次编号：${result.data?.batchCode || ''}`)
      }
      dialogVisible.value = false
      loadBatchList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  batchFormRef.value?.resetFields()
  Object.assign(batchForm, defaultForm())
  batchConfig.value = null
  taskOptions.value = []
}

onMounted(() => {
  loadBatchList()
  loadItemOptions()
  loadVendorOptions()
  loadClientOptions()
  loadWorkorderOptions()
  loadWorkstationOptions()
  loadDict('wm_batch_quality_status', qualityStatusOptions)
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

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
