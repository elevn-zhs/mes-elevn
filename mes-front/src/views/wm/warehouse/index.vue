<template>
  <div class="wm-warehouse">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>仓库设置</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增仓库</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="仓库编码">
          <el-input v-model="queryForm.warehouseCode" placeholder="请输入仓库编码" clearable />
        </el-form-item>
        <el-form-item label="仓库名称">
          <el-input v-model="queryForm.warehouseName" placeholder="请输入仓库名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.enableFlag" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" value="Y" />
            <el-option label="停用" value="N" />
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
        <el-table-column prop="warehouseId" label="ID" width="70" />
        <el-table-column prop="warehouseCode" label="仓库编码" min-width="130">
          <template #default="{ row }">
            <el-link type="primary" @click="handleDetail(row)">{{ row.warehouseCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库名称" min-width="170" />
        <el-table-column prop="location" label="仓库位置" min-width="150" show-overflow-tooltip />
        <el-table-column prop="area" label="面积(㎡)" width="100" align="right" />
        <el-table-column label="仓管员" width="110">
          <template #default="{ row }">{{ row.charge || row.userName || '-' }}</template>
        </el-table-column>
        <el-table-column label="主管" width="110">
          <template #default="{ row }">{{ row.managerNick || row.managerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="库区/库位" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.areaCount || 0 }} / {{ row.locationCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库存总量" width="110" align="right">
          <template #default="{ row }">
            <span :class="{ 'stock-zero': !Number(row.stockQuantity) }">
              {{ row.stockQuantity ?? 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="enableFlag" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
              {{ row.enableFlag === 'Y' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <!-- ============ 新增/编辑仓库弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="640px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="warehouseFormRef"
        :model="warehouseForm"
        :rules="warehouseRules"
        label-width="100px"
      >
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input
            v-model="warehouseForm.warehouseCode"
            :disabled="isEdit"
            placeholder="如 WH-LB01"
            @blur="handleCodeBlur"
          />
          <div v-if="isEdit" class="form-tip">
            编码已冗余到库位、库存与单据中，保存时不会修改
          </div>
        </el-form-item>
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="warehouseForm.warehouseName" placeholder="如 一线边库（钣金）" />
        </el-form-item>
        <el-form-item label="仓库位置" prop="location">
          <el-input v-model="warehouseForm.location" placeholder="如 一号厂房东侧" />
        </el-form-item>
        <el-form-item label="面积(㎡)" prop="area">
          <el-input-number v-model="warehouseForm.area" :min="0" :max="999999" :precision="2" />
        </el-form-item>
        <el-form-item label="仓管员" prop="userId">
          <el-select
            v-model="warehouseForm.userId"
            placeholder="请选择仓管员"
            clearable
            filterable
            style="width: 100%"
            @change="handleUserChange"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="u.nickName ? `${u.nickName}（${u.userName}）` : u.userName"
              :value="u.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主管" prop="managerId">
          <el-select
            v-model="warehouseForm.managerId"
            placeholder="请选择主管"
            clearable
            filterable
            style="width: 100%"
            @change="handleManagerChange"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="u.nickName ? `${u.nickName}（${u.userName}）` : u.userName"
              :value="u.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enableFlag">
          <el-radio-group v-model="warehouseForm.enableFlag">
            <el-radio value="Y">启用</el-radio>
            <el-radio value="N">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="warehouseForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 仓库详情弹窗 ============ -->
    <el-dialog v-model="detailVisible" title="仓库详情" width="760px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="仓库编码">{{ detailData.warehouseCode }}</el-descriptions-item>
        <el-descriptions-item label="仓库名称">{{ detailData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="仓库位置">{{ detailData.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面积(㎡)">{{ detailData.area ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="仓管员">
          {{ detailData.charge || detailData.userName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="主管">
          {{ detailData.managerNick || detailData.managerName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.enableFlag === 'Y' ? 'success' : 'info'">
            {{ detailData.enableFlag === 'Y' ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">库存概况</el-divider>

      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-value">{{ detailData.areaCount || 0 }}</div>
            <div class="stat-label">库区数量</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-value">{{ detailData.locationCount || 0 }}</div>
            <div class="stat-label">库位数量</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-value">{{ detailData.stockQuantity ?? 0 }}</div>
            <div class="stat-label">库存总量</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-value">{{ detailData.stockItemCount || 0 }}</div>
            <div class="stat-label">物料种数</div>
          </div>
        </el-col>
      </el-row>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        style="margin-top: 16px"
        title="库存是只读账，不能直接修改，只能由出入库单据过账驱动"
      />

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Close, View } from '@element-plus/icons-vue'

import {
  getWarehouseByPage,
  getWarehouseById,
  createWarehouse,
  updateWarehouse,
  deleteWarehouse,
  deleteWarehouseBatch,
  getWarehouseByCode
} from '@/api/wm/warehouse.js'
import { getUserByPage } from '@/api/sys/user.js'

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  warehouseCode: '',
  warehouseName: '',
  enableFlag: null
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

// 用户下拉数据（仓管员 / 主管）
const userOptions = ref([])

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const warehouseFormRef = ref(null)

const defaultForm = () => ({
  warehouseId: null,
  warehouseCode: '',
  warehouseName: '',
  location: '',
  area: null,
  userId: null,
  userName: '',
  charge: '',
  managerId: null,
  managerName: '',
  managerNick: '',
  enableFlag: 'Y',
  remark: ''
})

const warehouseForm = reactive(defaultForm())

// 编码：新增时必填；编辑时后端不允许改，前端也锁死
const warehouseRules = computed(() => ({
  warehouseCode: isEdit.value
    ? []
    : [
        { required: true, message: '请输入仓库编码', trigger: 'blur' },
        { max: 64, message: '仓库编码长度不能超过 64 个字符', trigger: 'blur' }
      ],
  warehouseName: [
    { required: true, message: '请输入仓库名称', trigger: 'blur' },
    { max: 255, message: '仓库名称长度不能超过 255 个字符', trigger: 'blur' }
  ]
}))

const dialogTitle = computed(() => isEdit.value ? '编辑仓库' : '新增仓库')

// ==================== 详情弹窗相关 ====================

const detailVisible = ref(false)
const detailData = ref({})

// ==================== 数据加载 ====================

/** 分页查询仓库列表 */
const loadWarehouseList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getWarehouseByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

/** 加载用户下拉数据（供仓管员/主管选择） */
const loadUserOptions = async function () {
  const result = await getUserByPage({ pageNum: 1, pageSize: 200 })
  userOptions.value = result.data.list || []
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
  loadWarehouseList()
}

function handleReset() {
  queryForm.warehouseCode = ''
  queryForm.warehouseName = ''
  queryForm.enableFlag = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadWarehouseList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(warehouseForm, defaultForm())
  dialogVisible.value = true
}

/**
 * 编辑：按 warehouseId 查最新数据再回填
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  const result = await getWarehouseById(row.warehouseId)
  if (result.data) {
    Object.assign(warehouseForm, defaultForm(), result.data)
    isEdit.value = true
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

/**
 * 编码失焦：实时校验是否已被占用（用户体验上的第一道防线，最终以后端为准）
 */
async function handleCodeBlur() {
  const code = warehouseForm.warehouseCode
  if (isEdit.value || !code) return
  const result = await getWarehouseByCode(code)
  if (result.data) {
    ElMessage.warning(`仓库编码「${code}」已被占用`)
  }
}

/**
 * 选中仓管员：把 userId / userName / charge 一起带上
 * 后端只认这三个冗余字段，前端一次填齐，页面列表就不用再回查用户表
 */
function handleUserChange(userId) {
  const user = userOptions.value.find(u => u.userId === userId)
  if (user) {
    warehouseForm.userName = user.userName
    warehouseForm.charge = user.nickName || user.userName
  } else {
    warehouseForm.userName = ''
    warehouseForm.charge = ''
  }
}

/** 选中主管：同理带上 managerId / managerName / managerNick */
function handleManagerChange(userId) {
  const user = userOptions.value.find(u => u.userId === userId)
  if (user) {
    warehouseForm.managerName = user.userName
    warehouseForm.managerNick = user.nickName || user.userName
  } else {
    warehouseForm.managerName = ''
    warehouseForm.managerNick = ''
  }
}

// ==================== 详情 ====================

async function handleDetail(row) {
  const result = await getWarehouseById(row.warehouseId)
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
    `确定要删除仓库「${row.warehouseName}」吗？若下挂库区库位或还有库存，后端会拒绝删除。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteWarehouse(row.warehouseId)
    ElMessage.success('删除成功')
    loadWarehouseList()
  }).catch(() => {})
}

function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个仓库吗？`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.warehouseId)
    await deleteWarehouseBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadWarehouseList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  warehouseFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateWarehouse(warehouseForm)
        ElMessage.success('编辑成功')
      } else {
        await createWarehouse(warehouseForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadWarehouseList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  warehouseFormRef.value?.resetFields()
  Object.assign(warehouseForm, defaultForm())
}

onMounted(() => {
  loadWarehouseList()
  loadUserOptions()
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

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 2px;
}

.stock-zero {
  color: #c0c4cc;
}

.stat-box {
  text-align: center;
  padding: 12px 0;
  background: #f5f7fa;
  border-radius: 6px;
}

.stat-value {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
