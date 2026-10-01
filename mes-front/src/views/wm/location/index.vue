<template>
  <div class="wm-location">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>库区库位</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd('AREA')">新增库区</el-button>
            <el-button type="success" :icon="Plus" @click="handleAdd('LOCATION')">新增库位</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="所属仓库">
          <el-select v-model="queryForm.warehouseId" placeholder="全部仓库" clearable
                     style="width: 180px" @change="handleSearch">
            <el-option v-for="w in warehouseOptions" :key="w.warehouseId"
                       :label="w.warehouseName" :value="w.warehouseId" />
          </el-select>
        </el-form-item>

        <template v-if="viewMode === 'list'">
          <el-form-item label="类型">
            <el-select v-model="queryForm.locationType" placeholder="全部" clearable style="width: 120px">
              <el-option label="库区" value="AREA" />
              <el-option label="库位" value="LOCATION" />
            </el-select>
          </el-form-item>
          <el-form-item label="编码">
            <el-input v-model="queryForm.locationCode" placeholder="请输入编码" clearable />
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="queryForm.locationName" placeholder="请输入名称" clearable />
          </el-form-item>
        </template>

        <el-form-item label="状态">
          <el-select v-model="queryForm.enableFlag" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" value="Y" />
            <el-option label="停用" value="N" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- ============ 两种视图：树形 / 列表 ============ -->
      <el-tabs v-model="viewMode" @tab-change="handleViewChange">

        <!-- ---------- 树形视图：库区为父、库位为子，不分页 ---------- -->
        <el-tab-pane label="树形视图" name="tree">
          <el-table :data="treeData" row-key="locationId" border default-expand-all
                    :tree-props="{ children: 'children' }">
            <el-table-column label="名称" min-width="240">
              <template #default="{ row }">
                <el-tag :type="row.locationType === 'AREA' ? 'primary' : 'success'"
                        size="small" style="margin-right: 6px">
                  {{ row.locationType === 'AREA' ? '库区' : '库位' }}
                </el-tag>
                <el-link type="primary" @click="handleDetail(row)">{{ row.locationName }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="locationCode" label="编码" width="140" />
            <el-table-column prop="warehouseName" label="所属仓库" min-width="160" />
            <el-table-column label="面积(㎡)" width="100" align="right">
              <template #default="{ row }">{{ row.area ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="最大载重" width="100" align="right">
              <template #default="{ row }">{{ row.maxLoa ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="坐标(X,Y,Z)" width="150" align="center">
              <template #default="{ row }">
                {{ row.positionX ?? '-' }}, {{ row.positionY ?? '-' }}, {{ row.positionZ ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column label="冻结" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.frozenFlag === 'Y'" type="danger" size="small">已冻结</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'" size="small">
                  {{ row.enableFlag === 'Y' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.locationType === 'AREA'" link type="success"
                           :icon="Plus" @click="handleAdd('LOCATION', row)">加库位</el-button>
                <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ---------- 列表视图：扁平分页 ---------- -->
        <el-tab-pane label="列表视图" name="list">
          <el-table :data="tableData" border stripe>
            <el-table-column prop="locationId" label="ID" width="70" />
            <el-table-column prop="locationCode" label="编码" min-width="130">
              <template #default="{ row }">
                <el-link type="primary" @click="handleDetail(row)">{{ row.locationCode }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="locationName" label="名称" min-width="160" />
            <el-table-column label="类型" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.locationType === 'AREA' ? 'primary' : 'success'" size="small">
                  {{ row.locationType === 'AREA' ? '库区' : '库位' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="所属仓库" min-width="150" />
            <el-table-column label="上级库区" width="120" align="center">
              <template #default="{ row }">
                <span v-if="row.locationType === 'AREA'" class="muted">— 顶级 —</span>
                <span v-else>{{ parentName(row.parentId) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="area" label="面积(㎡)" width="100" align="right" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
                  {{ row.enableFlag === 'Y' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

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
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ============ 新增/编辑弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="locationFormRef"
        :model="locationForm"
        :rules="locationRules"
        label-width="100px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型" prop="locationType">
              <el-radio-group v-model="locationForm.locationType" :disabled="isEdit">
                <el-radio value="AREA">库区</el-radio>
                <el-radio value="LOCATION">库位</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属仓库" prop="warehouseId">
              <el-select v-model="locationForm.warehouseId" placeholder="请选择仓库"
                         :disabled="isEdit" style="width: 100%">
                <el-option v-for="w in warehouseOptions" :key="w.warehouseId"
                           :label="w.warehouseName" :value="w.warehouseId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="locationForm.locationType === 'LOCATION'" label="所属库区" prop="parentId">
          <el-select v-model="locationForm.parentId" placeholder="请选择所属库区"
                     :disabled="isEdit" style="width: 100%">
            <el-option v-for="a in areaOptions" :key="a.locationId"
                       :label="`${a.locationName}（${a.locationCode}）`" :value="a.locationId" />
          </el-select>
          <div v-if="isEdit" class="form-tip">所属库区与仓库不允许修改，如需调整请新建库位</div>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="编码" prop="locationCode">
              <el-input v-model="locationForm.locationCode" placeholder="如 AREA-A01 / LOC-A01-01" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" prop="locationName">
              <el-input v-model="locationForm.locationName" placeholder="如 钣金料架A-01" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="面积(㎡)" prop="area">
              <el-input-number v-model="locationForm.area" :min="0" :max="999999"
                               :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大载重" prop="maxLoa">
              <el-input-number v-model="locationForm.maxLoa" :min="0" :max="999999"
                               :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="坐标(X,Y,Z)">
          <el-row :gutter="8">
            <el-col :span="8">
              <el-input-number v-model="locationForm.positionX" :min="-9999" :max="9999"
                               placeholder="X" controls-position="right" style="width: 100%" />
            </el-col>
            <el-col :span="8">
              <el-input-number v-model="locationForm.positionY" :min="-9999" :max="9999"
                               placeholder="Y" controls-position="right" style="width: 100%" />
            </el-col>
            <el-col :span="8">
              <el-input-number v-model="locationForm.positionZ" :min="-9999" :max="9999"
                               placeholder="Z" controls-position="right" style="width: 100%" />
            </el-col>
          </el-row>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="是否启用">
              <el-radio-group v-model="locationForm.enableFlag">
                <el-radio value="Y">启用</el-radio>
                <el-radio value="N">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否冻结">
              <el-radio-group v-model="locationForm.frozenFlag">
                <el-radio value="N">正常</el-radio>
                <el-radio value="Y">冻结</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品混放">
              <el-radio-group v-model="locationForm.productMixing">
                <el-radio value="Y">允许</el-radio>
                <el-radio value="N">禁止</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次混放">
              <el-radio-group v-model="locationForm.batchMixing">
                <el-radio value="Y">允许</el-radio>
                <el-radio value="N">禁止</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="locationForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 详情弹窗 ============ -->
    <el-dialog v-model="detailVisible" title="库区库位详情" width="780px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="编码">{{ detailData.locationCode }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ detailData.locationName }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="detailData.locationType === 'AREA' ? 'primary' : 'success'" size="small">
            {{ detailData.locationType === 'AREA' ? '库区' : '库位' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属仓库">{{ detailData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="面积(㎡)">{{ detailData.area ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="最大载重">{{ detailData.maxLoa ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="坐标(X,Y,Z)">
          {{ detailData.positionX ?? '-' }}, {{ detailData.positionY ?? '-' }}, {{ detailData.positionZ ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.enableFlag === 'Y' ? 'success' : 'info'" size="small">
            {{ detailData.enableFlag === 'Y' ? '启用' : '停用' }}
          </el-tag>
          <el-tag v-if="detailData.frozenFlag === 'Y'" type="danger" size="small"
                  style="margin-left: 6px">已冻结</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="产品混放">
          {{ detailData.productMixing === 'Y' ? '允许' : '禁止' }}
        </el-descriptions-item>
        <el-descriptions-item label="批次混放">
          {{ detailData.batchMixing === 'Y' ? '允许' : '禁止' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 库位才有：当前存放的物料与批次 -->
      <template v-if="detailData.locationType === 'LOCATION'">
        <el-divider content-position="left">当前存放（{{ (detailData.stockList || []).length }} 条）</el-divider>
        <el-table :data="detailData.stockList || []" border size="small" max-height="260">
          <el-table-column prop="itemCode" label="物料编码" width="130" />
          <el-table-column prop="itemName" label="物料名称" min-width="140" show-overflow-tooltip />
          <el-table-column prop="batchCode" label="批次号" width="140" show-overflow-tooltip />
          <el-table-column prop="quantityOnhand" label="在库数" width="90" align="right" />
          <el-table-column prop="quantityReserved" label="保留数" width="90" align="right" />
          <el-table-column prop="quantityAvailable" label="可用数" width="90" align="right" />
          <el-table-column prop="expireDate" label="有效期" width="160" />
        </el-table>
        <el-empty v-if="!(detailData.stockList || []).length" description="该库位当前没有库存" :image-size="60" />
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, View } from '@element-plus/icons-vue'

import {
  getLocationByPage,
  getLocationTree,
  getLocationById,
  createLocation,
  updateLocation,
  deleteLocation,
  deleteLocationBatch,
  getLocationByCode
} from '@/api/wm/location.js'
import { getAllWarehouseList } from '@/api/wm/warehouse.js'

// ==================== 视图与查询条件 ====================

// tree = 树形视图（不分页） / list = 列表视图（分页）
const viewMode = ref('tree')

const queryForm = reactive({
  warehouseId: null,
  locationType: null,
  locationCode: '',
  locationName: '',
  enableFlag: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ==================== 数据源 ====================

const treeData = ref([])
const tableData = ref([])
const warehouseOptions = ref([])

/** 新增库位时"所属库区"下拉的数据源：直接从树里取库区节点 */
const areaOptions = computed(() => treeData.value.filter(n => n.locationType === 'AREA'))

/** 列表视图里把 parentId 显示成库区名称 */
const parentName = (parentId) => {
  const area = treeData.value.find(n => n.locationId === parentId)
  return area ? area.locationName : '-'
}

// ==================== 新增/编辑弹窗相关 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const locationFormRef = ref(null)

const defaultForm = () => ({
  locationId: null,
  parentId: 0,
  locationType: 'AREA',
  locationCode: '',
  locationName: '',
  warehouseId: null,
  area: null,
  maxLoa: null,
  positionX: null,
  positionY: null,
  positionZ: null,
  areaFlag: 'N',
  enableFlag: 'Y',
  frozenFlag: 'N',
  productMixing: 'Y',
  batchMixing: 'Y',
  remark: ''
})

const locationForm = reactive(defaultForm())

const locationRules = computed(() => ({
  locationType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择所属仓库', trigger: 'change' }],
  // 只有库位才要求选库区
  parentId: locationForm.locationType === 'LOCATION'
    ? [{ required: true, message: '请选择所属库区', trigger: 'change' }]
    : [],
  locationCode: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { max: 64, message: '编码长度不能超过 64 个字符', trigger: 'blur' }
  ],
  locationName: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 255, message: '名称长度不能超过 255 个字符', trigger: 'blur' }
  ]
}))

const dialogTitle = computed(() => {
  const typeName = locationForm.locationType === 'AREA' ? '库区' : '库位'
  return (isEdit.value ? '编辑' : '新增') + typeName
})

// ==================== 详情弹窗 ====================

const detailVisible = ref(false)
const detailData = ref({})

// ==================== 数据加载 ====================

const loadTree = async function () {
  const result = await getLocationTree({
    warehouseId: queryForm.warehouseId,
    enableFlag: queryForm.enableFlag
  })
  treeData.value = result.data || []
}

const loadList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  const result = await getLocationByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

const loadWarehouseOptions = async function () {
  const result = await getAllWarehouseList()
  warehouseOptions.value = result.data || []
}

/** 按当前视图重新加载 */
const reload = function () {
  if (viewMode.value === 'tree') {
    loadTree()
  } else {
    loadList()
  }
}

// ==================== 搜索区操作 ====================

function handleSearch() {
  pagination.page = 1
  reload()
}

function handleReset() {
  queryForm.warehouseId = null
  queryForm.locationType = null
  queryForm.locationCode = ''
  queryForm.locationName = ''
  queryForm.enableFlag = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadList()
}

function handleViewChange() {
  pagination.page = 1
  reload()
}

// ==================== 新增 / 编辑 ====================

/**
 * 新增
 * @param {string} type 'AREA' 或 'LOCATION'
 * @param {Object} [parentArea] 从库区行点"加库位"时传进来，直接锁定父库区
 */
function handleAdd(type, parentArea) {
  if (type === 'LOCATION' && !areaOptions.value.length) {
    ElMessage.warning('请先新增库区，库位必须挂在库区下面')
    return
  }
  isEdit.value = false
  Object.assign(locationForm, defaultForm())
  locationForm.locationType = type
  if (parentArea) {
    locationForm.parentId = parentArea.locationId
    locationForm.warehouseId = parentArea.warehouseId
  } else if (queryForm.warehouseId) {
    // 当前筛选了仓库，新增时默认带上，少点一次
    locationForm.warehouseId = queryForm.warehouseId
  }
  dialogVisible.value = true
}

async function handleEdit(row) {
  const result = await getLocationById(row.locationId)
  if (result.data) {
    Object.assign(locationForm, defaultForm(), result.data)
    isEdit.value = true
    dialogVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

async function handleDetail(row) {
  const result = await getLocationById(row.locationId)
  if (result.data) {
    detailData.value = result.data
    detailVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 删除 ====================

function handleDelete(row) {
  const typeName = row.locationType === 'AREA' ? '库区' : '库位'
  ElMessageBox.confirm(
    `确定要删除${typeName}「${row.locationName}」吗？若有下级库位或仍有库存，后端会拒绝删除。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteLocation(row.locationId)
    ElMessage.success('删除成功')
    reload()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  locationFormRef.value.validate(async (valid) => {
    if (!valid) return

    // 编码查重：先给用户一次友好提示，最终以后端校验为准
    if (!isEdit.value) {
      const check = await getLocationByCode(locationForm.locationType, locationForm.locationCode)
      if (check.data) {
        ElMessage.warning(`该编码已被占用：${locationForm.locationCode}`)
        return
      }
    }

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateLocation(locationForm)
        ElMessage.success('编辑成功')
      } else {
        await createLocation(locationForm)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      reload()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  locationFormRef.value?.resetFields()
  Object.assign(locationForm, defaultForm())
}

onMounted(() => {
  loadWarehouseOptions()
  loadTree()
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
  margin-bottom: 8px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 2px;
}

.muted {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
