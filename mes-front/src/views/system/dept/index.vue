<template>
  <div class="system-dept">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <div class="header-actions">
            <el-button :icon="Download" @click="handleExport">导出</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增部门</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="部门名称">
          <el-input v-model="queryForm.deptName" placeholder="请输入部门名称" clearable />
        </el-form-item>
        <el-form-item label="部门编码">
          <el-input v-model="queryForm.deptCode" placeholder="请输入部门编码" clearable />
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

      <!-- ============ 数据表格（树形） ============ -->
      <!--
        row-key 必须指定，而且值要唯一，否则展开/收起会错乱
        :tree-props 告诉 el-table 哪个字段是子节点 —— 后端 SysDept 里就叫 children
        default-expand-all 默认全部展开，部门这种数据量一眼能看完，省得一层层点
      -->
      <el-table
        ref="tableRef"
        :data="tableData"
        row-key="deptId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        border
        stripe
      >
        <el-table-column prop="deptName" label="部门名称" min-width="200" />
        <el-table-column prop="deptCode" label="部门编码" width="140" />
        <el-table-column prop="orderNum" label="排序" width="80" align="center" />
        <el-table-column prop="leader" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <!-- status 在数据库里是 char(1)：'0' 正常，'1' 停用，所以这里比较的是字符串 -->
            <el-tag v-if="row.status === '0'" type="success">正常</el-tag>
            <el-tag v-else type="warning">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Plus" @click="handleAddChild(row)">新增下级</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ============ 新增/编辑部门弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="deptFormRef"
        :model="deptForm"
        :rules="deptRules"
        label-width="90px"
      >
        <el-form-item label="上级部门" prop="parentId">
          <!--
            el-tree-select 就是「下拉框 + 树」的组合，选上级部门用它最合适
            check-strictly 打开后可以选中任意层级（否则父子会联动勾选）
            :props 里把 label 映射成 deptName，因为树的节点没有默认的 name 字段
          -->
          <el-tree-select
            v-model="deptForm.parentId"
            :data="treeOptions"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="deptId"
            check-strictly
            placeholder="请选择上级部门（不选则为顶级）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="deptForm.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="deptForm.deptCode" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="deptForm.orderNum" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="deptForm.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="deptForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="deptForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="deptForm.status">
            <!-- 数据库 status 是 char 类型，所以这里用 value="0"（字符串）；如果后端是 Integer 类型，要写成 :value="0" -->
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="deptForm.remark" type="textarea" :rows="2" />
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
import { Plus, Search, Refresh, Edit, Delete, Download } from '@element-plus/icons-vue'

import {
  getDeptTree,
  getDeptById,
  createDept,
  updateDept,
  deleteDept
} from '@/api/sys/dept.js'

// ==================== 搜索条件 ====================

/** 搜索筛选条件：部门名称 / 部门编码 / 状态 */
const queryForm = reactive({
  deptName: '',
  deptCode: '',
  status: null
})

// ==================== 数据源 ====================

/**
 * 树形表格的数据源
 * 直接拿后端 /dept/tree 返回的嵌套结构（每个节点自带 children），
 * 前端不用再自己拼树 —— 后端已经用「查全量 + 内存组装」做完了。
 */
const tableData = ref([])

/**
 * 上级部门下拉树的数据源
 * 和表格共用同一份树数据，但表格会随搜索条件变化，下拉树始终要展示完整的部门结构，
 * 所以单独存一份，避免搜索后「上级部门」选项跟着变少。
 */
const treeOptions = ref([])

/** el-table 组件引用 */
const tableRef = ref(null)

// ==================== 新增/编辑弹窗相关 ====================

/** 弹窗是否可见 */
const dialogVisible = ref(false)

/** true = 编辑，false = 新增 */
const isEdit = ref(false)

/** 提交按钮 loading，防止重复提交 */
const submitLoading = ref(false)

/** 弹窗内 el-form 引用，用于 validate() 和 resetFields() */
const deptFormRef = ref(null)

/**
 * 表单默认值
 * parentId 默认为 0：0 表示「没有上级」，也就是顶级部门
 */
const defaultForm = () => ({
  deptId: null,
  parentId: 0,
  deptName: '',
  deptCode: '',
  orderNum: 0,
  leader: '',
  phone: '',
  email: '',
  status: '0',
  remark: ''
})

/** 弹窗表单的双向绑定数据源 */
const deptForm = reactive(defaultForm())

/** 表单校验规则 */
const deptRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 20, message: '部门名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  deptCode: [
    { required: true, message: '请输入部门编码', trigger: 'blur' }
  ],
  orderNum: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')

// ==================== 数据加载 ====================

/**
 * 加载部门树
 * 搜索条件一并传给后端，由后端在组装树之前过滤
 */
const loadDeptTree = async function () {
  const result = await getDeptTree(queryForm)
  tableData.value = result.data || []
  // 第一次加载时把完整结构备份一份给下拉树用
  if (!queryForm.deptName && !queryForm.deptCode && !queryForm.status) {
    treeOptions.value = result.data || []
  }
}

/**
 * 加载上级部门下拉树（点击「新增」时调一次，保证选项是最新的）
 */
const loadTreeOptions = async function () {
  const result = await getDeptTree({})
  treeOptions.value = result.data || []
}

// ==================== 搜索区操作 ====================

/** 搜索：按当前条件重新查树 */
function handleSearch() {
  loadDeptTree()
}

/** 重置：清空条件后重新查 */
function handleReset() {
  queryForm.deptName = ''
  queryForm.deptCode = ''
  queryForm.status = null
  loadDeptTree()
}

// ==================== 新增 / 编辑 ====================

/** 新增顶级部门 */
function handleAdd() {
  isEdit.value = false
  Object.assign(deptForm, defaultForm())
  loadTreeOptions()
  dialogVisible.value = true
}

/**
 * 新增下级部门：把当前行的 deptId 作为新部门的 parentId 预填进去
 * @param {Object} row 当前行（父部门）
 */
function handleAddChild(row) {
  isEdit.value = false
  Object.assign(deptForm, defaultForm())
  deptForm.parentId = row.deptId
  loadTreeOptions()
  dialogVisible.value = true
}

/**
 * 编辑：先根据 id 查最新数据再回填，
 * 不要直接用表格里的 row —— 表格数据可能是几分钟前查的，已经不是最新的了
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  const result = await getDeptById(row.deptId)
  if (result.data) {
    Object.assign(deptForm, result.data)
    // 后端可能返回 null，兜一下底，否则 el-tree-select 会报警告
    if (deptForm.parentId === null || deptForm.parentId === undefined) {
      deptForm.parentId = 0
    }
    loadTreeOptions()
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 删除 ====================

/**
 * 删除部门
 * 有子部门的先提示一下 —— 后端是逻辑删除，子部门不会跟着删，
 * 但会变成「父部门没了、子部门还挂着」的孤儿数据，所以这里拦一道
 * @param {Object} row 当前行
 */
function handleDelete(row) {
  const hasChild = row.children && row.children.length > 0
  const tip = hasChild
    ? `部门「${row.deptName}」下还有 ${row.children.length} 个子部门，删除后子部门将失去上级，确定继续吗？`
    : `确定要删除部门「${row.deptName}」吗？此操作不可恢复。`

  ElMessageBox.confirm(tip, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDept(row.deptId)
    ElMessage.success('删除成功')
    loadDeptTree()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

/**
 * 提交：先校验，再根据 isEdit 走新增或修改分支
 * 两个分支的后端接口都是「不带 id 的路径」，id 放在请求体里，所以都传整个表单对象即可
 */
function handleSubmit() {
  deptFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateDept(deptForm)
        ElMessage.success('编辑成功')
      } else {
        await createDept(deptForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadDeptTree()
    } finally {
      submitLoading.value = false
    }
  })
}

/** 弹窗关闭时清理校验提示和表单数据 */
function handleDialogClose() {
  deptFormRef.value?.resetFields()
  Object.assign(deptForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 把树形数据摊平后再导出 CSV
 * 树是嵌套结构，直接导出会显示成 [object Object]，所以先递归拍平成一维数组
 * 文件带 UTF-8 BOM（\ufeff），防止 Excel 打开中文乱码
 */
function handleExport() {
  const flatten = (list) => list.reduce((acc, item) => {
    const { children, ...rest } = item
    acc.push(rest)
    if (children && children.length) acc.push(...flatten(children))
    return acc
  }, [])

  const rows = flatten(tableData.value).map(item => [
    item.deptId,
    item.deptName,
    item.deptCode,
    item.orderNum,
    item.leader,
    item.phone,
    item.status === '0' ? '正常' : '停用',
    item.createTime
  ])

  const headers = ['ID', '部门名称', '部门编码', '排序', '负责人', '电话', '状态', '创建时间']
  const csv = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `dept_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

onMounted(() => {
  loadDeptTree()
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
