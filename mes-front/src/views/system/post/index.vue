<template>
  <div class="system-post">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>岗位管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增岗位</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="岗位名称">
          <el-input v-model="queryForm.postName" placeholder="请输入岗位名称" clearable />
        </el-form-item>
        <el-form-item label="岗位编码">
          <el-input v-model="queryForm.postCode" placeholder="请输入岗位编码" clearable />
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
        <el-table-column prop="postId" label="ID" width="80" />
        <el-table-column prop="postCode" label="岗位编码" width="140" />
        <el-table-column prop="postName" label="岗位名称" min-width="140" />
        <el-table-column prop="postSort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <!-- status 是 char(1)：'0' 正常，'1' 停用 -->
            <el-tag v-if="row.status === '0'" type="success">正常</el-tag>
            <el-tag v-else type="warning">停用</el-tag>
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

    <!-- ============ 新增/编辑岗位弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="postRules"
        label-width="90px"
      >
        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="postForm.postName" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="岗位编码" prop="postCode">
          <el-input v-model="postForm.postCode" placeholder="请输入岗位编码" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="postSort">
          <el-input-number v-model="postForm.postSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="postForm.status">
            <!-- 数据库 status 是 char 类型，用 value="0"；后端若是 Integer 要写成 :value="0" -->
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="postForm.remark" type="textarea" :rows="2" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Download, Close } from '@element-plus/icons-vue'

import {
  getPostByPage,
  getPostById,
  createPost,
  updatePost,
  deletePost
} from '@/api/sys/post.js'

// ==================== 搜索与分页 ====================

/** 搜索筛选条件：岗位名称 / 岗位编码 / 状态 */
const queryForm = reactive({
  postName: '',
  postCode: '',
  status: null
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
const postFormRef = ref(null)

const defaultForm = () => ({
  postId: null,
  postCode: '',
  postName: '',
  postSort: 0,
  status: '0',
  remark: ''
})

const postForm = reactive(defaultForm())

const postRules = {
  postName: [
    { required: true, message: '请输入岗位名称', trigger: 'blur' },
    { min: 2, max: 20, message: '岗位名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  postCode: [
    { required: true, message: '请输入岗位编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '岗位编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  postSort: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑岗位' : '新增岗位')

// ==================== 数据加载 ====================

/**
 * 分页查询岗位列表
 * 把分页参数合并到查询条件里一起传给后端
 */
const loadPostList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getPostByPage(params)
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
  loadPostList()
}

/** 重置：清空条件回到第一页 */
function handleReset() {
  queryForm.postName = ''
  queryForm.postCode = ''
  queryForm.status = null
  handleSearch()
}

/** 翻页 / 改每页条数 */
function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadPostList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(postForm, defaultForm())
  dialogVisible.value = true
}

/**
 * 编辑：按 id 查最新数据再回填
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getPostById(row.postId)
  if (result.data) {
    Object.assign(postForm, result.data)
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 删除 ====================

/**
 * 删除单条
 * @param {Object} row 当前行
 */
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除岗位「${row.postName}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deletePost(row.postId)
    ElMessage.success('删除成功')
    loadPostList()
  }).catch(() => {})
}

/**
 * 批量删除
 * 后端没有提供批量删除接口，所以用 Promise.all 并发发多个单条删除请求。
 * 岗位数据量小，这样做完全够用；真到几百条的场景，正确做法是让后端加一个 /deleteBatch 接口。
 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个岗位吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.postId)
    await Promise.all(ids.map(id => deletePost(id)))
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadPostList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  postFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updatePost(postForm)
        ElMessage.success('编辑成功')
      } else {
        await createPost(postForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadPostList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  postFormRef.value?.resetFields()
  Object.assign(postForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 把当前页数据导出为 CSV
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const headers = ['ID', '岗位编码', '岗位名称', '排序', '状态', '创建时间']
  const rows = tableData.value.map(item => [
    item.postId,
    item.postCode,
    item.postName,
    item.postSort,
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
  a.download = `post_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadPostList()
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
