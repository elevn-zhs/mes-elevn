<template>
  <div class="system-user">
    <el-row :gutter="12">

      <!-- ================= 左侧：组织架构（部门树） =================
           点树上的部门 → 右侧列表只显示这个部门的人。
           highlight-current 让当前选中的部门高亮，不加的话点完看不出选的是谁。 -->
      <el-col :span="5">
        <el-card shadow="never" class="dept-tree-card">
          <template #header>
            <div class="tree-header">
              <span>组织架构</span>
              <el-button link type="primary" @click="handleClearDept">全部</el-button>
            </div>
          </template>
          <!--
            expand-on-click-node 默认是 true，点节点名会连带展开/收起子节点，
            筛人的时候这个行为很烦，所以关掉它，只有点前面那个小箭头才展开。
          -->
          <el-tree
            ref="deptTreeRef"
            :data="deptTreeData"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="deptId"
            default-expand-all
            highlight-current
            :expand-on-click-node="false"
            @node-click="handleDeptClick"
          />
        </el-card>
      </el-col>

      <!-- ================= 右侧：用户列表 ================= -->
      <el-col :span="19">
        <el-card shadow="never">

          <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
          <template #header>
            <div class="card-header">
              <span>
                用户管理
                <!-- 当前按哪个部门筛的，在这里再提示一次，免得树滚上去之后忘了 -->
                <el-tag v-if="queryForm.deptId" type="info" size="small" class="dept-filter-tag">
                  {{ deptPathMap[queryForm.deptId] || '已选部门' }}
                </el-tag>
              </span>
              <div class="header-actions">
                <el-button :icon="Download" @click="handleExport">导出</el-button>
                <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
              </div>
            </div>
          </template>

          <!-- ============ 搜索筛选表单 ============ -->
          <el-form :inline="true" :model="queryForm" class="search-form">
            <el-form-item label="用户名">
              <el-input v-model="queryForm.userName" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="queryForm.phonenumber" placeholder="请输入手机号" clearable />
            </el-form-item>
            <el-form-item label="岗位">
              <el-select v-model="queryForm.postId" placeholder="请选择" clearable filterable style="width: 140px">
                <el-option
                  v-for="post in allPosts"
                  :key="post.postId"
                  :value="post.postId"
                  :label="post.postName"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="queryForm.sex" placeholder="请选择" clearable style="width: 120px">
                <el-option v-for="gender in genderDictData" :value="gender.dictValue" :label="gender.dictLabel"/>
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
            <el-table-column prop="avatar" label="头像" >
              <template #default="scope">
                <el-image :src="scope.row.avatar" height="30"/>
              </template>
            </el-table-column>
            <el-table-column prop="userId" label="ID" width="80" />
            <el-table-column prop="userName" label="用户名" width="120" />
            <el-table-column prop="nickName" label="昵称" width="120" />
            <!--
              部门走 getDeptPath() 显示全路径：库里有两个「技术部」（集团总部下面一个、
              长沙分公司下面一个），只显示部门名根本分不出是哪个。
              路径是前端拿部门树自己算出来的，后端只返回部门名。
            -->
            <el-table-column label="部门" min-width="170" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.deptId">{{ getDeptPath(row) }}</span>
                <span v-else class="empty-text">未分配</span>
              </template>
            </el-table-column>
            <el-table-column prop="postName" label="岗位" width="110">
              <template #default="{ row }">
                <span v-if="row.postName">{{ row.postName }}</span>
                <span v-else class="empty-text">未分配</span>
              </template>
            </el-table-column>
            <el-table-column prop="phonenumber" label="手机号" width="140" />
            <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
            <el-table-column prop="sex" label="性别" width="80" align="center">
              <template #default="{ row }">
                <el-tag>{{getGender(row)}}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <!-- status 是 char(1)：'0' 正常，'1' 停用 -->
                <el-tag v-if="row.status === '0'" type="success">正常</el-tag>
                <el-tag v-else type="warning">停用</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="角色" min-width="180">
              <template #default="{ row }">
                <template v-if="splitRoles(row.roleNames).length">
                  <el-tag
                    v-for="roleName in splitRoles(row.roleNames)"
                    :key="roleName"
                    size="small"
                    class="role-tag"
                  >
                    {{ roleName }}
                  </el-tag>
                </template>
                <span v-else class="empty-text">未分配</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="primary" :icon="UserFilled" @click="handleAssignRole(row)">分配角色</el-button>
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
      </el-col>
    </el-row>

    <!-- ============ 新增/编辑用户弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="90px"
      >
        <el-form-item label="用户名" prop="userName">
          <el-input
            v-model="userForm.userName"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <!--
          新增时密码必填；编辑时留空表示「不修改密码」。
          后端 queryById 返回前已经把 password 清掉了，所以编辑回填时这里一定是空的。
        -->
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            :placeholder="isEdit ? '留空表示不修改密码' : '请输入密码'"
            show-password
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="userForm.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <!--
          所属部门用 el-tree-select（下拉框 + 树），和部门管理页面里的「上级部门」一个组件。
          check-strictly 打开后可以选中任意层级：关掉的话点父部门会把子部门一起带上，
          这里只需要「这个人是哪个部门的」，不需要联动。
        -->
        <el-form-item label="所属部门" prop="deptId">
          <el-tree-select
            v-model="userForm.deptId"
            :data="deptTreeData"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="deptId"
            check-strictly
            clearable
            placeholder="请选择所属部门"
            style="width: 100%"
          />
        </el-form-item>
        <!-- 一人一主岗，所以是单选下拉；岗位数据量一般不大，一次性查出来 + filterable 搜索 -->
        <el-form-item label="所属岗位" prop="postId">
          <el-select
            v-model="userForm.postId"
            placeholder="请选择所属岗位"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="post in allPosts"
              :key="post.postId"
              :value="post.postId"
              :label="post.postName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input v-model="userForm.phonenumber" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="userForm.sex">
            <!-- sex 是 char 类型，用 value="0"；后端若是 Integer 要写成 :value="0" -->
            <el-radio v-for="gender in genderDictData" :value="gender.dictValue">{{gender.dictLabel}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="头像" >
          <el-upload
              class="avatar-uploader"
              name="imageFile"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
          >
            <img v-if="imageUrl" :src="imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="userForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 分配角色弹窗 ============ -->
    <el-dialog
      v-model="roleVisible"
      :title="`分配角色 —— ${currentUser.nickName || currentUser.userName}`"
      width="460px"
      :close-on-click-modal="false"
    >
      <!--
        用户和角色是多对多：一个用户可以有多个角色。
        这里用 checkbox-group 展示全部角色，勾选后一次性提交（后端是全量覆盖：先删旧的后插新的）。
      -->
      <el-checkbox-group v-model="checkedRoleIds" class="role-checkbox-group">
        <el-checkbox
          v-for="role in allRoles"
          :key="role.roleId"
          :value="role.roleId"
          :label="role.roleId"
        >
          {{ role.roleName }}（{{ role.roleKey }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitLoading" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Download, Close, UserFilled } from '@element-plus/icons-vue'

import {
  getUserByPage,
  getUserById,
  createUser,
  updateUser,
  deleteUser
} from '@/api/sys/user.js'
import { getAllRoleList } from '@/api/sys/role.js'
import { getRoleIdsByUserId, assignRole } from '@/api/sys/userRole.js'
// 左侧部门树 + 弹窗里的部门选择器，共用这一份树数据
import { getDeptTree } from '@/api/sys/dept.js'
// 岗位下拉的数据源（岗位表数据量小，一次性取完）
import { getPostByPage } from '@/api/sys/post.js'
// 导入加载字典信息的API
import {getDictDataListByType} from "@/api/sys/dictData.js";

// 上传图片的服务器地址
const uploadUrl = "http://localhost:8081/upload/image";
// 对图片进行校验
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像图片不能超过2MB!')
    return false
  }
  return true
}
// 缓存图片上传成功之后的访问地址
const imageUrl = ref("");
// 自动上传图片的处理函数
const handleAvatarSuccess = (response, uploadFile) => {
  // 这里的response就是后端响应的结果对象
  // 将图片访问地址赋值给imageUrl
  imageUrl.value = response.data;
}

// 申明数组来保存性别字典的数据
const genderDictData = ref([]);
// 声明字典的type,这个内容不要修改，是常量
const dictType = "sys_user_sex";
// 根据一行数据对象得到性别对应的文本
function getGender(row){
  let sex = row.sex;// 1 2 3
  // 从字典数据列表中找出对应的文本返回
  for (let index = 0;index < genderDictData.value.length;index++){
    if(genderDictData.value[index].dictValue == sex){
      return genderDictData.value[index].dictLabel
    }
  }
  return "错误"
}


// ==================== 搜索与分页 ====================

const queryForm = reactive({
  userName: '',
  phonenumber: '',
  postId: null,
  sex:'',
  status: null,
  // 左侧部门树点中谁就填谁；null 表示不按部门过滤
  deptId: null
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

// ==================== 部门树相关 ====================

/** 部门树数据（/dept/all 返回的就是带 children 的嵌套结构，后端已经递归组装好了） */
const deptTreeData = ref([])

/** el-tree 组件引用，用来清空选中状态 */
const deptTreeRef = ref(null)

/**
 * 部门ID → 部门全路径 的映射，例如 { 108: '长沙分公司/技术部/研发部' }
 * 由部门树在内存里遍历生成，不额外请求后端。
 * 库里存在两个「技术部」（集团总部下、长沙分公司下各一个），列表里只显示部门名分不清，
 * 所以表格的部门列走 getDeptPath() 显示全路径。
 */
const deptPathMap = ref({})

/** 全部岗位，弹窗下拉和搜索区共用 */
const allPosts = ref([])

/**
 * 递归遍历部门树，把「全路径」铺平成 deptId → path 的字典
 * @param {Array} nodes 当前层节点
 * @param {string} prefix 上层累积的路径
 */
function buildDeptPathMap(nodes, prefix) {
  const map = deptPathMap.value
  ;(nodes || []).forEach(node => {
    const path = prefix ? `${prefix}/${node.deptName}` : node.deptName
    map[node.deptId] = path
    buildDeptPathMap(node.children, path)
  })
}

/** 加载部门树，并顺手把全路径字典建好 */
const loadDeptTree = async function () {
  const result = await getDeptTree()
  deptTreeData.value = result.data || []
  deptPathMap.value = {}
  buildDeptPathMap(deptTreeData.value, '')
}

/** 加载全部岗位（分页接口给个大 pageSize 一次性取完） */
const loadAllPosts = async function () {
  const result = await getPostByPage({ pageNum: 1, pageSize: 1000 })
  allPosts.value = result.data.list
}

/** 表格部门列显示用：优先取全路径，树还没加载完就退回部门名 */
function getDeptPath(row) {
  return deptPathMap.value[row.deptId] || row.deptName || '未分配'
}

/**
 * 把后端 group_concat 拼出来的角色名拆成数组，交给 v-for 渲染成多个 tag
 * @param {string} roleNames 形如「超级管理员,仓储专员」
 */
function splitRoles(roleNames) {
  if (!roleNames) return []
  return String(roleNames).split(',').filter(name => name)
}

/** 点击部门树节点：按这个部门筛人 */
function handleDeptClick(data) {
  queryForm.deptId = data.deptId
  pagination.page = 1
  loadUserList()
}

/** 点击「全部」：取消部门筛选，并把树上的高亮也清掉 */
function handleClearDept() {
  queryForm.deptId = null
  deptTreeRef.value?.setCurrentKey(null)
  pagination.page = 1
  loadUserList()
}

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const userFormRef = ref(null)

const defaultForm = () => ({
  userId: null,
  userName: '',
  nickName: '',
  password: '',
  deptId: null,
  postId: null,
  phonenumber: '',
  email: '',
  sex: '0',
  status: '0',
  remark: ''
})

const userForm = reactive(defaultForm())

const userRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  nickName: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  phonenumber: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

// ==================== 分配角色弹窗相关 ====================

/** 分配角色弹窗是否可见 */
const roleVisible = ref(false)

/** 分配角色按钮的 loading */
const roleSubmitLoading = ref(false)

/** 当前正在分配角色的那个用户 */
const currentUser = ref({})

/** 全部角色列表（角色数量有限，一次性全查出来） */
const allRoles = ref([])

/** 当前勾选的角色ID数组 */
const checkedRoleIds = ref([])

// ==================== 数据加载 ====================

/** 分页查询用户列表 */
const loadUserList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getUserByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

/**
 * 加载全部角色（分配角色弹窗用）
 * 走 /role/all，后端直接返回 List<SysRole>，不分页，所以这里直接取 result.data 就行，
 * 不要写成 result.data.list —— 那是分页接口的结构。
 */
const loadAllRoles = async function () {
  const result = await getAllRoleList()
  allRoles.value = result.data || []
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
  loadUserList()
}

/** 重置：把搜索区连同左侧的部门选中一起清掉，回到「全公司所有人」 */
function handleReset() {
  queryForm.userName = ''
  queryForm.phonenumber = ''
  queryForm.postId = null
  queryForm.sex = ''
  queryForm.status = null
  queryForm.deptId = null
  deptTreeRef.value?.setCurrentKey(null)
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadUserList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  imageUrl.value = "";
  Object.assign(userForm, defaultForm())
  dialogVisible.value = true
}

/**
 * 编辑：按 userId 查最新数据再回填
 * 后端已经把 password 清掉了，所以 userForm.password 会是空串，
 * 提交时如果是空串就不带这个字段，避免把密码覆盖成空。
 * @param {Object} row 当前行
 */
async function handleEdit(row) {
  isEdit.value = true
  imageUrl.value = "";
  const result = await getUserById(row.userId)
  if (result.data) {
    Object.assign(userForm, result.data)
    userForm.password = ''
    imageUrl.value = result.data.avatar || '';
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 分配角色 ====================

/**
 * 打开分配角色弹窗
 * 先查全部角色，再查这个用户当前已有的角色，把已有的勾上
 * @param {Object} row 当前行
 */
async function handleAssignRole(row) {
  currentUser.value = row
  await loadAllRoles()
  const result = await getRoleIdsByUserId(row.userId)
  checkedRoleIds.value = result.data || []
  roleVisible.value = true
}

/**
 * 提交角色分配
 * 后端是「全量覆盖」：先删掉用户原来的全部角色，再把本次勾选的重新插入。
 * 所以一个都不勾直接提交，等价于清空这个用户的角色。
 */
async function handleRoleSubmit() {
  roleSubmitLoading.value = true
  try {
    const result = await assignRole(currentUser.value.userId, checkedRoleIds.value)
    ElMessage.success(`已分配 ${result.data} 个角色`)
    roleVisible.value = false
    // 列表里的角色列来自后端聚合，分配完要重新查一次才会刷新
    loadUserList()
  } finally {
    roleSubmitLoading.value = false
  }
}

// ==================== 删除 ====================

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除用户「${row.userName}」吗？此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteUser(row.userId)
    ElMessage.success('删除成功')
    loadUserList()
  }).catch(() => {})
}

/** 批量删除：后端没有批量接口，用 Promise.all 并发发多个单条请求 */
function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个用户吗？此操作不可恢复。`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.userId)
    await Promise.all(ids.map(id => deleteUser(id)))
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 条`)
    loadUserList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  userFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      // 浅拷贝一份，避免直接改动绑定在表单上的响应式对象
      const payload = { ...userForm }

      // 编辑时密码留空 = 不修改密码，把这个字段整个去掉，别把库里的密码覆盖成空串
      if (isEdit.value && !payload.password) {
        delete payload.password
      }

      // 设置用户头像
      payload.avatar = imageUrl.value;

      if (isEdit.value) {
        await updateUser(payload)
        ElMessage.success('编辑成功')
      } else {
        await createUser(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadUserList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  userFormRef.value?.resetFields()
  Object.assign(userForm, defaultForm())
}

// ==================== 导出 ====================

/**
 * 导出当前页数据为 CSV（带 BOM 防中文乱码）
 * 性别走 getGender()，它内部读的是字典数据 —— 不能另写一张写死的映射表，
 * 那样字典一改导出结果就对不上了。
 */
function handleExport() {
  const headers = ['ID', '用户名', '昵称', '部门', '岗位', '手机号', '邮箱', '性别', '状态', '角色', '创建时间']
  const rows = tableData.value.map(item => [
    item.userId,
    item.userName,
    item.nickName,
    item.deptId ? getDeptPath(item) : '未分配',
    item.postName || '未分配',
    item.phonenumber,
    item.email,
    getGender(item),
    item.status === '0' ? '正常' : '停用',
    item.roleNames || '未分配',
    item.createTime
  ])
  const csv = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `user_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出 ${rows.length} 条数据`)
}

// 加载字典数据
const initDictData = async function(){
  // 加载性别字典数据
  let response = await getDictDataListByType(dictType);
  genderDictData.value = response.data;
}


onMounted(() => {
  // 部门树和岗位是共用数据，先拉，列表拿到 deptId 才有名字可显示
  loadDeptTree()
  loadAllPosts()
  loadUserList()
  // 初始化需要的字典数据
  initDictData();
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

/** 左侧部门树卡片：和右侧列表卡片等高，树不够高时也不会缩成一条 */
.dept-tree-card {
  min-height: 640px;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/** 列表头部的部门筛选提示标签 */
.dept-filter-tag {
  margin-left: 8px;
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

/** 角色 tag 横向排列，多个角色之间留点缝 */
.role-tag {
  margin-right: 4px;
  margin-bottom: 2px;
}

/** 「未分配」这类占位文案，弱化显示别抢眼 */
.empty-text {
  color: #a8abb2;
  font-size: 13px;
}

/** 角色多的时候纵向排列，一行一个更好点 */
.role-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>
