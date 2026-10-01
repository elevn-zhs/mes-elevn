<template>
  <div class="system-user">
    <!-- 主卡片容器 -->
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>字典管理</span>
          <div class="header-actions">
            <el-button :icon="Upload" @click="handleImport">导入</el-button>
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增字典</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="字典名称">
          <el-input v-model="queryForm.dictName" placeholder="请输入字典名称" clearable />
        </el-form-item>
        <el-form-item label="字典类型">
          <el-input v-model="queryForm.dictType" placeholder="请输入字典类型" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-table-column prop="dictId" label="ID" width="80" />
        <el-table-column prop="dictName" label="字典名称" />
        <el-table-column prop="dictType" label="字典类型" >
          <template #default="scope">
            <!-- 点字典类型直接跳到「字典数据」页，并把类型带过去，进去就能看到这个类型下的数据项 -->
            <el-link type="primary" @click="handleViewData(scope.row)">{{ scope.row.dictType }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="dictSort" label="排序" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === '0'" type="success">正常</el-tag>
            <el-tag v-else type="warning">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Grid" @click="handleViewData(row)">字典数据</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ============ 分页组件 ============ -->
      <div class="pagination-wrapper">
        <el-pagination
            @change="handlerPageChange"
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <!-- ============ 新增/编辑用户弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="dictTypeFormRef"
        :model="dictTypeForm"
        :rules="dictTypeRules"
        label-width="80px"
      >
        <el-form-item label="字典名称" prop="dictName">
          <el-input
            v-model="dictTypeForm.dictName"
            placeholder="请输字典名称"
          />
        </el-form-item>
        <el-form-item  label="字典类型" prop="dictType">
          <el-input
            v-model="dictTypeForm.dictType"
            placeholder="请输入字典类型"
          />
        </el-form-item>

        <el-form-item label="显示顺序" prop="dictSort">
          <el-input-number min="1" v-model="dictTypeForm.dictSort" placeholder="请输入显示顺序" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictTypeForm.status">
            <!-- 数据库的类型是char类型，这里直接使用value='1',如果后端是Integer类型，要使用 :value='1' -->
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dictTypeForm.remark" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 导入用户弹窗 ============ -->
    <el-dialog v-model="importVisible" title="导入用户" width="460px">
      <div class="import-area">
        <el-upload
          drag
          :auto-upload="false"
          :show-file-list="true"
          accept=".xlsx,.xls,.csv"
          :on-change="handleFileChange"
        >
          <el-icon class="el-icon--upload"><Upload /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">支持 .xlsx / .xls / .csv 格式</div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImport">确定导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, computed, onMounted} from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Edit, Delete,
  Upload, Download, Close, Grid
} from '@element-plus/icons-vue'
// 导入我们api中的方法
import {deleteBatch,getDictTypeByPage,createDictType,updateDictType,deleteDictType,getDictTypeById} from "@/api/sys/dictType.js";

// 用来跳转到「字典数据」页
const router = useRouter()

// ==================== 搜索与分页 ====================

/** 搜索筛选条件：字典名称，字典类型，字典状态 */
const queryForm = reactive({
  dictName: '',
  dictType: '',
  status: null
})

/** 分页参数：当前页码 / 每页条数 */
const pagination = reactive({
  page: 1,
  size: 10,
  total:0
})

// ==================== 数据源 ====================

/**
 * 最终传给 el-table 的数据
 */
const tableData = ref([]);

// ==================== 表格多选相关 ====================

/** 当前勾选的行数据数组，由 el-table 的 selection-change 事件维护 */
const selectedRows = ref([])

/** el-table 组件引用，用于调用 clearSelection() 等内置方法 */
const tableRef = ref(null)

// ==================== 导入弹窗相关 ====================

/** 导入弹窗是否可见 */
const importVisible = ref(false)

/** 用户选择的导入文件对象（File.raw） */
const importFile = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

/** 新增/编辑弹窗是否可见 */
const dialogVisible = ref(false)

/** 当前是否为编辑模式（true = 编辑，false = 新增） */
const isEdit = ref(false)

/** 提交按钮的 loading 状态，防止重复提交 */
const submitLoading = ref(false)

/** 弹窗内 el-form 组件引用，用于触发 validate() 和 resetFields() */
const dictTypeFormRef = ref(null)

/**
 * 返回表单的默认值对象
 * 每次新增 / 重置表单时调用，避免对象引用共享
 */
const defaultForm = () => ({
  // 注意：这张表的主键叫 dictId，不是 id，写错了后端收不到主键，编辑就会变成新增
  dictId: null,
  dictName:"",
  dictType:"",
  dictSort:0,
  status:'0',
  remark:""
})

/** 弹窗表单的双向绑定数据源 */
const dictTypeForm = reactive(defaultForm())

/** 弹窗表单的校验规则 */
const dictTypeRules = {
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { min: 3, max: 20, message: '字典名称长度在3~20之间', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]+$/, message: '用户名只能包含中文', trigger: 'blur' }
  ],
  dictType: [
    { required: true, message: '请输入字典类型', trigger: 'blur' },
    { min: 3, max: 50, message: '字典类型的长度应该是3~50个英文字母', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '字典类型只能包含字母、数字和下划线', trigger: 'blur' }
  ]
}

/** 根据 isEdit 动态计算弹窗标题 */
const dialogTitle = computed(() => isEdit.value ? '编辑字典' : '新增字典')

// ==================== 表格多选回调 ====================

/**
 * 表格 selection-change 事件回调
 * @param {Array} rows 当前勾选的所有行
 */
function handleSelectionChange(rows) {
  // 将当前勾选的所有行设置到selectedRows响应式变量中；
  selectedRows.value = rows
}

/** 清空表格勾选状态（通过 el-table 内置 API） */
function clearSelection() {
  tableRef.value?.clearSelection()
}

// ==================== 新增 / 编辑 ====================

/** 点击「新增用户」按钮，打开空白表单 */
function handleAdd() {
  isEdit.value = false
  // 将绑定表单数据的对象置空
  Object.assign(dictTypeForm, defaultForm())
  // 弹窗
  dialogVisible.value = true
}

/**
 * 点击行内「编辑」按钮，回填表单数据
 * @param {Object} row 当前行数据
 */
async function handleEdit(row) {
  // isEdit修改为true，表示当前是编辑模式
  isEdit.value = true
  // 通过id查询最新的数据赋值给 dictTypeForm
  let result = await getDictTypeById(row.dictId);
  // 校验是否查询到数据了
  if(result.data){
    Object.assign(dictTypeForm,result.data);
    dialogVisible.value = true
  }else{
    ElMessage({
      type:"info",
      message:"数据有误，请刷新页面重试"
    });
  }
}

// ==================== 删除 ====================

/**
 * 删除单条数据（行内「删除」按钮）
 * @param {Object} row 当前行数据
 */
 function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除字典 "${row.dictName}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    let result = await deleteDictType(row.dictId);
    if(result.data == "1"){
      ElMessage.success('删除成功')
    }else{
      ElMessage.success('删除失败')
    }
    loadDictTypeList();
  }).catch(() => {})
}

/** 批量删除（工具栏按钮） */
function handleBatchDelete() {
  // 如果没有选中任何数据，直接退出
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个字典吗？此操作不可恢复。`,
    '批量删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    // 通过数组的方法，将id整理到ids数组中
    const ids = selectedRows.value.map(r => r.dictId)
    // 调用删除的函数进行删除操作
    let result = await deleteBatch(ids);
    // 清理标记
    clearSelection()
    ElMessage.success(`已删除 ${result.data} 条`)
    loadDictTypeList();
  }).catch(() => {})
}

// ==================== 跳转到字典数据 ====================

/**
 * 查看某个字典类型下面挂的数据项
 * 跳到「字典数据」页，并把 dictType 作为路由参数带过去，
 * 那边一进来就自动按这个类型过滤，不用再手输一遍。
 * @param {Object} row 当前行
 */
function handleViewData(row) {
  router.push({ path: '/system/dictData/' + row.dictType})
}

// ==================== 表单提交 ====================

/**
 * 弹窗「确定 / 保存」按钮的提交逻辑
 * 先做表单校验，校验通过后根据 isEdit 走新增或更新分支
 */
function handleSubmit() {
  dictTypeFormRef.value.validate(async (valid) => {
    // 表单校验未通过直接返回
    if (!valid) return
    // 开始提交
    submitLoading.value = true
    try {
      if (isEdit.value) {
        // 编辑操作
        let result = await updateDictType(dictTypeForm);
        ElMessage.success('编辑成功')
      } else {
        // 新增操作  调用新增的函数直接提交数据  dictTypeForm是reactive包裹的，所以不需要.value
        let result = await createDictType(dictTypeForm);
        // result就是后端响应的reponse，而且状态码肯定是200
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      // 刷新列表
      loadDictTypeList();
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 弹窗关闭（右上角 X 或点击遮罩）时的清理工作
 * 清除表单校验提示 + 重置表单数据
 */
function handleDialogClose() {
  dictTypeFormRef.value?.resetFields()
  Object.assign(dictTypeForm, defaultForm())
}

// ==================== 搜索区操作 ====================

/** 点击「搜索」按钮 —— 回到第一页，让 tableData 重新计算 */
function handleSearch() {
  pagination.page = 1;
  loadDictTypeList();
}

/** 点击「重置」按钮 —— 清空所有筛选条件并回到第一页 */
function handleReset() {
  queryForm.dictName = ''
  queryForm.dictType = ''
  queryForm.status = null
  handleSearch();
}

// ==================== 导入 ====================

/** 点击顶部「导入」按钮 —— 打开导入弹窗并清空上一次的文件 */
function handleImport() {
  importFile.value = null
  importVisible.value = true
}

/**
 * el-upload 文件选择/拖拽变更回调
 * @param {import('element-plus').UploadUserFile} file 上传组件封装的文件对象
 */
function handleFileChange(file) {
  importFile.value = file.raw
}

/** 导入弹窗「确定导入」按钮 —— 实际项目中此处应调用后端接口 */
function confirmImport() {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  ElMessage.success(`已导入文件：${importFile.value.name}（演示功能，实际需对接后端）`)
  importVisible.value = false
}

// ==================== 导出 ====================

/**
 * 将 tableData（当前页数据）导出为 CSV 文件
 * 前端直接生成 Blob 并触发下载，无需后端参与
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const headers = ['ID', '字典名称', '字典类型', '显示顺序', '状态', '创建时间']
  const rows = tableData.value.map(item => [
    item.dictId,
    item.dictName,
    item.dictType,
    item.dictSort,
    item.status === 1 ? '启用' : '禁用',
    item.createTime
  ])
  // CSV 格式：每个字段用双引号包裹，引号内部的双引号转义为两个双引号
  const csv = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `users_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

/**
 * 根据表单的搜索条件和分页相关参数查询字典列表
 * @returns {Promise<void>}
 */
const loadDictTypeList = async function(){
  // 搜索条件 将分页参数添加到搜索条件上
  queryForm.pageNum = pagination.page;
  queryForm.pageSize = pagination.size;
  // 调用函数查询数据
  let result = await getDictTypeByPage(queryForm);
  // 展示列表
  tableData.value = result.data.list;
  pagination.total = result.data.total;
}
/**
 * 翻页事件
 * @param newPageNum
 * @param newPageSize
 */
const handlerPageChange = function(newPageNum,newPageSize){
  pagination.page = newPageNum;
  pagination.size = newPageSize;
  loadDictTypeList();
}

onMounted(()=>{
  // 页面组件挂载完成之后，立刻加载数据
  loadDictTypeList();
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

.import-area {
  padding: 10px 0;
}
</style>