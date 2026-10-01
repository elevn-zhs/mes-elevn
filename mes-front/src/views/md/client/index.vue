<template>
  <div class="md-client">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>客户管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增客户</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="客户编码">
          <el-input v-model="queryForm.clientCode" placeholder="请输入客户编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="queryForm.clientName" placeholder="请输入客户名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-select v-model="queryForm.clientType" placeholder="请选择" clearable style="width: 130px">
            <el-option label="企业" value="ENTERPRISE" />
            <el-option label="个人" value="INDIVIDUAL" />
          </el-select>
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
        <el-table-column prop="clientId" label="ID" width="80" />
        <el-table-column prop="clientCode" label="客户编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="clientName" label="客户名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="clientNick" label="客户简称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="clientType" label="客户类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.clientType === 'ENTERPRISE'" type="primary">企业</el-tag>
            <el-tag v-if="row.clientType === 'INDIVIDUAL'" type="primary">个人</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tel" label="客户电话" min-width="140" show-overflow-tooltip />
        <el-table-column prop="contact1" label="联系人1" min-width="140" show-overflow-tooltip />
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
      width="760px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="clientFormRef"
        :model="clientForm"
        :rules="clientFormRules"
        label-width="110px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户编码" prop="clientCode">
              <el-input v-model="clientForm.clientCode" placeholder="请输入客户编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="clientName">
              <el-input v-model="clientForm.clientName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户简称" prop="clientNick">
              <el-input v-model="clientForm.clientNick" placeholder="请输入客户简称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户英文名称" prop="clientEn">
              <el-input v-model="clientForm.clientEn" placeholder="请输入客户英文名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="客户简介" prop="clientDes">
              <el-input v-model="clientForm.clientDes" type="textarea" :rows="2" placeholder="请输入客户简介" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户LOGO地址" prop="clientLogo">
              <el-input v-model="clientForm.clientLogo" placeholder="请输入客户LOGO地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户类型" prop="clientType">
              <el-select v-model="clientForm.clientType" placeholder="请选择客户类型" style="width: 100%">
                <el-option label="企业" value="ENTERPRISE" />
                <el-option label="个人" value="INDIVIDUAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="客户地址" prop="address">
              <el-input v-model="clientForm.address" type="textarea" :rows="2" placeholder="请输入客户地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户官网地址" prop="website">
              <el-input v-model="clientForm.website" placeholder="请输入客户官网地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户邮箱地址" prop="email">
              <el-input v-model="clientForm.email" placeholder="请输入客户邮箱地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户电话" prop="tel">
              <el-input v-model="clientForm.tel" placeholder="请输入客户电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人1" prop="contact1">
              <el-input v-model="clientForm.contact1" placeholder="请输入联系人1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人1-电话" prop="contact1Tel">
              <el-input v-model="clientForm.contact1Tel" placeholder="请输入联系人1-电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人1-邮箱" prop="contact1Email">
              <el-input v-model="clientForm.contact1Email" placeholder="请输入联系人1-邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人2" prop="contact2">
              <el-input v-model="clientForm.contact2" placeholder="请输入联系人2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人2-电话" prop="contact2Tel">
              <el-input v-model="clientForm.contact2Tel" placeholder="请输入联系人2-电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人2-邮箱" prop="contact2Email">
              <el-input v-model="clientForm.contact2Email" placeholder="请输入联系人2-邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input v-model="clientForm.creditCode" placeholder="请输入统一社会信用代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="enableFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="clientForm.enableFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="clientForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
  getClientByPage,
  getClientById,
  createClient,
  updateClient,
  deleteClient,
  deleteClientBatch
} from '@/api/md/client.js'

// ==================== 搜索与分页 ====================

/** 搜索筛选条件 */
const queryForm = reactive({
  clientCode: '',
  clientName: '',
  clientType: '',
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
const clientFormRef = ref(null)

const defaultForm = () => ({
  clientCode: '',
  clientName: '',
  clientNick: '',
  clientEn: '',
  clientDes: '',
  clientLogo: '',
  clientType: 'ENTERPRISE',
  address: '',
  website: '',
  email: '',
  tel: '',
  contact1: '',
  contact1Tel: '',
  contact1Email: '',
  contact2: '',
  contact2Tel: '',
  contact2Email: '',
  creditCode: '',
  enableFlag: 'Y',
  remark: '',
})

const clientForm = reactive(defaultForm())

const clientFormRules = {
  clientCode: [
    { required: true, message: '请输入客户编码', trigger: 'blur' },
    { max: 64, message: '客户编码长度不能超过 64 个字符', trigger: 'blur' },
  ],
  clientName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { max: 255, message: '客户名称长度不能超过 255 个字符', trigger: 'blur' },
  ],
  enableFlag: [
    { required: true, message: '请输入是否启用', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => isEdit.value ? '编辑客户' : '新增客户')

// ==================== 数据加载 ====================

/**
 * 分页查询客户列表
 * 把分页参数合并到查询条件里一起传给后端
 */
const loadClientList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getClientByPage(params)
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
  loadClientList()
}

/** 重置：清空条件回到第一页 */
function handleReset() {
  queryForm.clientCode = ''
  queryForm.clientName = ''
  queryForm.clientType = ''
  queryForm.enableFlag = ''
  handleSearch()
}

/** 翻页 / 改每页条数 */
function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadClientList()
}

// ==================== 新增 / 编辑 ====================

/**
 * 清空表单
 * 先 delete 掉所有旧键再赋默认值：Object.assign 只能覆盖、不能删除键，
 * 编辑过一条记录后再点新增，上一条详情里的 createBy / delFlag 之类的字段会残留在对象里一起提交
 */
function resetForm() {
  Object.keys(clientForm).forEach(k => delete clientForm[k])
  Object.assign(clientForm, defaultForm())
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
  const result = await getClientById(row.clientId)
  if (result.data) {
    Object.assign(clientForm, result.data)
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
    `确定要删除客户「${row.clientCode}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteClient(row.clientId)
    ElMessage.success('删除成功')
    loadClientList()
  }).catch(() => {})
}

/**
 * 批量删除
 * 后端提供了 /deleteBatch 接口，一次请求搞定，不用像岗位页那样并发发 N 个单条删除
 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条客户吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.clientId)
    await deleteClientBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadClientList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  clientFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateClient(clientForm)
        ElMessage.success('编辑成功')
      } else {
        await createClient(clientForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadClientList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  clientFormRef.value?.resetFields()
  Object.assign(clientForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 把当前页数据导出为 CSV
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const headers = ['客户ID', '客户编码', '客户名称', '客户简称', '客户类型', '客户电话', '联系人1', '是否启用', '创建时间']
  const rows = tableData.value.map(item => [
    item.clientId,
    item.clientCode,
    item.clientName,
    item.clientNick,
    item.clientType,
    item.tel,
    item.contact1,
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
  a.download = `client_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadClientList()
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
