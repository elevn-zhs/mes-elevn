<template>
  <div class="system-route">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>工艺路线</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增路线</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="路线编码">
          <el-input v-model="queryForm.routeCode" placeholder="请输入路线编码" clearable />
        </el-form-item>
        <el-form-item label="路线名称">
          <el-input v-model="queryForm.routeName" placeholder="请输入路线名称" clearable />
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
        <el-table-column prop="routeId" label="ID" width="80" />
        <el-table-column prop="routeCode" label="路线编码" min-width="130" >
          <template #default="scope">
            <el-link @click="handleDetail(scope.row)" type="primary">{{scope.row.routeCode}}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="routeName" label="路线名称" min-width="180" />
        <el-table-column prop="routeDesc" label="路线描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="enableFlag" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
              {{ row.enableFlag === 'Y' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
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

    <!-- ============ 新增/编辑路线弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="routeFormRef"
        :model="routeForm"
        :rules="routeRules"
        label-width="90px"
      >
        <el-form-item label="路线编码" prop="routeCode">
          <el-input v-model="routeForm.routeCode" placeholder="如 RT-SMT-01" />
        </el-form-item>
        <el-form-item label="路线名称" prop="routeName">
          <el-input v-model="routeForm.routeName" placeholder="如 智能控制柜整机组装线" />
        </el-form-item>
        <el-form-item label="路线描述" prop="routeDesc">
          <el-input v-model="routeForm.routeDesc" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="enableFlag">
          <el-radio-group v-model="routeForm.enableFlag">
            <el-radio value="Y">启用</el-radio>
            <el-radio value="N">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="routeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
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
import { Plus, Search, Refresh, Edit, Delete, Close, View } from '@element-plus/icons-vue'

import {
  getRouteByPage,
  getRouteById,
  createRoute,
  updateRoute,
  deleteRoute,
  deleteRouteBatch
} from '@/api/pro/route.js'

const router = useRouter()

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  routeCode: '',
  routeName: '',
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

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const routeFormRef = ref(null)

const defaultForm = () => ({
  routeId: null,
  routeCode: '',
  routeName: '',
  routeDesc: '',
  enableFlag: 'Y',
  remark: ''
})

const routeForm = reactive(defaultForm())

const routeRules = {
  routeCode: [
    { required: true, message: '请输入路线编码', trigger: 'blur' },
    { max: 64, message: '路线编码长度不能超过 64 个字符', trigger: 'blur' }
  ],
  routeName: [
    { required: true, message: '请输入路线名称', trigger: 'blur' },
    { max: 255, message: '路线名称长度不能超过 255 个字符', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑工艺路线' : '新增工艺路线')

// ==================== 数据加载 ====================

/** 分页查询路线列表 */
const loadRouteList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getRouteByPage(params)
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
  loadRouteList()
}

function handleReset() {
  queryForm.routeCode = ''
  queryForm.routeName = ''
  queryForm.enableFlag = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadRouteList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(routeForm, defaultForm())
  dialogVisible.value = true
}

async function handleEdit(row) {
  const result = await getRouteById(row.routeId)
  if (result.data) {
    Object.assign(routeForm, result.data)
    isEdit.value = true
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

/**
 * 跳转详情页：路线工序明细在详情页 Tab 里整表编辑、整批保存
 * @param {Object} row 当前行
 */
function handleDetail(row) {
  router.push('/pro/routeDetail/' + row.routeId)
}

// ==================== 删除 ====================

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除路线「${row.routeName}」吗？若已被产品制程引用，后端会拒绝删除。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteRoute(row.routeId)
    ElMessage.success('删除成功')
    loadRouteList()
  }).catch(() => {})
}

function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条路线吗？`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.routeId)
    await deleteRouteBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadRouteList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  routeFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateRoute(routeForm)
        ElMessage.success('编辑成功')
      } else {
        await createRoute(routeForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadRouteList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  routeFormRef.value?.resetFields()
  Object.assign(routeForm, defaultForm())
}

onMounted(() => {
  loadRouteList()
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
