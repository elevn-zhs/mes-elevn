<template>
  <div class="md-workstation">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>工作站管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增工作站</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="工作站编码">
          <el-input v-model="queryForm.workstationCode" placeholder="请输入工作站编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="工作站名称">
          <el-input v-model="queryForm.workstationName" placeholder="请输入工作站名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-select v-model="queryForm.enableFlag" placeholder="请选择" clearable style="width: 110px">
            <el-option label="是" value="Y" />
            <el-option label="否" value="N" />
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
        <el-table-column prop="workstationId" label="ID" width="80" />
        <el-table-column prop="workstationCode" label="工作站编码" min-width="140" show-overflow-tooltip >
          <template #default="scope">
            <el-link @click="handleDetail(scope.row)" type="primary">{{scope.row.workstationCode}}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="workstationName" label="工作站名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="workshopName" label="所在车间名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="processName" label="工序名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="workstationAddress" label="工作站地点" min-width="140" show-overflow-tooltip />
        <el-table-column prop="warehouseName" label="线边库名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="enableFlag" label="是否启用" width="100" align="center">
          <template #default="{ row }">
            <!-- enableFlag 是 char(1)：'Y' 是，'N' 否，不能直接拿布尔判断 -->
            <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
              {{ row.enableFlag === 'Y' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
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

    <!-- ============ 新增/编辑弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="760px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="workstationFormRef"
        :model="workstationForm"
        :rules="workstationFormRules"
        label-width="110px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作站编码" prop="workstationCode">
              <el-input v-model="workstationForm.workstationCode" placeholder="请输入工作站编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作站名称" prop="workstationName">
              <el-input v-model="workstationForm.workstationName" placeholder="请输入工作站名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作站地点" prop="workstationAddress">
              <el-input v-model="workstationForm.workstationAddress" placeholder="请输入工作站地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在车间" prop="workshopId">
              <!-- 选完自动带出 workshopCode / workshopName，不用手填 -->
              <el-select
                v-model="workstationForm.workshopId"
                filterable clearable placeholder="请选择所在车间" style="width: 100%"
                @change="handleRefChange"
              >
                <el-option
                  v-for="r in refOptions"
                  :key="r.workshopId"
                  :label="r.workshopName"
                  :value="r.workshopId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工序ID" prop="processId">
              <el-input-number v-model="workstationForm.processId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="线边库ID" prop="warehouseId">
              <el-input-number v-model="workstationForm.warehouseId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库区ID" prop="areaId">
              <el-input-number v-model="workstationForm.areaId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库位ID" prop="locationId">
              <el-input-number v-model="workstationForm.locationId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="enableFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="workstationForm.enableFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="workstationForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Download, Close, View } from '@element-plus/icons-vue'

import {
  getWorkstationByPage,
  getWorkstationById,
  createWorkstation,
  updateWorkstation,
  deleteWorkstation,
  deleteWorkstationBatch
} from '@/api/md/workstation.js'

import { getWorkshopByPage } from '@/api/md/workshop.js'

const router = useRouter()

// ==================== 搜索与分页 ====================

/** 搜索筛选条件 */
const queryForm = reactive({
  workstationCode: '',
  workstationName: '',
  enableFlag: '',
})

/** 分页参数：当前页码 / 每页条数 / 总条数 */
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ==================== 数据源 ====================

/** 表格数据 */
const tableData = ref([])

/** 所在车间下拉选项（一次拉全量，主数据量不会太大） */
const refOptions = ref([])

/** 当前勾选的行 */
const selectedRows = ref([])

/** el-table 组件引用 */
const tableRef = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const workstationFormRef = ref(null)

const defaultForm = () => ({
  workstationCode: '',
  workstationName: '',
  workstationAddress: '',
  workshopId: 0,
  processId: 0,
  warehouseId: 0,
  areaId: 0,
  locationId: 0,
  enableFlag: 'Y',
  remark: '',
})

const workstationForm = reactive(defaultForm())

const workstationFormRules = {
  workstationCode: [
    { required: true, message: '请输入工作站编码', trigger: 'blur' },
    { max: 64, message: '工作站编码长度不能超过 64 个字符', trigger: 'blur' },
  ],
  workstationName: [
    { required: true, message: '请输入工作站名称', trigger: 'blur' },
    { max: 255, message: '工作站名称长度不能超过 255 个字符', trigger: 'blur' },
  ],
  warehouseId: [
    { required: true, message: '请输入线边库ID', trigger: 'change' },
  ],
  areaId: [
    { required: true, message: '请输入库区ID', trigger: 'change' },
  ],
  locationId: [
    { required: true, message: '请输入库位ID', trigger: 'change' },
  ],
  enableFlag: [
    { required: true, message: '请输入是否启用', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => isEdit.value ? '编辑工作站' : '新增工作站')

// ==================== 数据加载 ====================

/**
 * 分页查询工作站列表
 * 把分页参数合并到查询条件里一起传给后端
 */
const loadWorkstationList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getWorkstationByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

/**
 * 加载所在车间下拉选项
 */
async function loadRefOptions() {
  const result = await getWorkshopByPage({ pageNum: 1, pageSize: 1000 })
  refOptions.value = result.data.list || []
}

// ==================== 表格多选回调 ====================

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function clearSelection() {
  tableRef.value?.clearSelection()
}

// ==================== 搜索区操作 ====================

/** 搜索：回到第一页再查 */
function handleSearch() {
  pagination.page = 1
  loadWorkstationList()
}

/** 重置：清空条件回到第一页 */
function handleReset() {
  queryForm.workstationCode = ''
  queryForm.workstationName = ''
  queryForm.enableFlag = ''
  handleSearch()
}

/** 翻页 / 改每页条数 */
function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadWorkstationList()
}

// ==================== 新增 / 编辑 ====================

/**
 * 查看详情：跳转到工作站详情页（设备 / 人力 / 工装都收在详情页的 tab 里维护）
 * 路径对应 permission.js 里 hidden 的 workstationDetail/:workstationId 路由
 * @param {Object} row 当前行
 */
function handleDetail(row) {
  router.push({path: "/md/workstationDetail/" + row.workstationId})
}

/**
 * 清空表单
 * 先 delete 掉所有旧键再赋默认值：Object.assign 只能覆盖、不能删除键，
 * 编辑过一条记录后再点新增，上一条详情里的 createBy / delFlag 之类的字段会残留在对象里一起提交
 */
function resetForm() {
  Object.keys(workstationForm).forEach(k => delete workstationForm[k])
  Object.assign(workstationForm, defaultForm())
}

function handleAdd() {
  isEdit.value = false
  resetForm()

  dialogVisible.value = true
}

/**
 * 选中所在车间后，自动把冗余字段（workshopCode、workshopName）带出来
 * 这些字段在数据库里是冗余存储，专门用来避免列表查询时频繁 join
 */
function handleRefChange(id) {
  const hit = refOptions.value.find(r => r.workshopId === id)
  if (!hit) return
  workstationForm.workshopCode = hit.workshopCode
  workstationForm.workshopName = hit.workshopName
}


/**
 * 编辑：按 id 查最新数据再回填，避免用列表里的旧数据
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getWorkstationById(row.workstationId)
  if (result.data) {
    Object.assign(workstationForm, result.data)
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 删除 ====================

/**
 * 删除单条（后端是逻辑删除，记录还在库里）
 * @param {Object} row 当前行
 */
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除工作站「${row.workstationCode}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteWorkstation(row.workstationId)
    ElMessage.success('删除成功')
    loadWorkstationList()
  }).catch(() => {})
}

/**
 * 批量删除
 * 后端提供了 /deleteBatch 接口，一次请求搞定，不用像岗位页那样并发发 N 个单条删除
 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条工作站吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.workstationId)
    await deleteWorkstationBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadWorkstationList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  workstationFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateWorkstation(workstationForm)
        ElMessage.success('编辑成功')
      } else {
        await createWorkstation(workstationForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadWorkstationList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  workstationFormRef.value?.resetFields()
  Object.assign(workstationForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 把当前页数据导出为 CSV
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const headers = ['工作站ID', '工作站编码', '工作站名称', '所在车间名称', '工序名称', '工作站地点', '线边库名称', '是否启用', '创建时间']
  const rows = tableData.value.map(item => [
    item.workstationId,
    item.workstationCode,
    item.workstationName,
    item.workshopName,
    item.processName,
    item.workstationAddress,
    item.warehouseName,
    item.enableFlag === 'Y' ? '是' : '否',
    item.createTime
  ])
  const csv = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `workstation_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadRefOptions()
  loadWorkstationList()
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
