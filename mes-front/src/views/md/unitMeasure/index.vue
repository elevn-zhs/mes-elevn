<template>
  <div class="md-unitMeasure">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>计量单位管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增计量单位</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="单位编码">
          <el-input v-model="queryForm.measureCode" placeholder="请输入单位编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="单位名称">
          <el-input v-model="queryForm.measureName" placeholder="请输入单位名称" clearable style="width: 180px" />
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
        <el-table-column prop="measureId" label="ID" width="80" />
        <el-table-column prop="measureCode" label="单位编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="measureName" label="单位名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="primaryFlag" label="是否是主单位" width="100" align="center">
          <template #default="{ row }">
            <!-- primaryFlag 是 char(1)：'Y' 是，'N' 否，不能直接拿布尔判断 -->
            <el-tag :type="row.primaryFlag === 'Y' ? 'success' : 'info'">
              {{ row.primaryFlag === 'Y' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeRate" label="与主单位换算比例" width="100" align="right" />
        <el-table-column prop="enableFlag" label="是否启用" width="100" align="center">
          <template #default="{ row }">
            <!-- enableFlag 是 char(1)：'Y' 是，'N' 否，不能直接拿布尔判断 -->
            <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
              {{ row.enableFlag === 'Y' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
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
        ref="unitMeasureFormRef"
        :model="unitMeasureForm"
        :rules="unitMeasureFormRules"
        label-width="110px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位编码" prop="measureCode">
              <el-input v-model="unitMeasureForm.measureCode" placeholder="请输入单位编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位名称" prop="measureName">
              <el-input v-model="unitMeasureForm.measureName" placeholder="请输入单位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否是主单位" prop="primaryFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="unitMeasureForm.primaryFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主单位ID" prop="primaryId">
              <el-input-number v-model="unitMeasureForm.primaryId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="与主单位换算比例" prop="changeRate">
              <el-input-number v-model="unitMeasureForm.changeRate" :min="0" :precision="6" :step="0.001" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="enableFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="unitMeasureForm.enableFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="unitMeasureForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
  getUnitMeasureByPage,
  getUnitMeasureById,
  createUnitMeasure,
  updateUnitMeasure,
  deleteUnitMeasure,
  deleteUnitMeasureBatch
} from '@/api/md/unitMeasure.js'

// ==================== 搜索与分页 ====================

/** 搜索筛选条件 */
const queryForm = reactive({
  measureCode: '',
  measureName: '',
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

/** 当前勾选的行 */
const selectedRows = ref([])

/** el-table 组件引用 */
const tableRef = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const unitMeasureFormRef = ref(null)

const defaultForm = () => ({
  measureCode: '',
  measureName: '',
  primaryFlag: 'Y',
  primaryId: null,
  changeRate: 0,
  enableFlag: 'Y',
  remark: '',
})

const unitMeasureForm = reactive(defaultForm())

const unitMeasureFormRules = {
  measureCode: [
    { required: true, message: '请输入单位编码', trigger: 'blur' },
    { max: 64, message: '单位编码长度不能超过 64 个字符', trigger: 'blur' },
  ],
  measureName: [
    { required: true, message: '请输入单位名称', trigger: 'blur' },
    { max: 255, message: '单位名称长度不能超过 255 个字符', trigger: 'blur' },
  ],
  primaryFlag: [
    { required: true, message: '请输入是否是主单位', trigger: 'change' },
  ],
  enableFlag: [
    { required: true, message: '请输入是否启用', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => isEdit.value ? '编辑计量单位' : '新增计量单位')

// ==================== 数据加载 ====================

/**
 * 分页查询计量单位列表
 * 把分页参数合并到查询条件里一起传给后端
 */
const loadUnitMeasureList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getUnitMeasureByPage(params)
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

/** 搜索：回到第一页再查 */
function handleSearch() {
  pagination.page = 1
  loadUnitMeasureList()
}

/** 重置：清空条件回到第一页 */
function handleReset() {
  queryForm.measureCode = ''
  queryForm.measureName = ''
  queryForm.enableFlag = ''
  handleSearch()
}

/** 翻页 / 改每页条数 */
function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadUnitMeasureList()
}

// ==================== 新增 / 编辑 ====================

/**
 * 清空表单
 * 先 delete 掉所有旧键再赋默认值：Object.assign 只能覆盖、不能删除键，
 * 编辑过一条记录后再点新增，上一条详情里的 createBy / delFlag 之类的字段会残留在对象里一起提交
 */
function resetForm() {
  Object.keys(unitMeasureForm).forEach(k => delete unitMeasureForm[k])
  Object.assign(unitMeasureForm, defaultForm())
}

function handleAdd() {
  isEdit.value = false
  resetForm()

  dialogVisible.value = true
}


/**
 * 编辑：按 id 查最新数据再回填，避免用列表里的旧数据
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getUnitMeasureById(row.measureId)
  if (result.data) {
    Object.assign(unitMeasureForm, result.data)
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
    `确定要删除计量单位「${row.measureCode}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteUnitMeasure(row.measureId)
    ElMessage.success('删除成功')
    loadUnitMeasureList()
  }).catch(() => {})
}

/**
 * 批量删除
 * 后端提供了 /deleteBatch 接口，一次请求搞定，不用像岗位页那样并发发 N 个单条删除
 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条计量单位吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.measureId)
    await deleteUnitMeasureBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadUnitMeasureList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  unitMeasureFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateUnitMeasure(unitMeasureForm)
        ElMessage.success('编辑成功')
      } else {
        await createUnitMeasure(unitMeasureForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadUnitMeasureList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  unitMeasureFormRef.value?.resetFields()
  Object.assign(unitMeasureForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 把当前页数据导出为 CSV
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const headers = ['单位ID', '单位编码', '单位名称', '是否是主单位', '与主单位换算比例', '是否启用', '创建时间']
  const rows = tableData.value.map(item => [
    item.measureId,
    item.measureCode,
    item.measureName,
    item.primaryFlag === 'Y' ? '是' : '否',
    item.changeRate,
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
  a.download = `unitMeasure_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadUnitMeasureList()
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
