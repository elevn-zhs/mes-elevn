<template>
  <div class="wm-taking">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="盘点计划" name="plan" />
            <el-tab-pane label="盘点单" name="taking" />
          </el-tabs>
          <div>
            <el-button v-if="activeTab === 'plan'" type="primary" :icon="Plus"
                       @click="handleAddPlan">新增计划</el-button>
          </div>
        </div>
      </template>

      <!-- ==================== 盘点计划 ==================== -->
      <template v-if="activeTab === 'plan'">
        <el-table :data="planData" border stripe>
          <el-table-column prop="planId" label="ID" width="70" />
          <el-table-column prop="planCode" label="计划编号" min-width="150" />
          <el-table-column prop="planName" label="计划名称" min-width="160" show-overflow-tooltip />
          <el-table-column label="类型" width="95" align="center">
            <template #default="{ row }">{{ row.takingType === 'FULL' ? '全盘' : '部分盘点' }}</template>
          </el-table-column>
          <el-table-column label="范围" min-width="220">
            <template #default="{ row }">
              <el-tag v-for="s in row.scopePreview || []" :key="s" size="small"
                      style="margin-right: 4px">{{ s }}</el-tag>
              <span v-if="!row.scopePreview">-</span>
            </template>
          </el-table-column>
          <el-table-column label="盲盘" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="row.blindFlag === 'Y' ? 'danger' : 'info'" size="small">
                {{ row.blindFlag === 'Y' ? '盲盘' : '明盘' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="160" />
          <el-table-column prop="takingCount" label="已生成单" width="90" align="right" />
          <el-table-column label="状态" width="95" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PREPARE' ? 'info' : 'primary'" size="small">
                {{ row.status === 'PREPARE' ? '待执行' : '已生成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status !== 'PREPARE'"
                         @click="handleEditPlan(row)">编辑</el-button>
              <el-button link type="success" :disabled="row.status !== 'PREPARE'"
                         @click="handleGenerate(row)">生成盘点单</el-button>
              <el-button link type="danger" :disabled="row.status !== 'PREPARE'"
                         @click="handleDeletePlan(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- ==================== 盘点单 ==================== -->
      <template v-else>
        <el-table :data="takingData" border stripe>
          <el-table-column prop="takingId" label="ID" width="70" />
          <el-table-column prop="takingCode" label="盘点单号" min-width="150">
            <template #default="{ row }">
              <el-link type="primary" @click="openCount(row)">{{ row.takingCode }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="takingName" label="名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="planCode" label="来源计划" min-width="140">
            <template #default="{ row }">{{ row.planCode || '-' }}</template>
          </el-table-column>
          <el-table-column label="盲盘" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="row.blindFlag === 'Y' ? 'danger' : 'info'" size="small">
                {{ row.blindFlag === 'Y' ? '盲盘' : '明盘' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lineCount" label="行数" width="70" align="right" />
          <el-table-column label="已录/总" width="90" align="center">
            <template #default="{ row }">{{ row.countedCount }} / {{ row.lineCount }}</template>
          </el-table-column>
          <el-table-column label="盘盈" width="90" align="right">
            <template #default="{ row }">
              <span :style="profitStyle(row.profitQuantity)">{{ row.profitQuantity || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="盘亏" width="90" align="right">
            <template #default="{ row }">
              <span :style="lossStyle(row.lossQuantity)">{{ Math.abs(row.lossQuantity || 0) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="95" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'CONFIRMED' ? 'success' : 'info'" size="small">
                {{ row.status === 'CONFIRMED' ? '已过账' : '待录入' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status !== 'PREPARE'"
                         @click="openCount(row)">录入</el-button>
              <el-button link type="warning" :disabled="row.status !== 'PREPARE'"
                         @click="handlePost(row)">过账</el-button>
              <el-button link type="info" @click="openDiff(row)">差异</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
                       :page-sizes="[10, 20, 50]" :total="pagination.total"
                       layout="total, sizes, prev, pager, next" background
                       @change="handlePageChange" />
      </div>
    </el-card>

    <!-- 新增/编辑计划 -->
    <el-dialog v-model="planDialogVisible" :title="isPlanEdit ? '编辑盘点计划' : '新增盘点计划'"
               width="760px" :close-on-click-modal="false" @close="handlePlanDialogClose">
      <el-form ref="planFormRef" :model="planForm" :rules="planRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="planForm.planName" placeholder="如：9月末原料仓盘点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="盘点类型" prop="takingType">
              <el-select v-model="planForm.takingType" style="width: 100%">
                <el-option label="部分盘点（按范围）" value="PART" />
                <el-option label="全盘" value="FULL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="planForm.startTime" type="datetime"
                              value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker v-model="planForm.endTime" type="datetime"
                              value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="盲盘">
              <el-switch v-model="planForm.blindFlag" active-value="Y" inactive-value="N"
                         active-text="录入页不显示账面数" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="planForm.remark" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">盘点范围（可混选，生成时取并集）</el-divider>
        <div v-for="(s, idx) in planForm.scopeList" :key="idx" style="margin-bottom: 8px">
          <el-select v-model="s.scopeType" style="width: 140px" placeholder="类型"
                     @change="s.scopeValueId = null; s.scopeValueCode = ''; s.scopeValueName = ''">
            <el-option label="按仓库" value="WAREHOUSE" />
            <el-option label="按库区" value="AREA" />
            <el-option label="按物料分类" value="ITEM_TYPE" />
          </el-select>
          <el-select v-model="s.scopeValueId" style="width: 280px; margin-left: 8px"
                     placeholder="选择对象" filterable @change="onScopeValueChange(s)">
            <el-option v-for="o in scopeOptions(s.scopeType)" :key="o.id"
                       :label="o.label" :value="o.id" />
          </el-select>
          <el-button type="danger" link style="margin-left: 8px"
                     @click="planForm.scopeList.splice(idx, 1)">移除</el-button>
        </div>
        <el-button :icon="Plus" @click="planForm.scopeList.push(emptyScope())">加范围</el-button>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handlePlanSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 录入实盘 -->
    <el-dialog v-model="countDialogVisible" :title="`实盘录入 → ${currentTaking?.takingCode || ''}`"
               width="920px" :close-on-click-modal="false">
      <el-alert v-if="currentTaking && currentTaking.blindFlag === 'Y'" type="warning"
                :closable="false" show-icon style="margin-bottom: 10px"
                title="盲盘模式：账面数量与差异不显示，凭实际清点录入" />
      <el-table :data="countLines" border size="small" max-height="420">
        <el-table-column prop="itemCode" label="物料编码" min-width="120" />
        <el-table-column prop="itemName" label="名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="batchCode" label="批次" min-width="140">
          <template #default="{ row }">{{ row.batchCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="locationCode" label="库位" width="90" />
        <el-table-column label="账面" width="90" align="right">
          <template #default="{ row }">
            <span>{{ isBlind ? '***' : (row.quantity ?? '-') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实盘录入" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.takingQuantity" :min="0" :max="99999999"
                             size="small" controls-position="right" style="width: 120px" />
          </template>
        </el-table-column>
        <el-table-column label="差异" width="90" align="right">
          <template #default="{ row }">
            <span v-if="!isBlind && row.quantity != null && row.takingQuantity != null"
                  :style="diffStyle(row.takingQuantity - row.quantity)">
              {{ (row.takingQuantity - row.quantity > 0 ? '+' : '') + (row.takingQuantity - row.quantity) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.takingStatus === 'CONFIRMED' ? 'success' : 'info'" size="small">
              {{ row.takingStatus === 'CONFIRMED' ? '已录' : '未录' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="countDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSaveCounts">保存实盘</el-button>
      </template>
    </el-dialog>

    <!-- 差异明细 -->
    <el-dialog v-model="diffVisible" title="盘点差异（盘盈 / 盘亏）" width="820px">
      <el-table :data="diffLines" border size="small" max-height="380">
        <el-table-column prop="itemCode" label="物料编码" min-width="120" />
        <el-table-column prop="itemName" label="名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="batchCode" label="批次" min-width="140">
          <template #default="{ row }">{{ row.batchCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="locationCode" label="库位" width="90" />
        <el-table-column prop="quantity" label="账面" width="90" align="right" />
        <el-table-column prop="takingQuantity" label="实盘" width="90" align="right" />
        <el-table-column label="差异" width="100" align="right">
          <template #default="{ row }">
            <span :style="diffStyle(row.diffQuantity)">
              {{ row.diffQuantity > 0 ? '+' : '' }}{{ row.diffQuantity }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="diffVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

import {
  createTakingPlan, getTakingPlanByPage, getTakingPlanById,
  updateTakingPlan, deleteTakingPlan, generateTaking,
  getTakingByPage, getTakingById, saveTakingQuantities, postTaking, getTakingDiff
} from '@/api/wm/stockTaking.js'
import { getWarehouseByPage } from '@/api/wm/warehouse.js'
import { getLocationTree } from '@/api/wm/location.js'
import { getItemTypeByPage } from '@/api/md/itemType.js'

const activeTab = ref('plan')
const pagination = reactive({ page: 1, size: 10, total: 0 })
const planData = ref([])
const takingData = ref([])
const submitLoading = ref(false)

// 中文股口径：盈红亏绿（A 股习惯：涨红跌绿）
const profitStyle = v => v > 0 ? 'color:#f56c6c;font-weight:bold' : ''
const lossStyle = v => v < 0 ? 'color:#67c23a;font-weight:bold' : ''
const diffStyle = v => v > 0 ? 'color:#f56c6c' : (v < 0 ? 'color:#67c23a' : '')

function handleTabChange() { pagination.page = 1; loadCurrent() }
function loadCurrent() { activeTab.value === 'plan' ? loadPlans() : loadTakings() }
function handlePageChange(p, s) { pagination.page = p; pagination.size = s; loadCurrent() }

async function loadPlans() {
  const result = await getTakingPlanByPage({ pageNum: pagination.page, pageSize: pagination.size })
  // 范围列预览：详情接口才带 scopeList，列表用懒加载补第一页的
  planData.value = result.data.list
  pagination.total = result.data.total
  for (const row of planData.value) {
    getTakingPlanById(row.planId).then(r => {
      row.scopePreview = (r.data.scopeList || [])
        .map(s => `${s.scopeType === 'WAREHOUSE' ? '仓' : s.scopeType === 'AREA' ? '区' : '类'}:${s.scopeValueName}`)
    })
  }
}

async function loadTakings() {
  const result = await getTakingByPage({ pageNum: pagination.page, pageSize: pagination.size })
  takingData.value = result.data.list
  pagination.total = result.data.total
}

// ==================== 计划 ====================
const planDialogVisible = ref(false)
const isPlanEdit = ref(false)
const planFormRef = ref(null)
const planForm = reactive({
  planId: null, planName: '', takingType: 'PART',
  startTime: null, endTime: null, blindFlag: 'N', remark: '', scopeList: []
})
const planRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  takingType: [{ required: true, message: '请选择盘点类型', trigger: 'change' }]
}

const warehouseOptions = ref([])
const locationTree = ref([])
const itemTypeOptions = ref([])

function emptyScope() {
  return { scopeType: 'WAREHOUSE', scopeValueId: null, scopeValueCode: '', scopeValueName: '' }
}

/** 范围对象候选项：类型切换后给对应数据源（下拉一律走后端接口，不硬编码） */
function scopeOptions(type) {
  if (type === 'WAREHOUSE') {
    return warehouseOptions.value.map(w => ({ id: w.warehouseId, label: w.warehouseName }))
  }
  if (type === 'AREA') {
    return locationTree.value
      .filter(n => n.locationType === 'AREA')
      .map(a => ({ id: a.locationId, label: `${a.locationCode} ${a.locationName}` }))
  }
  return itemTypeOptions.value.map(t => ({ id: t.itemTypeId, label: t.itemTypeName }))
}

function onScopeValueChange(scope) {
  const opt = scopeOptions(scope.scopeType).find(o => o.id === scope.scopeValueId)
  if (opt) {
    const row = scope.scopeType === 'WAREHOUSE'
      ? warehouseOptions.value.find(w => w.warehouseId === opt.id)
      : scope.scopeType === 'AREA'
        ? locationTree.value.find(n => n.locationId === opt.id)
        : itemTypeOptions.value.find(t => t.itemTypeId === opt.id)
    scope.scopeValueCode = row.warehouseCode || row.locationCode || row.itemTypeCode || ''
    scope.scopeValueName = row.warehouseName || row.locationName || row.itemTypeName || ''
  }
}

function handleAddPlan() {
  isPlanEdit.value = false
  resetPlanForm()
  planDialogVisible.value = true
}

async function handleEditPlan(row) {
  const result = await getTakingPlanById(row.planId)
  const d = result.data
  resetPlanForm()
  Object.assign(planForm, {
    planId: d.planId, planName: d.planName, takingType: d.takingType,
    startTime: d.startTime, endTime: d.endTime, blindFlag: d.blindFlag,
    remark: d.remark, scopeList: d.scopeList && d.scopeList.length ? d.scopeList : [emptyScope()]
  })
  isPlanEdit.value = true
  planDialogVisible.value = true
}

function resetPlanForm() {
  Object.assign(planForm, {
    planId: null, planName: '', takingType: 'PART',
    startTime: null, endTime: null, blindFlag: 'N', remark: '', scopeList: [emptyScope()]
  })
}

async function handlePlanSubmit() {
  planFormRef.value.validate(async (valid) => {
    if (!valid) return
    const validScopes = planForm.scopeList.filter(s => s.scopeValueId)
    if (!validScopes.length) {
      ElMessage.warning('至少要圈一个有效的盘点范围'); return
    }
    submitLoading.value = true
    try {
      const body = { ...planForm, scopeList: validScopes }
      if (isPlanEdit.value) {
        await updateTakingPlan(body)
        ElMessage.success('保存成功')
      } else {
        await createTakingPlan(body)
        ElMessage.success('计划已创建，编号由系统生成')
      }
      planDialogVisible.value = false
      loadPlans()
    } finally {
      submitLoading.value = false
    }
  })
}

function handlePlanDialogClose() { planFormRef.value?.resetFields() }

function handleDeletePlan(row) {
  ElMessageBox.confirm(`确定删除计划「${row.planName}」？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteTakingPlan(row.planId)
      ElMessage.success('删除成功')
      loadPlans()
    }).catch(() => {})
}

// ==================== 生成 / 录入 / 过账 / 差异 ====================
const currentTaking = ref(null)
const countDialogVisible = ref(false)
const countLines = ref([])
const isBlind = ref(false)
const diffVisible = ref(false)
const diffLines = ref([])

function handleGenerate(row) {
  ElMessageBox.confirm(
    `按计划范围展开库存行生成盘点单（带生成那一刻的账面数量）。确认生成？`,
    '生成盘点单', { type: 'info' }
  ).then(async () => {
    const result = await generateTaking(row.planId)
    ElMessage.success(`盘点单 ${result.data.takingCode} 已生成，共 ${result.data.lineList?.length ?? '若干'} 行`)
    loadPlans()
  }).catch(() => {})
}

async function openCount(row) {
  const result = await getTakingById(row.takingId)
  currentTaking.value = result.data
  isBlind.value = result.data.blind === true
  // 盲盘时后端已把账面/差异剥掉；录入初始值：已录的带原值，未录的空着
  countLines.value = (result.data.lineList || []).map(l => ({
    ...l, takingQuantity: l.takingStatus === 'CONFIRMED' ? l.takingQuantity : undefined
  }))
  countDialogVisible.value = true
}

async function handleSaveCounts() {
  const payload = countLines.value
    .filter(l => l.takingQuantity !== undefined && l.takingQuantity !== null)
    .map(l => ({ lineId: l.lineId, takingQuantity: l.takingQuantity }))
  if (!payload.length) {
    ElMessage.warning('没有要保存的实盘数据'); return
  }
  submitLoading.value = true
  try {
    await saveTakingQuantities(currentTaking.value.takingId, payload)
    ElMessage.success('实盘已保存（差异由系统按账面自动计算）')
    countDialogVisible.value = false
    loadTakings()
  } finally {
    submitLoading.value = false
  }
}

function handlePost(row) {
  ElMessageBox.confirm(
    '过账后盘盈/盘亏差异将写入库存与流水（一个事务，不可撤销）。确认过账？',
    '盘点过账', { confirmButtonText: '确认过账', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await postTaking(row.takingId)
    ElMessage.success('过账完成，差异已进账')
    loadTakings()
  }).catch(() => {})
}

async function openDiff(row) {
  const result = await getTakingDiff(row.takingId)
  diffLines.value = result.data || []
  diffVisible.value = true
}

onMounted(async () => {
  loadPlans()
  getWarehouseByPage({ pageNum: 1, pageSize: 100 }).then(r => { warehouseOptions.value = r.data.list || [] })
  getItemTypeByPage({ pageNum: 1, pageSize: 200 }).then(r => { itemTypeOptions.value = (r.data.list || []) })
  getLocationTree().then(r => { locationTree.value = flattenTree(r.data || []) })
})

/** 树拍平（库区选择只需要 AREA 层） */
function flattenTree(nodes, out = []) {
  for (const n of nodes) {
    out.push(n)
    if (n.children && n.children.length) flattenTree(n.children, out)
  }
  return out
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
