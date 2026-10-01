<template>
  <div class="system-menu">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="loadMenuTree">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增菜单</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="菜单名称">
          <el-input v-model="queryForm.menuName" placeholder="请输入菜单名称" clearable />
        </el-form-item>
        <el-form-item label="权限类型">
          <el-select v-model="queryForm.menuType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="目录" value="M" />
            <el-option label="菜单" value="C" />
            <el-option label="按钮" value="F" />
            <el-option label="接口" value="A" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- ============ 数据表格（树形） ============ -->
      <!--
        菜单天然是树：目录 → 菜单 → 按钮/接口。
        row-key 用 menuId，:tree-props 指定子节点字段是 children（后端 SysMenu 里就叫 children）。
      -->
      <el-table
        ref="tableRef"
        :data="tableData"
        row-key="menuId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        border
        stripe
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column label="图标" width="70" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="getIcon(row.icon)" /></el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="70" align="center" />
        <el-table-column prop="menuType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="menuTypeMap[row.menuType]?.type || 'info'" size="small">
              {{ menuTypeMap[row.menuType]?.label || row.menuType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由地址" min-width="140" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" min-width="160" show-overflow-tooltip />
        <el-table-column prop="perms" label="权限标识" min-width="140" show-overflow-tooltip />
        <el-table-column prop="visible" label="显示" width="80" align="center">
          <template #default="{ row }">
            <!-- visible：0 显示，1 隐藏 -->
            <el-tag v-if="row.visible === '0'" type="success" size="small">显示</el-tag>
            <el-tag v-else type="info" size="small">隐藏</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === '0'" type="success" size="small">正常</el-tag>
            <el-tag v-else type="warning" size="small">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Plus" @click="handleAddChild(row)">新增下级</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ============ 新增/编辑菜单弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="640px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="menuFormRef"
        :model="menuForm"
        :rules="menuRules"
        label-width="100px"
      >
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="treeOptions"
            :props="{ label: 'menuName', children: 'children' }"
            node-key="menuId"
            check-strictly
            placeholder="请选择上级菜单（不选则为顶级）"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="权限类型" prop="menuType">
          <el-radio-group v-model="menuForm.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
            <el-radio value="A">接口</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="菜单图标" prop="icon">
          <el-input v-model="menuForm.icon" placeholder="Element Plus 图标名，如 User、Setting" />
        </el-form-item>

        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="menuForm.orderNum" :min="0" :max="999" />
        </el-form-item>

        <!-- 路由地址和组件路径只有目录/菜单才用得上，按钮和接口不需要 -->
        <el-form-item v-if="menuForm.menuType === 'M' || menuForm.menuType === 'C'" label="路由地址" prop="path">
          <el-input v-model="menuForm.path" placeholder="如 user、/system/user" />
        </el-form-item>

        <el-form-item v-if="menuForm.menuType === 'C'" label="组件路径" prop="component">
          <el-input v-model="menuForm.component" placeholder="如 system/user/index" />
        </el-form-item>

        <!-- 权限标识：按钮和菜单用来做「有没有权限点这个按钮」的判断 -->
        <el-form-item v-if="menuForm.menuType === 'C' || menuForm.menuType === 'F'" label="权限标识" prop="perms">
          <el-input v-model="menuForm.perms" placeholder="如 system:user:add" />
        </el-form-item>

        <!-- 接口类型的菜单要额外登记接口地址和请求方式，后端鉴权时按这个匹配 -->
        <template v-if="menuForm.menuType === 'A'">
          <el-form-item label="接口地址" prop="apiUrl">
            <el-input v-model="menuForm.apiUrl" placeholder="如 /system/user/**" />
          </el-form-item>
          <el-form-item label="请求方式" prop="apiMethod">
            <el-select v-model="menuForm.apiMethod" placeholder="请选择" clearable style="width: 100%">
              <el-option label="GET" value="GET" />
              <el-option label="POST" value="POST" />
              <el-option label="PUT" value="PUT" />
              <el-option label="DELETE" value="DELETE" />
            </el-select>
          </el-form-item>
        </template>

        <el-form-item v-if="menuForm.menuType !== 'F' && menuForm.menuType !== 'A'" label="显示状态" prop="visible">
          <el-radio-group v-model="menuForm.visible">
            <el-radio value="0">显示</el-radio>
            <el-radio value="1">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="菜单状态" prop="status">
          <el-radio-group v-model="menuForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="menuForm.remark" type="textarea" :rows="2" />
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
import {
  Plus, Search, Refresh, RefreshLeft, Edit, Delete
} from '@element-plus/icons-vue'
// 菜单图标是「用户填的图标名字符串」，需要把 Element Plus 的图标全量注册进来才能动态渲染
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import {
  getMenuTree,
  getMenuById,
  createMenu,
  updateMenu,
  deleteMenu
} from '@/api/sys/menu.js'

/**
 * 按图标名取出对应的图标组件
 *
 * 注意这里不能直接写 <component :is="row.icon" />：
 * :is 传字符串时，Vue 只会去「全局注册过的组件」里找，而 Element Plus 的图标并没有全局注册，
 * 结果就是渲染不出来。必须先自己从图标库里按名字取出组件对象，再交给 :is。
 * @param {string} name 图标名，如 User、Setting
 */
function getIcon(name) {
  return ElementPlusIconsVue[name] || null
}

// ==================== 静态配置 ====================

/** 权限类型：M目录 C菜单 F按钮 A接口 */
const menuTypeMap = {
  M: { label: '目录', type: 'success' },
  C: { label: '菜单', type: 'primary' },
  F: { label: '按钮', type: 'warning' },
  A: { label: '接口', type: 'info' }
}

// ==================== 搜索条件 ====================

const queryForm = reactive({
  menuName: '',
  menuType: null,
  status: null
})

// ==================== 数据源 ====================

/** 树形表格数据（后端 /menu/tree 直接返回嵌套结构） */
const tableData = ref([])

/** 上级菜单下拉树数据，始终保存完整结构 */
const treeOptions = ref([])

const tableRef = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const menuFormRef = ref(null)

/**
 * 表单默认值
 * parentId 默认 0 表示顶级菜单；menuType 默认 C（菜单）
 */
const defaultForm = () => ({
  menuId: null,
  parentId: 0,
  menuName: '',
  menuType: 'C',
  orderNum: 0,
  path: '',
  component: '',
  query: '',
  isFrame: '1',
  isCache: '0',
  visible: '0',
  status: '0',
  perms: '',
  icon: '',
  apiUrl: '',
  apiMethod: '',
  remark: ''
})

const menuForm = reactive(defaultForm())

const menuRules = {
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' }
  ],
  orderNum: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入路由地址', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑菜单' : '新增菜单')

// ==================== 数据加载 ====================

/** 加载菜单树（搜索条件一并传给后端） */
const loadMenuTree = async function () {
  const result = await getMenuTree(queryForm)
  tableData.value = result.data || []
  // 没有任何筛选条件时，把完整结构备份一份给「上级菜单」下拉树用
  if (!queryForm.menuName && !queryForm.menuType && !queryForm.status) {
    treeOptions.value = result.data || []
  }
}

/** 加载完整菜单树，供上级菜单下拉使用 */
const loadTreeOptions = async function () {
  const result = await getMenuTree({})
  treeOptions.value = result.data || []
}

// ==================== 搜索区操作 ====================

function handleSearch() {
  loadMenuTree()
}

function handleReset() {
  queryForm.menuName = ''
  queryForm.menuType = null
  queryForm.status = null
  loadMenuTree()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(menuForm, defaultForm())
  loadTreeOptions()
  dialogVisible.value = true
}

/** 新增下级：把当前行的 menuId 预填为 parentId */
function handleAddChild(row) {
  isEdit.value = false
  Object.assign(menuForm, defaultForm())
  menuForm.parentId = row.menuId
  loadTreeOptions()
  dialogVisible.value = true
}

/**
 * 编辑：按 menuId 查最新数据再回填
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getMenuById(row.menuId)
  if (result.data) {
    Object.assign(menuForm, result.data)
    if (menuForm.parentId === null || menuForm.parentId === undefined) {
      menuForm.parentId = 0
    }
    loadTreeOptions()
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 删除 ====================

/**
 * 删除菜单
 * 有子菜单的先拦一道 —— 后端是逻辑删除，删了父节点子节点会变成孤儿
 * @param {Object} row 当前行
 */
function handleDelete(row) {
  const hasChild = row.children && row.children.length > 0
  const tip = hasChild
    ? `菜单「${row.menuName}」下还有 ${row.children.length} 个子菜单，删除后子菜单将失去上级，确定继续吗？`
    : `确定要删除菜单「${row.menuName}」吗？此操作不可恢复。`

  ElMessageBox.confirm(tip, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteMenu(row.menuId)
    ElMessage.success('删除成功')
    loadMenuTree()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  menuFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateMenu(menuForm)
        ElMessage.success('编辑成功')
      } else {
        await createMenu(menuForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadMenuTree()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  menuFormRef.value?.resetFields()
  Object.assign(menuForm, defaultForm())
}

onMounted(() => {
  loadMenuTree()
  loadTreeOptions()
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
