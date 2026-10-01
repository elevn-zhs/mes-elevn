<template>
  <div class="system-role">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>编码规则管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增编码规则</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="queryForm.ruleTitle" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
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
        <el-table-column prop="ruleId" label="ID" width="80" />
        <el-table-column prop="ruleTitle" label="标题" min-width="120" />
        <el-table-column prop="ruleCode" label="编码" min-width="120" />
        <el-table-column prop="ruleDesc" label="说明" min-width="140" />
        <el-table-column prop="prefix" label="前缀" width="100" align="center" />
        <el-table-column prop="dateStr" label="日期" width="100" align="center" />
        <el-table-column prop="serialNumber" label="当前序列化" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <!-- status 是 char(1)：'1' 正常，'2' 停用 -->
            <el-tag v-if="row.status == '1'" type="success">正常</el-tag>
            <el-tag v-else type="warning">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
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

    <!-- ============ 新增/编辑编码规则弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="roleRules"
        label-width="90px"
      >
        <el-form-item label="标题" prop="ruleTitle">
          <el-input v-model="ruleForm.ruleTitle" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="编码" prop="ruleCode">
          <el-input v-model="ruleForm.ruleCode" placeholder="请输入规则编码" />
        </el-form-item>
        <el-form-item label="说明" prop="roleKey">
          <el-input type="textarea" v-model="ruleForm.ruleDesc" placeholder="编码说明" />
        </el-form-item>
        <el-form-item label="前缀" prop="prefix">
          <el-input v-model="ruleForm.prefix"  />
        </el-form-item>
        <el-form-item label="启用日期" prop="prefix">
          <el-radio-group v-model="ruleForm.dateStr">
            <el-radio :value="1" label="启用"/>
            <el-radio :value="2" label="不启用"/>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="当前序列"  prop="serialNumber">
          <el-input-number min="1" v-model="ruleForm.serialNumber"  />
        </el-form-item>
        <el-form-item label="序列长度"  prop="numberLength">
          <el-input-number min="1" v-model="ruleForm.numberLength"  />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="ruleForm.status">
            <!-- status 是 char 类型，用 value="0"；后端若是 Integer 要写成 :value="0" -->
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="2">停用</el-radio>
          </el-radio-group>
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
import { reactive, ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Download, Close, Menu } from '@element-plus/icons-vue'
import {getCodingRuleById,getCodingRuleByPage,updateCodingRule,createCodingRule} from "@/api/sys/codingRule.js";


// ==================== 搜索与分页 ====================

const queryForm = reactive({
  ruleTitle: '',
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
const ruleFormRef = ref(null)

const defaultForm = () => ({
  ruleId: null,
  ruleTitle: '',
  ruleCode:'',
  ruleDesc: '',
  prefix: '',
  dateStr: 1,
  serialNumber:1,
  numberLength:6,
  status: 0,
})

const ruleForm = reactive(defaultForm())

const roleRules = {
  ruleTitle: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 20, message: '标题长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑编码规则' : '新增编码规则')

// ==================== 分配权限弹窗相关 ====================


/** 确定按钮 loading */
const menuSubmitLoading = ref(false)




// ==================== 数据加载 ====================

/** 分页查询编码规则列表 */
const loadRuleList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getCodingRuleByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

/** 加载菜单树 */
const loadMenuTree = async function () {
  const result = await getMenuTree({})
  menuTree.value = result.data || []
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
  loadRuleList()
}

function handleReset() {
  queryForm.ruleTitle = ''
  queryForm.roleKey = ''
  queryForm.status = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadRuleList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(ruleForm, defaultForm())
  dialogVisible.value = true
}

/**
 * 编辑：按 roleId 查最新数据再回填
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getCodingRuleById(row.ruleId)
  if (result.data) {
    Object.assign(ruleForm, result.data)
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}



// ==================== 删除 ====================

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除编码规则「${row.ruleTitle}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteCodingRule(row.ruleId)
    ElMessage.success('删除成功')
    loadRuleList()
  }).catch(() => {})
}

/** 批量删除：后端没有批量接口，用 Promise.all 并发发多个单条请求 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个编码规则吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.roleId)
    await Promise.all(ids.map(id => deleteRole(id)))
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadRuleList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  ruleFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateCodingRule(ruleForm)
        ElMessage.success('编辑成功')
      } else {
        await createCodingRule(ruleForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadRuleList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  ruleFormRef.value?.resetFields()
  Object.assign(ruleForm, defaultForm())
}

// ==================== 导出 ====================

/** 导出当前页数据为 CSV（带 BOM 防中文乱码） */
function handleExport() {
  const headers = ['ID', '标题', '权限字符', '显示顺序', '数据范围', '状态', '创建时间']
  const rows = tableData.value.map(item => [
    item.roleId,
    item.ruleTitle,
    item.roleKey,
    item.roleSort,
    dataScopeMap[item.dataScope] || '全部',
    item.status === '0' ? '正常' : '停用',
    item.createTime
  ])
  const csv = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `role_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadRuleList()
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

.menu-tree {
  max-height: 420px;
  overflow: auto;
}
</style>
