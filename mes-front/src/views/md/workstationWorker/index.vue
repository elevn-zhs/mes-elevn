<template>
  <div class="md-workstationWorker">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>工作站人力资源管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增工作站人力资源</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <!-- 父级筛选：子表数据都挂在父记录下面，先选父级再看明细 -->
        <el-form-item label="工作站">
          <el-select
            v-model="queryForm.workstationId"
            filterable clearable placeholder="请选择工作站" style="width: 220px"
            @change="handleSearch"
          >
            <el-option
              v-for="p in parentOptions"
              :key="p.workstationId"
              :label="p.workstationCode + ' - ' + p.workstationName"
              :value="p.workstationId"
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
        <el-table-column prop="recordId" label="ID" width="80" />
        <el-table-column prop="postCode" label="岗位编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="postName" label="岗位名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
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
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="workstationWorkerFormRef"
        :model="workstationWorkerForm"
        :rules="workstationWorkerFormRules"
        label-width="110px"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="工作站ID" prop="workstationId">
              <el-select
                v-model="workstationWorkerForm.workstationId"
                filterable placeholder="请选择工作站" style="width: 100%"
              >
                <el-option
                  v-for="p in parentOptions"
                  :key="p.workstationId"
                  :label="p.workstationCode + ' - ' + p.workstationName"
                  :value="p.workstationId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位ID" prop="postId">
              <el-input-number v-model="workstationWorkerForm.postId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位编码" prop="postCode">
              <el-input v-model="workstationWorkerForm.postCode" placeholder="请输入岗位编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位名称" prop="postName">
              <el-input v-model="workstationWorkerForm.postName" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="workstationWorkerForm.quantity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="workstationWorkerForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Download, Close } from '@element-plus/icons-vue'

import {
  getWorkstationWorkerByPage,
  getWorkstationWorkerById,
  createWorkstationWorker,
  updateWorkstationWorker,
  deleteWorkstationWorker,
  deleteWorkstationWorkerBatch
} from '@/api/md/workstationWorker.js'

import { getWorkstationByPage } from '@/api/md/workstation.js'

// ==================== 搜索与分页 ====================

/** 搜索筛选条件 */
const queryForm = reactive({
  workstationId: null,
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

/** 父级（工作站）下拉选项，一次拉全量即可，主数据量不会太大 */
const parentOptions = ref([])

/** 当前勾选的行 */
const selectedRows = ref([])

/** el-table 组件引用 */
const tableRef = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const workstationWorkerFormRef = ref(null)

const defaultForm = () => ({
  workstationId: null,
  postId: 0,
  postCode: '',
  postName: '',
  quantity: 1,
  remark: '',
})

const workstationWorkerForm = reactive(defaultForm())

const workstationWorkerFormRules = {
  workstationId: [
    { required: true, message: '请选择工作站ID', trigger: 'change' },
  ],
  postId: [
    { required: true, message: '请输入岗位ID', trigger: 'change' },
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => isEdit.value ? '编辑工作站人力资源' : '新增工作站人力资源')

// ==================== 数据加载 ====================

/**
 * 分页查询工作站人力资源列表
 * 把分页参数合并到查询条件里一起传给后端
 */
const loadWorkstationWorkerList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getWorkstationWorkerByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

/**
 * 加载父级（工作站）下拉选项
 * 子表必须挂在某个父记录下面，所以新增/筛选时都要先有父级列表
 */
async function loadParentOptions() {
  const result = await getWorkstationByPage({ pageNum: 1, pageSize: 1000 })
  parentOptions.value = result.data.list || []
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
  loadWorkstationWorkerList()
}

/** 重置：清空条件回到第一页 */
function handleReset() {
  queryForm.workstationId = null
  handleSearch()
}

/** 翻页 / 改每页条数 */
function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadWorkstationWorkerList()
}

// ==================== 新增 / 编辑 ====================

/**
 * 清空表单
 * 先 delete 掉所有旧键再赋默认值：Object.assign 只能覆盖、不能删除键，
 * 编辑过一条记录后再点新增，上一条详情里的 createBy / delFlag 之类的字段会残留在对象里一起提交
 */
function resetForm() {
  Object.keys(workstationWorkerForm).forEach(k => delete workstationWorkerForm[k])
  Object.assign(workstationWorkerForm, defaultForm())
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  workstationWorkerForm.workstationId = queryForm.workstationId || null
  dialogVisible.value = true
}


/**
 * 编辑：按 id 查最新数据再回填，避免用列表里的旧数据
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getWorkstationWorkerById(row.recordId)
  if (result.data) {
    Object.assign(workstationWorkerForm, result.data)
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
    `确定要删除工作站人力资源「${row.postCode}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteWorkstationWorker(row.recordId)
    ElMessage.success('删除成功')
    loadWorkstationWorkerList()
  }).catch(() => {})
}

/**
 * 批量删除
 * 后端提供了 /deleteBatch 接口，一次请求搞定，不用像岗位页那样并发发 N 个单条删除
 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条工作站人力资源吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.recordId)
    await deleteWorkstationWorkerBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadWorkstationWorkerList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  workstationWorkerFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateWorkstationWorker(workstationWorkerForm)
        ElMessage.success('编辑成功')
      } else {
        await createWorkstationWorker(workstationWorkerForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadWorkstationWorkerList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  workstationWorkerFormRef.value?.resetFields()
  Object.assign(workstationWorkerForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 把当前页数据导出为 CSV
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const headers = ['记录ID', '岗位编码', '岗位名称', '数量', '创建时间']
  const rows = tableData.value.map(item => [
    item.recordId,
    item.postCode,
    item.postName,
    item.quantity,
    item.createTime
  ])
  const csv = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `workstationWorker_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadParentOptions()
  loadWorkstationWorkerList()
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
