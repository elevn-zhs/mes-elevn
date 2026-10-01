<template>
  <div class="md-item">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>物料产品管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增物料产品</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="产品物料编码">
          <el-input v-model="queryForm.itemCode" placeholder="请输入产品物料编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品物料名称">
          <el-input v-model="queryForm.itemName" placeholder="请输入产品物料名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品物料标识">
          <el-select v-model="queryForm.itemOrProduct" placeholder="请选择" clearable style="width: 130px">
            <el-option label="物料" value="ITEM" />
            <el-option label="产品" value="PRODUCT" />
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
        <el-table-column prop="itemId" label="ID" width="80" />
        <el-table-column prop="itemCode" label="产品物料编码" min-width="140" show-overflow-tooltip >
          <template #default="scope">
            <el-link @click="handleToDetail(scope.row.itemId)" type="primary">{{scope.row.itemCode}}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="产品物料名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格型号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="unitOfMeasure" label="单位编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="itemTypeName" label="物料类型名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="itemOrProduct" label="产品物料标识" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.itemOrProduct === 'ITEM'" type="primary">物料</el-tag>
            <el-tag v-if="row.itemOrProduct === 'PRODUCT'" type="primary">产品</el-tag>
          </template>
        </el-table-column>
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
      width="860px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="itemFormRef"
        :model="itemForm"
        :rules="itemFormRules"
        label-width="110px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品物料编码" prop="itemCode">
              <el-row>
                <el-col :span="16"><el-input v-model="itemForm.itemCode" placeholder="请输入产品物料编码" /></el-col>
                <el-col :span="8"><el-switch @change="handleAutoCode" v-model="isAuoCode"/>自动生成</el-col>
              </el-row>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品物料名称" prop="itemName">
              <el-input v-model="itemForm.itemName" placeholder="请输入产品物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号" prop="specification">
              <el-input v-model="itemForm.specification" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unitOfMeasure">
              <el-select v-model="itemForm.unitOfMeasure" placeholder="请选择单位编码">
                  <el-option v-for="unit in unitList" :value="unit.measureCode" :label="unit.measureName"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品物料标识" prop="itemOrProduct">
              <el-select @change="handleItemOrProductChange" v-model="itemForm.itemOrProduct" placeholder="请选择产品物料标识" style="width: 100%">
                <el-option label="物料" value="ITEM" />
                <el-option label="产品" value="PRODUCT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料分类" prop="itemTypeId">
              <!-- 选完自动带出 itemTypeCode / itemTypeName，不用手填 -->
              <el-tree-select
                  v-model="itemForm.itemTypeId"
                  :data="itemTypeTree"
                  :render-after-expand="false"
                  filterable clearable placeholder="请选择物料产品分类" style="width: 100%"
                  @change="handleRefChange"
                  :props="{children: 'children',label:'itemTypeName',value:'itemTypeId'}"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料类型编码" prop="itemTypeCode">
              <el-input v-model="itemForm.itemTypeCode" placeholder="请输入物料类型编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="enableFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="itemForm.enableFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="设置安全库存" prop="safeStockFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="itemForm.safeStockFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col v-show="itemForm.safeStockFlag=='Y'" :span="12">
            <el-form-item label="最低库存量" prop="minStock">
              <el-input-number v-model="itemForm.minStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col v-show="itemForm.safeStockFlag=='Y'" :span="12">
            <el-form-item label="最大库存量" prop="maxStock">
              <el-input-number v-model="itemForm.maxStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高价值物资" prop="highValue">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="itemForm.highValue" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次管理" prop="batchFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="itemForm.batchFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="itemForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import {useRouter} from "vue-router";
import {
  getItemByPage,
  getItemById,
  createItem,
  updateItem,
  deleteItem,
  deleteItemBatch
} from '@/api/md/item.js'

import {getItemTypeByPage, getItemTypeForTreeByType} from '@/api/md/itemType.js'
import {autoCode} from "@/api/sys/codingRule.js";
import {getUnitMeasureByPage} from '@/api/md/unitMeasure.js';



const router = useRouter();
const isAuoCode = ref(false)

// ==================== 搜索与分页 ====================

/** 搜索筛选条件 */
const queryForm = reactive({
  itemCode: '',
  itemName: '',
  itemOrProduct: '',
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

/** 物料产品分类下拉选项（一次拉全量，主数据量不会太大） */
const refOptions = ref([])

/** 当前勾选的行 */
const selectedRows = ref([])

/** el-table 组件引用 */
const tableRef = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const itemFormRef = ref(null)

const defaultForm = () => ({
  itemCode: '',
  itemName: '',
  specification: '',
  unitOfMeasure: '',
  itemOrProduct: '',
  itemTypeId: null,
  itemTypeCode: '',
  enableFlag: 'Y',
  safeStockFlag: 'Y',
  minStock: 0,
  maxStock: 0,
  highValue: 'Y',
  batchFlag: 'Y',
  remark: '',
})

const itemForm = reactive(defaultForm())

const itemFormRules = {
  itemCode: [
    { required: true, message: '请输入产品物料编码', trigger: 'blur' },
    { max: 64, message: '产品物料编码长度不能超过 64 个字符', trigger: 'blur' },
  ],
  itemName: [
    { required: true, message: '请输入产品物料名称', trigger: 'blur' },
    { max: 255, message: '产品物料名称长度不能超过 255 个字符', trigger: 'blur' },
  ],
  unitOfMeasure: [
    { required: true, message: '请输入单位编码', trigger: 'blur' },
  ],
  itemOrProduct: [
    { required: true, message: '请选择产品物料标识', trigger: 'change' },
  ],
  enableFlag: [
    { required: true, message: '请输入是否启用', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => isEdit.value ? '编辑物料产品' : '新增物料产品')
//==========跳转到物料详情 ============
const handleToDetail = function(itemId){
  router.push({path:"/md/itemDetail/" + itemId})
}

//===========自动生成编号================
const handleAutoCode = async function(){
  if(isAuoCode.value){
    let response = await autoCode("mt_code");
    itemForm.itemCode = response.msg;
  }
}
// ==================== 数据加载 ====================

/**
 * 分页查询物料产品列表
 * 把分页参数合并到查询条件里一起传给后端
 */
const loadItemList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getItemByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

/**
 * 加载物料产品分类下拉选项
 */
async function loadRefOptions() {
  const result = await getItemTypeByPage({ pageNum: 1, pageSize: 1000 })
  refOptions.value = result.data.list || []
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
  loadItemList()
}

/** 重置：清空条件回到第一页 */
function handleReset() {
  queryForm.itemCode = ''
  queryForm.itemName = ''
  queryForm.itemOrProduct = ''
  queryForm.enableFlag = ''
  handleSearch()
}

/** 翻页 / 改每页条数 */
function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadItemList()
}

// ==================== 新增 / 编辑 ====================

/**
 * 清空表单
 * 先 delete 掉所有旧键再赋默认值：Object.assign 只能覆盖、不能删除键，
 * 编辑过一条记录后再点新增，上一条详情里的 createBy / delFlag 之类的字段会残留在对象里一起提交
 */
function resetForm() {
  Object.keys(itemForm).forEach(k => delete itemForm[k])
  Object.assign(itemForm, defaultForm())
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  loadUnitList();
  dialogVisible.value = true
}

/**
 * 选中物料产品分类后，自动把冗余字段（itemTypeCode、itemTypeName）带出来
 * 这些字段在数据库里是冗余存储，专门用来避免列表查询时频繁 join
 */
function handleRefChange(id,e) {
  console.log(id,e)
  const hit = refOptions.value.find(r => r.itemTypeId === id)
  if (!hit) return
  itemForm.itemTypeCode = hit.itemTypeCode
  itemForm.itemTypeName = hit.itemTypeName
}

//==========单位相关=======
const unitList = ref([]);
const loadUnitList = async function(){
    if(unitList.value.length == 0){
      let response = await getUnitMeasureByPage({pageNum:1,pageSize:10000});
      unitList.value = response.data.list;
    }
}
// ========物料产品分类========
// 物料类型选择改变事件
const handleItemOrProductChange = async function(){
  itemForm.itemTypeId = '';
  let response = await getItemTypeForTreeByType(itemForm.itemOrProduct);
  itemTypeTree.value = response.data;
}
// 缓存树结构的物料分类
const itemTypeTree = ref([]);
/**
 * 编辑：按 id 查最新数据再回填，避免用列表里的旧数据
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getItemById(row.itemId)
  if (result.data) {
    Object.assign(itemForm, result.data)
    loadUnitList();
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
    `确定要删除物料产品「${row.itemCode}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteItem(row.itemId)
    ElMessage.success('删除成功')
    loadItemList()
  }).catch(() => {})
}

/**
 * 批量删除
 * 后端提供了 /deleteBatch 接口，一次请求搞定，不用像岗位页那样并发发 N 个单条删除
 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条物料产品吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.itemId)
    await deleteItemBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadItemList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  itemFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateItem(itemForm)
        ElMessage.success('编辑成功')
      } else {
        await createItem(itemForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadItemList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  itemFormRef.value?.resetFields()
  Object.assign(itemForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 把当前页数据导出为 CSV
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const headers = ['产品物料ID', '产品物料编码', '产品物料名称', '规格型号', '单位编码', '物料类型名称', '产品物料标识', '是否启用', '创建时间']
  const rows = tableData.value.map(item => [
    item.itemId,
    item.itemCode,
    item.itemName,
    item.specification,
    item.unitOfMeasure,
    item.itemTypeName,
    item.itemOrProduct,
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
  a.download = `item_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadRefOptions()
  loadItemList()
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
