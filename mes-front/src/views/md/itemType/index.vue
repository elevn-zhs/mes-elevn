<template>
  <div class="md-itemType">
    <el-card shadow="never">

      <template #header>
        <div class="card-header">
          <span>物料产品分类管理</span>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="loadItemTypeTree">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增物料产品分类</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="产品物料类型编码">
          <el-input v-model="queryForm.itemTypeCode" placeholder="请输入产品物料类型编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品物料类型名称">
          <el-input v-model="queryForm.itemTypeName" placeholder="请输入产品物料类型名称" clearable style="width: 180px" />
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
          <el-button type="primary" :icon="Search" @click="loadItemTypeTree">搜索</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- ============ 树形表格 ============ -->
      <el-table
        ref="tableRef"
        :data="treeData"
        row-key="itemTypeId"
        border
        stripe
        lazy
        :tree-props="{children:'children',hasChildren:'hasChildren'}"
        :load="loadTreeNode"
      >
        <el-table-column prop="itemTypeCode" label="产品物料类型编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="itemTypeName" label="产品物料类型名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="itemOrProduct" label="产品物料标识" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.itemOrProduct === 'ITEM'" type="primary">物料</el-tag>
            <el-tag v-if="row.itemOrProduct === 'PRODUCT'" type="primary">产品</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排列顺序" width="100" align="right" />
        <el-table-column prop="enableFlag" label="是否启用" width="100" align="center">
          <template #default="{ row }">
            <!-- enableFlag 是 char(1)：'Y' 是，'N' 否，不能直接拿布尔判断 -->
            <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
              {{ row.enableFlag === 'Y' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Plus" @click="handleAddChild(row)">新增下级</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ============ 新增/编辑弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="itemTypeFormRef"
        :model="itemTypeForm"
        :rules="itemTypeFormRules"
        label-width="auto"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="上级分类" prop="parentTypeId">
              <el-tree-select
                v-model="itemTypeForm.parentTypeId"
                :data="treeSelectData"
                :props="{ label: 'itemTypeName', children: 'children' }"
                node-key="itemTypeId"
                check-strictly
                :render-after-expand="false"
                default-expand-all
                placeholder="不选则为顶级分类" style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品物料类型编码" prop="itemTypeCode">
              <el-input v-model="itemTypeForm.itemTypeCode" placeholder="请输入产品物料类型编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品物料类型名称" prop="itemTypeName">
              <el-input v-model="itemTypeForm.itemTypeName" placeholder="请输入产品物料类型名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上级分类" prop="parentTypeId">
              <el-input v-model="itemTypeForm.parentTypeId" placeholder="请输入上级分类" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品物料标识" prop="itemOrProduct">
              <el-select v-model="itemTypeForm.itemOrProduct" placeholder="请选择产品物料标识" style="width: 100%">
                <el-option label="物料" value="ITEM" />
                <el-option label="产品" value="PRODUCT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排列顺序" prop="orderNum">
              <el-input-number v-model="itemTypeForm.orderNum" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="enableFlag">
              <!-- 数据库存的是 char(1) 的 Y/N，所以 active-value 写字符串 'Y' -->
              <el-switch v-model="itemTypeForm.enableFlag" active-value="Y" inactive-value="N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="itemTypeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { Plus, Search, Refresh, RefreshLeft, Edit, Delete } from '@element-plus/icons-vue'

import {
  getItemTypeByParentId,
  getItemTypeByPage,
  getItemTypeById,
  createItemType,
  updateItemType,
  deleteItemType
} from '@/api/md/itemType.js'

// ==================== 搜索 ====================

const queryForm = reactive({
  itemTypeCode: '',
  itemTypeName: '',
  itemOrProduct: '',
  enableFlag: '',
})

// ==================== 数据源 ====================

/** 组装好的树形数据 */
const treeData = ref([])

/** 拉回来的平铺数据，父级下拉树要用 */
const flatData = ref([])

const tableRef = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const itemTypeFormRef = ref(null)

const defaultForm = () => ({
  itemTypeCode: '',
  itemTypeName: '',
  parentTypeId: null,
  itemOrProduct: 'ITEM',
  orderNum: 0,
  enableFlag: 'Y',
  remark: '',
})

const itemTypeForm = reactive(defaultForm())

const itemTypeFormRules = {
  itemTypeCode: [
    { required: true, message: '请输入产品物料类型编码', trigger: 'blur' },
    { max: 64, message: '产品物料类型编码长度不能超过 64 个字符', trigger: 'blur' },
  ],
  itemTypeName: [
    { required: true, message: '请输入产品物料类型名称', trigger: 'blur' },
    { max: 255, message: '产品物料类型名称长度不能超过 255 个字符', trigger: 'blur' },
  ],
  parentTypeId: [
    { required: true, message: '请选择上级分类', trigger: 'change' },
  ],
  itemOrProduct: [
    { required: true, message: '请选择产品物料标识', trigger: 'change' },
  ],
  orderNum: [
    { required: true, message: '请输入排列顺序', trigger: 'change' },
  ],
  enableFlag: [
    { required: true, message: '请输入是否启用', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => isEdit.value ? '编辑物料产品分类' : '新增物料产品分类')

/**
 * 清空表单
 * 先 delete 掉所有旧键再赋默认值，避免上一条详情的字段残留
 */
function resetForm() {
  Object.keys(itemTypeForm).forEach(k => delete itemTypeForm[k])
  Object.assign(itemTypeForm, defaultForm())
}

/** 上级分类下拉树的数据：额外加一个「顶级分类」（ID 为 0）作为根节点 */
const treeSelectData = computed(() => [
  { itemTypeId: 0, itemTypeName: '顶级分类' },
  ...treeData.value
])

// ==================== 数据加载 ====================

/**
 * 按需加载树表的数据，
 * @param parentId
 */
const loadItemTypeTree = async function (parentId) {
  let response = await getItemTypeByParentId(parentId);
  treeData.value = response.data;
}

/**
 * 点击树表中的展开箭头的时候触发的加载数据的处理函数
 * @param nodeData 当前点击的行的数据
 */
const loadTreeNode = async function(nodeData,treeNode,resolve){
  // 将当前行的itemTypeId作为parentId传入，查询其子分类列表
  let response = await getItemTypeByParentId(nodeData.itemTypeId);
  // 查到数据之后，使用resolve函数将数据汇入到表格中
  resolve(response.data);
}


// ==================== 搜索区操作 ====================

function handleReset() {
  queryForm.itemTypeCode = ''
  queryForm.itemTypeName = ''
  queryForm.itemOrProduct = ''
  queryForm.enableFlag = ''
  loadItemTypeTree()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  resetForm()
  itemTypeForm.parentTypeId = 0
  dialogVisible.value = true
}

/**
 * 新增下级：自动带上父节点
 * @param {Object} row 父节点行
 */
function handleAddChild(row) {
  isEdit.value = false
  resetForm()
  itemTypeForm.parentTypeId = row.itemTypeId
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  const result = await getItemTypeById(row.itemTypeId)
  if (result.data) {
    Object.assign(itemTypeForm, result.data)
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 删除 ====================

function handleDelete(row) {
  const hasChild = flatData.value.some(item => (item.parentTypeId ?? 0) === row.itemTypeId)
  if (hasChild) {
    ElMessage.warning('存在下级分类，请先删除下级')
    return
  }
  ElMessageBox.confirm(
    `确定要删除物料产品分类「${row.itemTypeName}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteItemType(row.itemTypeId)
    ElMessage.success('删除成功')
    loadItemTypeTree()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  itemTypeFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateItemType(itemTypeForm)
        ElMessage.success('编辑成功')
      } else {
        await createItemType(itemTypeForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadItemTypeTree()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  itemTypeFormRef.value?.resetFields()
  Object.assign(itemTypeForm, defaultForm())
}

onMounted(() => {
  // 默认加载第一层分类
  loadItemTypeTree(0)
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
</style>
