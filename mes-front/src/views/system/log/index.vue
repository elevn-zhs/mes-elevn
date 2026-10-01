<template>
  <div class="system-log">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button :icon="Delete" @click="handleClear">清空日志</el-button>
          </div>
        </div>
      </template>

      <!--
        说明：日志页刻意没有「新增」和「编辑」按钮。
        后端 SysLogController 也只提供了查询和删除两个接口 —— 日志是系统自动记录的操作痕迹，
        不允许人工编造，也不允许事后篡改，从接口层面就把这条路堵死了。
      -->

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="操作模块">
          <el-input v-model="queryForm.title" placeholder="请输入操作模块" clearable />
        </el-form-item>
        <el-form-item label="操作人员">
          <el-input v-model="queryForm.operName" placeholder="请输入操作人员" clearable />
        </el-form-item>
        <el-form-item label="日志类型">
          <el-select v-model="queryForm.logType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="操作" value="1" />
            <el-option label="登录" value="2" />
            <el-option label="异常" value="3" />
            <el-option label="接口" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryForm.businessType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="其他" value="0" />
            <el-option label="新增" value="1" />
            <el-option label="修改" value="2" />
            <el-option label="删除" value="3" />
            <el-option label="导出" value="4" />
            <el-option label="导入" value="5" />
            <el-option label="授权" value="6" />
            <el-option label="强退" value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" value="0" />
            <el-option label="异常" value="1" />
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
        <el-table-column prop="logId" label="ID" width="80" />
        <el-table-column prop="title" label="操作模块" min-width="120" show-overflow-tooltip />
        <el-table-column prop="businessType" label="业务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="businessTypeMap[row.businessType]?.type || 'info'" size="small">
              {{ businessTypeMap[row.businessType]?.label || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operName" label="操作人员" width="110" />
        <el-table-column prop="operIp" label="操作IP" width="140" />
        <el-table-column prop="operLocation" label="操作地点" width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <!-- status：0 正常，1 异常 -->
            <el-tag v-if="row.status === '0'" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">异常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'cost-slow': row.costTime > 1000 }">{{ row.costTime }} ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
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

    <!-- ============ 日志详情抽屉 ============ -->
    <el-drawer v-model="detailVisible" title="日志详情" size="600px">
      <el-descriptions :column="1" border v-if="detailData">
        <el-descriptions-item label="日志ID">{{ detailData.logId }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="日志类型">
          {{ logTypeMap[detailData.logType] || detailData.logType }}
        </el-descriptions-item>
        <el-descriptions-item label="业务类型">
          {{ businessTypeMap[detailData.businessType]?.label || '其他' }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人员">
          {{ detailData.operName }}（{{ detailData.operNick }}）
        </el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detailData.deptName }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">{{ detailData.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailData.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作方法">{{ detailData.method }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detailData.operIp }}</el-descriptions-item>
        <el-descriptions-item label="操作地点">{{ detailData.operLocation }}</el-descriptions-item>
        <el-descriptions-item label="浏览器 / 系统">
          {{ detailData.browser }} / {{ detailData.os }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailData.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag v-if="detailData.status === '0'" type="success" size="small">正常</el-tag>
          <el-tag v-else type="danger" size="small">异常</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.operTime }}</el-descriptions-item>

        <!-- 请求参数和返回结果可能很长，用 <pre> 保格式，超出滚动 -->
        <el-descriptions-item label="请求参数">
          <pre class="detail-pre">{{ detailData.operParam }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果">
          <pre class="detail-pre">{{ detailData.jsonResult }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="异常信息" v-if="detailData.status === '1'">
          <pre class="detail-pre detail-error">{{ detailData.errorMsg }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete, Download, Close, View } from '@element-plus/icons-vue'

import { getLogByPage, getLogById, deleteLog } from '@/api/sys/log.js'

// ==================== 字典映射（和数据库里的注释一一对应） ====================

/** 日志类型：1操作 2登录 3异常 4接口 */
const logTypeMap = { 1: '操作', 2: '登录', 3: '异常', 4: '接口' }

/** 业务类型：0其他 1新增 2修改 3删除 4导出 5导入 6授权 7强退 */
const businessTypeMap = {
  0: { label: '其他', type: 'info' },
  1: { label: '新增', type: 'success' },
  2: { label: '修改', type: 'primary' },
  3: { label: '删除', type: 'danger' },
  4: { label: '导出', type: 'warning' },
  5: { label: '导入', type: 'warning' },
  6: { label: '授权', type: 'warning' },
  7: { label: '强退', type: 'danger' }
}

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  title: '',
  operName: '',
  logType: null,
  businessType: null,
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

// ==================== 详情抽屉 ====================

/** 抽屉是否可见 */
const detailVisible = ref(false)

/** 当前查看的日志详情 */
const detailData = ref(null)

// ==================== 数据加载 ====================

/** 分页查询日志 */
const loadLogList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getLogByPage(params)
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
  loadLogList()
}

function handleReset() {
  queryForm.title = ''
  queryForm.operName = ''
  queryForm.logType = null
  queryForm.businessType = null
  queryForm.status = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadLogList()
}

// ==================== 详情 ====================

/**
 * 查看详情
 * 列表接口通常不会把 oper_param / json_result 这种大字段全量返回（太占带宽），
 * 所以点详情时再根据 id 单独查一次完整记录
 * @param {Object} row 当前行
 */
async function handleDetail(row) {
  const result = await getLogById(row.logId)
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
    `确定要删除这条日志吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteLog(row.logId)
    ElMessage.success('删除成功')
    loadLogList()
  }).catch(() => {})
}

/** 批量删除：后端没有批量接口，用 Promise.all 并发发多个单条请求 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条日志吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.logId)
    await Promise.all(ids.map(id => deleteLog(id)))
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadLogList()
  }).catch(() => {})
}

/**
 * 清空日志
 * 目前后端的删除接口是「按 id 单条删」，没有「清空全部」的接口，
 * 所以这里做成：把当前筛选条件下查出来的日志逐条删掉。
 * 真要做成一键清空全表，需要后端补一个 /log/clear 接口（一条 DELETE 语句搞定，比前端循环快得多）。
 */
function handleClear() {
  if (!pagination.total) {
    ElMessage.info('当前没有可清理的日志')
    return
  }
  ElMessageBox.confirm(
    `确定要清空当前筛选条件下的全部 ${pagination.total} 条日志吗？此操作不可恢复。`,
    '清空确认',
    { confirmButtonText: '确定清空', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    // 分页逐批删：每页最多 100 条，删完一批再查下一批，直到查不出数据为止
    let deleted = 0
    for (let pageNum = 1; ; pageNum++) {
      const params = { ...queryForm, pageNum, pageSize: 100 }
      const result = await getLogByPage(params)
      const list = result.data.list || []
      if (!list.length) break
      await Promise.all(list.map(item => deleteLog(item.logId)))
      deleted += list.length
    }
    ElMessage.success(`已清空 ${deleted} 条日志`)
    loadLogList()
  }).catch(() => {})
}

// ==================== 导出 ====================

/** 导出当前页数据为 CSV（带 BOM 防中文乱码） */
function handleExport() {
  const headers = ['ID', '操作模块', '业务类型', '操作人员', '操作IP', '操作地点', '状态', '耗时(ms)', '操作时间']
  const rows = tableData.value.map(item => [
    item.logId,
    item.title,
    businessTypeMap[item.businessType]?.label || '其他',
    item.operName,
    item.operIp,
    item.operLocation,
    item.status === '0' ? '正常' : '异常',
    item.costTime,
    item.operTime
  ])
  const csv = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `log_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadLogList()
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

/** 超过 1 秒的请求标红，一眼看出慢接口 */
.cost-slow {
  color: #f56c6c;
  font-weight: 600;
}

.detail-pre {
  margin: 0;
  max-height: 220px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  line-height: 1.6;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}

.detail-error {
  color: #f56c6c;
  background: #fef0f0;
}
</style>
