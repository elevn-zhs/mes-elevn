<template>
  <div class="system-role">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="角色名">
          <el-input v-model="queryForm.roleName" placeholder="请输入角色名" clearable />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="queryForm.roleKey" placeholder="如 admin" clearable />
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
        <el-table-column prop="roleId" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名" min-width="120" />
        <el-table-column prop="roleKey" label="权限字符" min-width="140" />
        <el-table-column prop="roleSort" label="显示顺序" width="100" align="center" />
        <el-table-column prop="dataScope" label="数据范围" width="120" align="center">
          <template #default="{ row }">
            {{ dataScopeMap[row.dataScope] || '全部' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <!-- status 是 char(1)：'0' 正常，'1' 停用 -->
            <el-tag v-if="row.status === '0'" type="success">正常</el-tag>
            <el-tag v-else type="warning">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" :icon="Menu" @click="handleAssignMenu(row)">分配权限</el-button>
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

    <!-- ============ 新增/编辑角色弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="90px"
      >
        <el-form-item label="角色名" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="roleForm.roleKey" placeholder="如 admin、common" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="roleSort">
          <el-input-number v-model="roleForm.roleSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="roleForm.dataScope" placeholder="请选择数据范围" style="width: 100%">
            <el-option value="1" label="全部数据权限" />
            <el-option value="2" label="自定义数据权限" />
            <el-option value="3" label="本部门数据权限" />
            <el-option value="4" label="本部门及以下数据权限" />
            <el-option value="5" label="仅本人数据权限" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <!-- status 是 char 类型，用 value="0"；后端若是 Integer 要写成 :value="0" -->
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="roleForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 分配权限弹窗 ============ -->
    <el-dialog
      v-model="menuVisible"
      :title="`分配权限 —— ${currentRole.roleName}`"
      width="520px"
      :close-on-click-modal="false"
    >
      <!--
        菜单是树形结构，用 el-tree + show-checkbox 最直观。
        node-key 指定用 menuId 作为节点唯一标识；
        :props 把 label 映射成 menuName（树节点默认找 label 字段，这里是 menuName）。
      -->
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="menuId"
        show-checkbox
        default-expand-all
        class="menu-tree"
      />
      <template #footer>
        <el-button @click="menuVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSubmitLoading" @click="handleMenuSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Download, Close, Menu } from '@element-plus/icons-vue'

import {
  getRoleByPage,
  getRoleById,
  createRole,
  updateRole,
  deleteRole
} from '@/api/sys/role.js'
import { getMenuTree } from '@/api/sys/menu.js'
import { getMenuIdsByRoleId, assignMenu } from '@/api/sys/roleMenu.js'

// ==================== 静态配置 ====================

/** 数据范围：和 sys_role.data_scope 的取值一一对应（char 类型，key 写字符串） */
const dataScopeMap = {
  1: '全部',
  2: '自定义',
  3: '本部门',
  4: '本部门及以下',
  5: '仅本人'
}

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  roleName: '',
  roleKey: '',
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
const roleFormRef = ref(null)

const defaultForm = () => ({
  roleId: null,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  dataScope: '1',
  status: '0',
  remark: ''
})

const roleForm = reactive(defaultForm())

const roleRules = {
  roleName: [
    { required: true, message: '请输入角色名', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleKey: [
    { required: true, message: '请输入权限字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '权限字符只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  roleSort: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

// ==================== 分配权限弹窗相关 ====================

/** 分配权限弹窗是否可见 */
const menuVisible = ref(false)

/** 确定按钮 loading */
const menuSubmitLoading = ref(false)

/** 当前正在分配权限的角色 */
const currentRole = ref({})

/** 菜单树数据 */
const menuTree = ref([])

/** el-tree 组件引用，用来取勾选的节点 */
const menuTreeRef = ref(null)

// ==================== 数据加载 ====================

/** 分页查询角色列表 */
const loadRoleList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getRoleByPage(params)
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
  loadRoleList()
}

function handleReset() {
  queryForm.roleName = ''
  queryForm.roleKey = ''
  queryForm.status = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadRoleList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(roleForm, defaultForm())
  dialogVisible.value = true
}

/**
 * 编辑：按 roleId 查最新数据再回填
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getRoleById(row.roleId)
  if (result.data) {
    Object.assign(roleForm, result.data)
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 分配权限 ====================

/**
 * 加载菜单树数据
 * 后端 /api/menu/tree 返回的是组装好 children 的完整树，前端直接用
 */
async function loadMenuTree() {
  const result = await getMenuTree()
  menuTree.value = result.data || []
}

/**
 * 打开分配权限弹窗
 * 顺序很关键：必须等菜单树渲染完（nextTick）之后，
 * 才能调用 setCheckedKeys 回显已有的勾选，否则 setCheckedKeys 找不到节点，勾不上去。
 * @param {Object} row 当前行
 */
async function handleAssignMenu(row) {
  currentRole.value = row
  await loadMenuTree()
  const result = await getMenuIdsByRoleId(row.roleId)
  const checkedIds = result.data || []

  menuVisible.value = true
  // 等 el-tree 真正挂载到页面上，再去设置勾选
  await nextTick()
  menuTreeRef.value?.setCheckedKeys(checkedIds)
}

/**
 * 提交权限分配
 *
 * getCheckedKeys 拿的是「全选的节点」，getHalfCheckedKeys 拿的是「子节点只勾了一部分的父节点」。
 * 两个都要提交：只提交 getCheckedKeys 的话，父节点会丢，
 * 页面上就会显示成「子菜单勾着、父菜单没勾」的半吊子状态。
 */
async function handleMenuSubmit() {
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const menuIds = [...checkedKeys, ...halfCheckedKeys]

  menuSubmitLoading.value = true
  try {
    const result = await assignMenu(currentRole.value.roleId, menuIds)
    ElMessage.success(`已授权 ${result.data} 个菜单`)
    menuVisible.value = false
  } finally {
    menuSubmitLoading.value = false
  }
}

// ==================== 删除 ====================

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除角色「${row.roleName}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteRole(row.roleId)
    ElMessage.success('删除成功')
    loadRoleList()
  }).catch(() => {})
}

/** 批量删除：后端没有批量接口，用 Promise.all 并发发多个单条请求 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个角色吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.roleId)
    await Promise.all(ids.map(id => deleteRole(id)))
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadRoleList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  roleFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateRole(roleForm)
        ElMessage.success('编辑成功')
      } else {
        await createRole(roleForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadRoleList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  roleFormRef.value?.resetFields()
  Object.assign(roleForm, defaultForm())
}

// ==================== 导出 ====================

/** 导出当前页数据为 CSV（带 BOM 防中文乱码） */
function handleExport() {
  const headers = ['ID', '角色名', '权限字符', '显示顺序', '数据范围', '状态', '创建时间']
  const rows = tableData.value.map(item => [
    item.roleId,
    item.roleName,
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
  loadRoleList()
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
