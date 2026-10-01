<template>
  <div class="wm-package">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>装箱管理（箱套箱）</span>
          <div>
            <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增顶层箱</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="箱号">
          <el-input v-model="queryForm.packageCode" placeholder="装箱单编号" clearable />
        </el-form-item>
        <el-form-item label="销售订单">
          <el-input v-model="queryForm.soCode" placeholder="SO 编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="d in statusOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="packageId" label="ID" width="70" />
        <el-table-column prop="packageCode" label="箱号" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="handleDetail(row)">{{ row.packageCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="parentCode" label="父箱" width="150">
          <template #default="{ row }">{{ row.parentCode || '—（顶层箱）' }}</template>
        </el-table-column>
        <el-table-column prop="clientName" label="客户" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ row.clientName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="soCode" label="销售订单" min-width="130">
          <template #default="{ row }">{{ row.soCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="itemCount" label="物料种数" width="90" align="right" />
        <el-table-column prop="totalQuantity" label="箱内总量" width="95" align="right" />
        <el-table-column prop="packageDate" label="装箱日期" width="160" />
        <el-table-column label="状态" width="95" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PACKED' ? 'success' : 'info'" size="small">
              {{ row.statusName || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" :disabled="row.status === 'PACKED'"
                       @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" :icon="Plus" :disabled="row.status === 'PACKED'"
                       @click="handleAdd(row)">套子箱</el-button>
            <el-button link type="primary" :icon="Box" :disabled="row.status === 'PACKED'"
                       @click="openLineDialog(row)">装货</el-button>
            <el-button link type="success" :disabled="row.status === 'PACKED'"
                       @click="handleFinish(row)">完成</el-button>
            <el-button link type="danger" :disabled="row.status === 'PACKED'"
                       @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
                       :page-sizes="[10, 20, 50]" :total="pagination.total"
                       layout="total, sizes, prev, pager, next" background
                       @change="handlePageChange" />
      </div>
    </el-card>

    <!-- 新增/编辑箱 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑箱' : '新增箱'" width="680px"
               :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="pkgFormRef" :model="pkgForm" :rules="pkgRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="父箱">
              <el-input :model-value="parentCodeDisplay" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="装箱日期" prop="packageDate">
              <el-date-picker v-model="pkgForm.packageDate" type="datetime"
                              value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户">
              <el-select v-model="pkgForm.clientId" placeholder="请选择客户" clearable filterable
                         style="width: 100%" @change="handleClientChange">
                <el-option v-for="c in clientOptions" :key="c.clientId"
                           :label="c.clientName" :value="c.clientId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售订单">
              <el-input v-model="pkgForm.soCode" placeholder="SO 编号，选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6"><el-form-item label="长"><el-input-number v-model="pkgForm.packageLength" :min="0" :max="99999" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="宽"><el-input-number v-model="pkgForm.packageWidth" :min="0" :max="99999" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="高"><el-input-number v-model="pkgForm.packageHeight" :min="0" :max="99999" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="尺寸单位"><el-input v-model="pkgForm.sizeUnit" placeholder="mm" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="净重"><el-input-number v-model="pkgForm.netWeight" :min="0" :max="999999" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="毛重"><el-input-number v-model="pkgForm.grossWeight" :min="0" :max="999999" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="重量单位"><el-input v-model="pkgForm.weightUnit" placeholder="kg" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="pkgForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 装货：从库存行选 -->
    <el-dialog v-model="lineDialogVisible" :title="`装货 → ${currentPkg?.packageCode || ''}`"
               width="860px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 10px"
                title="从库存里选要装箱的货（带出批次与库位快照）；装箱不扣库存，出库过账才扣" />
      <el-form :inline="true">
        <el-form-item label="物料">
          <el-input v-model="stockFilter.itemCode" placeholder="物料编码" clearable />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="stockFilter.onlyStock" :true-value="'Y'" :false-value="''">只看有货</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStockRows">查询库存</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="stockRows" border size="small" max-height="300"
                @current-change="row => selectedStock = row" highlight-current-row>
        <el-table-column prop="itemCode" label="物料编码" min-width="120" />
        <el-table-column prop="itemName" label="名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="batchCode" label="批次" min-width="140">
          <template #default="{ row }">{{ row.batchCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" min-width="110" />
        <el-table-column prop="locationCode" label="库位" width="100" />
        <el-table-column prop="quantityOnhand" label="现存" width="80" align="right" />
      </el-table>

      <el-form :inline="true" style="margin-top: 12px">
        <el-form-item label="本次装箱数量">
          <el-input-number v-model="lineQty" :min="1" :max="maxLineQty" controls-position="right" />
          <span v-if="selectedStock" style="margin-left: 8px; color: #909399">
            现存 {{ selectedStock.quantityOnhand }}
          </span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!selectedStock" @click="handleAddLine">加入箱内</el-button>
        </el-form-item>
      </el-form>

      <el-divider content-position="left">箱内明细</el-divider>
      <el-table :data="currentLines" border size="small" max-height="240">
        <el-table-column prop="itemCode" label="物料编码" min-width="120" />
        <el-table-column prop="itemName" label="名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="batchCode" label="批次" min-width="140">
          <template #default="{ row }">{{ row.batchCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="locationCode" label="库位" width="90" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDeleteLine(row)">删</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="lineDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="handleFinishFromLine">完成装箱</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="装箱详情" width="860px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="箱号" :span="2">{{ detailData.packageCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 'PACKED' ? 'success' : 'info'" size="small">
            {{ detailData.statusName || detailData.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="父箱">{{ detailData.parentCode || '—（顶层箱）' }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detailData.clientName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销售订单">{{ detailData.soCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="装箱日期">{{ detailData.packageDate }}</el-descriptions-item>
        <el-descriptions-item label="条码">
          {{ detailData.barcodeContent || '未生成' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">箱内明细</el-divider>
      <el-table :data="detailData.lineList || []" border size="small" max-height="260">
        <el-table-column prop="itemCode" label="物料编码" min-width="120" />
        <el-table-column prop="itemName" label="名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="batchCode" label="批次" min-width="140">
          <template #default="{ row }">{{ row.batchCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="warehouseName" label="仓库" min-width="110" />
        <el-table-column prop="locationCode" label="库位" width="90" />
      </el-table>

      <el-divider content-position="left">子箱</el-divider>
      <el-table :data="detailData.children || []" border size="small" max-height="200">
        <el-table-column prop="packageCode" label="箱号" min-width="150" />
        <el-table-column prop="statusName" label="状态" width="100" />
        <el-table-column prop="itemCount" label="物料种数" width="100" align="right" />
        <el-table-column prop="totalQuantity" label="箱内总量" width="100" align="right" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Box, Edit } from '@element-plus/icons-vue'

import {
  getPackageByPage, getPackageById, createPackage, updatePackage,
  finishPackage, deletePackageById, createPackageLine, deletePackageLine
} from '@/api/wm/package.js'
import { getStockByPage } from '@/api/wm/stock.js'
import { getClientByPage } from '@/api/md/client.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'

// ==================== 列表 ====================
const queryForm = reactive({ packageCode: '', soCode: '', status: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const statusOptions = ref([])
const clientOptions = ref([])

const loadList = async () => {
  const result = await getPackageByPage({
    ...queryForm, pageNum: pagination.page, pageSize: pagination.size
  })
  tableData.value = result.data.list
  pagination.total = result.data.total
}

function handleSearch() { pagination.page = 1; loadList() }
function handleReset() {
  queryForm.packageCode = ''; queryForm.soCode = ''; queryForm.status = null
  handleSearch()
}
function handlePageChange(p, s) { pagination.page = p; pagination.size = s; loadList() }

// ==================== 新增/编辑箱 ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const pkgFormRef = ref(null)
const parentPkg = ref(null)

const defaultForm = () => ({
  packageId: null, packageDate: null, clientId: null,
  soCode: '', packageLength: null, packageWidth: null, packageHeight: null,
  sizeUnit: '', netWeight: null, grossWeight: null, weightUnit: '', remark: ''
})
const pkgForm = reactive(defaultForm())
const parentCodeDisplay = computed(() =>
  parentPkg.value ? `${parentPkg.value.packageCode}` : '—（顶层箱）')

const pkgRules = {
  packageDate: [{ required: true, message: '请选择装箱日期', trigger: 'change' }]
}

function handleAdd(row) {
  isEdit.value = false
  parentPkg.value = row
  Object.assign(pkgForm, defaultForm())
  dialogVisible.value = true
}

async function handleEdit(row) {
  const result = await getPackageById(row.packageId)
  Object.assign(pkgForm, defaultForm(), {
    packageId: result.data.packageId,
    packageDate: result.data.packageDate,
    clientId: result.data.clientId, soCode: result.data.soCode,
    packageLength: result.data.packageLength, packageWidth: result.data.packageWidth,
    packageHeight: result.data.packageHeight, sizeUnit: result.data.sizeUnit,
    netWeight: result.data.netWeight, grossWeight: result.data.grossWeight,
    weightUnit: result.data.weightUnit, remark: result.data.remark
  })
  parentPkg.value = null
  isEdit.value = true
  dialogVisible.value = true
}

function handleClientChange() { /* 客户名快照由后端回填，前端只传 id */ }

async function handleSubmit() {
  pkgFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updatePackage(pkgForm)
        ElMessage.success('保存成功')
      } else {
        const body = { ...pkgForm, parentId: parentPkg.value ? parentPkg.value.packageId : 0 }
        await createPackage(body)
        ElMessage.success('新增成功，箱号由系统生成')
      }
      dialogVisible.value = false
      loadList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  pkgFormRef.value?.resetFields()
  Object.assign(pkgForm, defaultForm())
  parentPkg.value = null
}

// ==================== 装货 ====================
const lineDialogVisible = ref(false)
const currentPkg = ref(null)
const stockRows = ref([])
const selectedStock = ref(null)
const lineQty = ref(1)
const currentLines = ref([])
const stockFilter = reactive({ itemCode: '', onlyStock: 'Y' })

/** el-input-number 的 max 必须恒 <= min（ElementPlus 陷阱），异步数据兜底为 1 */
const maxLineQty = computed(() =>
  selectedStock.value && selectedStock.value.quantityOnhand > 0
    ? selectedStock.value.quantityOnhand : 1)

async function openLineDialog(row) {
  currentPkg.value = row
  selectedStock.value = null
  lineQty.value = 1
  lineDialogVisible.value = true
  await loadStockRows()
  await loadLines()
}

async function loadStockRows() {
  const result = await getStockByPage({
    itemCode: stockFilter.itemCode, onlyStock: stockFilter.onlyStock,
    pageNum: 1, pageSize: 50
  })
  stockRows.value = result.data.list || []
}

async function loadLines() {
  const result = await getPackageById(currentPkg.value.packageId)
  currentLines.value = result.data.lineList || []
}

async function handleAddLine() {
  if (!selectedStock.value) return
  if (!lineQty.value || lineQty.value <= 0) {
    ElMessage.warning('请填写装箱数量'); return
  }
  await createPackageLine({
    packageId: currentPkg.value.packageId,
    materialStockId: selectedStock.value.materialStockId,
    quantity: lineQty.value
  })
  ElMessage.success('已加入箱内')
  lineQty.value = 1
  selectedStock.value = null
  await loadLines()
  loadList()
}

async function handleDeleteLine(row) {
  await deletePackageLine(row.lineId)
  ElMessage.success('已移出')
  await loadLines()
  loadList()
}

// ==================== 完成 / 删除 ====================
function handleFinish(row) {
  ElMessageBox.confirm(
    `完成后箱内明细锁定，不能再装货或改箱信息。确认完成「${row.packageCode}」？`,
    '完成装箱', { type: 'warning' }
  ).then(async () => {
    await finishPackage(row.packageId)
    ElMessage.success('已完成装箱')
    loadList()
  }).catch(() => {})
}

function handleFinishFromLine() {
  handleFinish(currentPkg.value)
  lineDialogVisible.value = false
}

function handleDelete(row) {
  ElMessageBox.confirm(
    `删除「${row.packageCode}」会连子箱和箱内明细一起删（仅限未完成的箱）。确认删除？`,
    '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deletePackageById(row.packageId)
    ElMessage.success('删除成功')
    loadList()
  }).catch(() => {})
}

// ==================== 详情 ====================
const detailVisible = ref(false)
const detailData = ref({})

async function handleDetail(row) {
  const result = await getPackageById(row.packageId)
  detailData.value = result.data
  detailVisible.value = true
}

onMounted(() => {
  loadList()
  getDictDataListByType('wm_package_status').then(r => { statusOptions.value = r.data || [] })
  getClientByPage({ pageNum: 1, pageSize: 500 }).then(r => { clientOptions.value = r.data.list || [] })
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 12px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
