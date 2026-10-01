<template>
  <div class="md-vendor">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>供应商管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增供应商</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="供应商编码">
          <el-input v-model="queryForm.vendorCode" placeholder="请输入供应商编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="供应商名称">
          <el-input v-model="queryForm.vendorName" placeholder="请输入供应商名称" clearable style="width: 180px" />
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
        <el-table-column prop="vendorId" label="ID" width="80" />
        <el-table-column prop="vendorCode" label="供应商编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="vendorName" label="供应商名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="vendorNick" label="供应商简称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="vendorLevel" label="供应商等级" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.vendorLevel === 'A'" type="primary">A级</el-tag>
            <el-tag v-if="row.vendorLevel === 'B'" type="primary">B级</el-tag>
            <el-tag v-if="row.vendorLevel === 'C'" type="primary">C级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vendorScore" label="供应商评分" width="100" align="right" />
        <el-table-column prop="tel" label="供应商电话" min-width="140" show-overflow-tooltip />
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
        ref="vendorFormRef"
        :model="vendorForm"
        :rules="vendorFormRules"
        label-width="110px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商编码" prop="vendorCode">
              <el-input v-model="vendorForm.vendorCode" placeholder="请输入供应商编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="vendorName">
              <el-input v-model="vendorForm.vendorName" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商简称" prop="vendorNick">
              <el-input v-model="vendorForm.vendorNick" placeholder="请输入供应商简称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商英文名称" prop="vendorEn">
              <el-input v-model="vendorForm.vendorEn" placeholder="请输入供应商英文名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="供应商简介" prop="vendorDes">
              <el-input v-model="vendorForm.vendorDes" type="textarea" :rows="2" placeholder="请输入供应商简介" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商LOGO地址" prop="vendorLogo">
              <el-input v-model="vendorForm.vendorLogo" placeholder="请输入供应商LOGO地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商等级" prop="vendorLevel">
              <el-select v-model="vendorForm.vendorLevel" placeholder="请选择供应商等级" style="width: 100%">
                <el-option label="A级" value="A" />
                <el-option label="B级" value="B" />
                <el-option label="C级" value="C" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商评分" prop="vendorScore">
              <el-input-number v-model="vendorForm.vendorScore" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="供应商地址" prop="address">
              <el-input v-model="vendorForm.address" type="textarea" :rows="2" placeholder="请输入供应商地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商官网地址" prop="website">
              <el-input v-model="vendorForm.website" placeholder="请输入供应商官网地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商邮箱地址" prop="email">
              <el-input v-model="vendorForm.email" placeholder="请输入供应商邮箱地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商电话" prop="tel">
              <el-input v-model="vendorForm.tel" placeholder="请输入供应商电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人1" prop="contact1">
              <el-input v-model="vendorForm.contact1" placeholder="请输入联系人1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人1-电话" prop="contact1Tel">
              <el-input v-model="vendorForm.contact1Tel" placeholder="请输入联系人1-电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人1-邮箱" prop="contact1Email">
              <el-input v-model="vendorForm.contact1Email" placeholder="请输入联系人1-邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人2" prop="contact2">
              <el-input v-model="vendorForm.contact2" placeholder="请输入联系人2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人2-电话" prop="contact2Tel">
              <el-input v-model="vendorForm.contact2Tel" placeholder="请输入联系人2-电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人2-邮箱" prop="contact2Email">
              <el-input v-model="vendorForm.contact2Email" placeholder="请输入联系人2-邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input v-model="vendorForm.creditCode" placeholder="请输入统一社会信用代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="enableFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="vendorForm.enableFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="vendorForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
  getVendorByPage,
  getVendorById,
  createVendor,
  updateVendor,
  deleteVendor,
  deleteVendorBatch
} from '@/api/md/vendor.js'

// ==================== 搜索与分页 ====================

/** 搜索筛选条件 */
const queryForm = reactive({
  vendorCode: '',
  vendorName: '',
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
const vendorFormRef = ref(null)

const defaultForm = () => ({
  vendorCode: '',
  vendorName: '',
  vendorNick: '',
  vendorEn: '',
  vendorDes: '',
  vendorLogo: '',
  vendorLevel: 'A',
  vendorScore: 0,
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

const vendorForm = reactive(defaultForm())

const vendorFormRules = {
  vendorCode: [
    { required: true, message: '请输入供应商编码', trigger: 'blur' },
    { max: 64, message: '供应商编码长度不能超过 64 个字符', trigger: 'blur' },
  ],
  vendorName: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { max: 255, message: '供应商名称长度不能超过 255 个字符', trigger: 'blur' },
  ],
  enableFlag: [
    { required: true, message: '请输入是否启用', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => isEdit.value ? '编辑供应商' : '新增供应商')

// ==================== 数据加载 ====================

/**
 * 分页查询供应商列表
 * 把分页参数合并到查询条件里一起传给后端
 */
const loadVendorList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getVendorByPage(params)
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
  loadVendorList()
}

/** 重置：清空条件回到第一页 */
function handleReset() {
  queryForm.vendorCode = ''
  queryForm.vendorName = ''
  queryForm.enableFlag = ''
  handleSearch()
}

/** 翻页 / 改每页条数 */
function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadVendorList()
}

// ==================== 新增 / 编辑 ====================

/**
 * 清空表单
 * 先 delete 掉所有旧键再赋默认值：Object.assign 只能覆盖、不能删除键，
 * 编辑过一条记录后再点新增，上一条详情里的 createBy / delFlag 之类的字段会残留在对象里一起提交
 */
function resetForm() {
  Object.keys(vendorForm).forEach(k => delete vendorForm[k])
  Object.assign(vendorForm, defaultForm())
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
  const result = await getVendorById(row.vendorId)
  if (result.data) {
    Object.assign(vendorForm, result.data)
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
    `确定要删除供应商「${row.vendorCode}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteVendor(row.vendorId)
    ElMessage.success('删除成功')
    loadVendorList()
  }).catch(() => {})
}

/**
 * 批量删除
 * 后端提供了 /deleteBatch 接口，一次请求搞定，不用像岗位页那样并发发 N 个单条删除
 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条供应商吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.vendorId)
    await deleteVendorBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadVendorList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  vendorFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateVendor(vendorForm)
        ElMessage.success('编辑成功')
      } else {
        await createVendor(vendorForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadVendorList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  vendorFormRef.value?.resetFields()
  Object.assign(vendorForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 把当前页数据导出为 CSV
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const headers = ['供应商ID', '供应商编码', '供应商名称', '供应商简称', '供应商等级', '供应商评分', '供应商电话', '联系人1', '是否启用', '创建时间']
  const rows = tableData.value.map(item => [
    item.vendorId,
    item.vendorCode,
    item.vendorName,
    item.vendorNick,
    item.vendorLevel,
    item.vendorScore,
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
  a.download = `vendor_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadVendorList()
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
