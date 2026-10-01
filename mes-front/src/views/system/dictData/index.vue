<template>
  <div class="system-dict-data">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>
            <el-button @click="handlerBack">返回</el-button>字典数据
            <!-- 从字典管理页点字典类型跳过来时，这里会带上具体的类型名，让人知道当前在看哪一类 -->
            <el-tag v-if="queryForm.dictType" type="info" style="margin-left: 8px">
              {{ currentDictType.dictType }} : {{currentDictType.dictName}}
            </el-tag>
          </span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增数据</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="字典标签">
          <el-input v-model="queryForm.dictLabel" placeholder="请输入字典标签" clearable />
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
        <el-table-column prop="dictCode" label="ID" width="80" />
        <el-table-column prop="dictType" label="字典类型" width="160" />
        <el-table-column prop="dictLabel" label="字典标签" min-width="120">
          <template #default="{ row }">
            <!--
              listClass 控制标签配色（primary / success / info / warning / danger），
              这是若依那套约定：字典数据自己带样式，页面上直接就能用
            -->
            <el-tag :type="row.listClass || 'primary'" :class="row.cssClass">
              {{ row.dictLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dictValue" label="字典键值" width="120" />
        <el-table-column prop="dictSort" label="排序" width="80" align="center" />
        <el-table-column prop="isDefault" label="是否默认" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 'Y'" type="success">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === '0'" type="success">正常</el-tag>
            <el-tag v-else type="warning">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
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

    <!-- ============ 新增/编辑字典数据弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="dictDataFormRef"
        :model="dictDataForm"
        :rules="dictDataRules"
        label-width="90px"
      >
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="dictDataForm.dictType" placeholder="如 sys_user_sex" />
        </el-form-item>
        <el-form-item label="数据标签" prop="dictLabel">
          <el-input v-model="dictDataForm.dictLabel" placeholder="页面上显示的名称，如 男" />
        </el-form-item>
        <el-form-item label="数据键值" prop="dictValue">
          <el-input v-model="dictDataForm.dictValue" placeholder="存进数据库的值，如 0" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="dictSort">
          <el-input-number v-model="dictDataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="回显样式" prop="listClass">
          <el-select v-model="dictDataForm.listClass" placeholder="标签配色" clearable style="width: 100%">
            <el-option label="默认 primary" value="primary" />
            <el-option label="成功 success" value="success" />
            <el-option label="信息 info" value="info" />
            <el-option label="警告 warning" value="warning" />
            <el-option label="危险 danger" value="danger" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否默认" prop="isDefault">
          <el-radio-group v-model="dictDataForm.isDefault">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictDataForm.status">
            <!-- status 是 char 类型，用 value="0"；后端若是 Integer 要写成 :value="0" -->
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dictDataForm.remark" type="textarea" :rows="2" />
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
import { useRoute,useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Download, Close } from '@element-plus/icons-vue'

import {
  getDictDataByPage,
  getDictDataById,
  createDictData,
  updateDictData,
  deleteDictData
} from '@/api/sys/dictData.js'

import {getDictTypeByType} from "@/api/sys/dictType.js";

// 用来接住从「字典管理」页跳过来时带的 ?dictType=xxx 参数
const route = useRoute()
const router = useRouter();
const currentDictType = ref({})


// ==================== 搜索与分页 ====================

/**
 * 搜索筛选条件
 * dictType 的初始值取自路由参数 —— 从字典管理页点某个类型进来时会自动过滤
 */
const queryForm = reactive({
  dictLabel: '',
  dictType: route.params.dictType || '',
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
const dictDataFormRef = ref(null)

const defaultForm = () => ({
  dictCode: null,
  dictType: queryForm.dictType || '',
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  listClass: 'primary',
  isDefault: 'N',
  status: '0',
  remark: ''
})

const dictDataForm = reactive(defaultForm())

const dictDataRules = {
  dictType: [
    { required: true, message: '请输入字典类型', trigger: 'blur' }
  ],
  dictLabel: [
    { required: true, message: '请输入数据标签', trigger: 'blur' }
  ],
  dictValue: [
    { required: true, message: '请输入数据键值', trigger: 'blur' }
  ],
  dictSort: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑字典数据' : '新增字典数据')

// ==================== 数据加载 ====================

/** 分页查询字典数据 */
const loadDictDataList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getDictDataByPage(params)
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
  loadDictDataList()
}

function handleReset() {
  queryForm.dictLabel = ''
  queryForm.status = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadDictDataList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(dictDataForm, defaultForm())
  dialogVisible.value = true
}

/**
 * 编辑：按 dictCode 查最新数据再回填
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getDictDataById(row.dictCode)
  if (result.data) {
    Object.assign(dictDataForm, result.data)
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 删除 ====================

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除字典数据「${row.dictLabel}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteDictData(row.dictCode)
    ElMessage.success('删除成功')
    loadDictDataList()
  }).catch(() => {})
}

/**
 * 批量删除
 * 后端没有批量接口，用 Promise.all 并发发多个单条删除请求
 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条字典数据吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.dictCode)
    await Promise.all(ids.map(id => deleteDictData(id)))
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadDictDataList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  dictDataFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateDictData(dictDataForm)
        ElMessage.success('编辑成功')
      } else {
        await createDictData(dictDataForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadDictDataList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  dictDataFormRef.value?.resetFields()
  Object.assign(dictDataForm, defaultForm())
}

// ==================== 导出 ====================

/** 导出当前页数据为 CSV（带 BOM 防中文乱码） */
function handleExport() {
  const headers = ['ID', '字典类型', '字典标签', '字典键值', '排序', '是否默认', '状态', '创建时间']
  const rows = tableData.value.map(item => [
    item.dictCode,
    item.dictType,
    item.dictLabel,
    item.dictValue,
    item.dictSort,
    item.isDefault === 'Y' ? '是' : '否',
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
  a.download = `dictData_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

const loadDictTypeByType = async function(){
  let response = await getDictTypeByType(route.params.dictType);
  currentDictType.value = response.data;
}

// 回退按钮事件
const handlerBack = function(){
  router.back();
}

onMounted(() => {
  loadDictTypeByType();
  loadDictDataList()
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
