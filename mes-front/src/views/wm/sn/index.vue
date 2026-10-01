<template>
  <div class="wm-sn">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>SN 序列号管理（一物一码）</span>
          <div>
            <el-button type="primary" :icon="Plus" @click="genVisible = true">批量生成</el-button>
            <el-button :icon="Search" @click="scanVisible = true">SN 追溯</el-button>
          </div>
        </div>
      </template>

      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 10px"
                title="SN 是最小个体编码：一个成品一台一个号。生成后先绑定工单/批次，生产过程中每过一道工序写一条 pro_sn_process，就能从 SN 追溯完整制程" />

      <el-form :inline="true" class="search-form">
        <el-form-item label="SN 编码">
          <el-input v-model="query.snKeyword" placeholder="模糊查询" clearable />
        </el-form-item>
        <el-form-item label="物料">
          <el-select v-model="query.itemId" filterable clearable placeholder="全部" style="width: 200px">
            <el-option v-for="i in itemOptions" :key="i.itemId"
                       :label="`${i.itemCode} ${i.itemName}`" :value="i.itemId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 130px">
            <el-option v-for="d in statusOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border stripe>
        <el-table-column prop="snCode" label="SN 编码" min-width="170" />
        <el-table-column prop="itemCode" label="物料编码" width="130" />
        <el-table-column prop="itemName" label="物料名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="batchCode" label="批次" width="140">
          <template #default="{ row }">{{ row.batchCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="workorderCode" label="生产工单" width="150">
          <template #default="{ row }">{{ row.workorderCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="160" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openTrace(row)">追溯</el-button>
            <el-button link type="danger" :disabled="row.status !== 'IN_STOCK'"
                       @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
                       :page-sizes="[10, 20, 50]" :total="pagination.total"
                       layout="total, sizes, prev, pager, next" background
                       @change="handlePageChange" />
      </div>
    </el-card>

    <!-- 批量生成：选产品 + 工单 + 数量 -->
    <el-dialog v-model="genVisible" title="批量生成 SN" width="560px" :close-on-click-modal="false">
      <el-form ref="genFormRef" :model="genForm" :rules="genRules" label-width="100px">
        <el-form-item label="产品" prop="itemId">
          <el-select v-model="genForm.itemId" filterable style="width: 100%"
                     @change="handleGenItemChange">
            <el-option v-for="i in itemOptions" :key="i.itemId"
                       :label="`${i.itemCode} ${i.itemName}`" :value="i.itemId" />
          </el-select>
        </el-form-item>
        <el-form-item label="生产工单" prop="workorderId">
          <el-select v-model="genForm.workorderId" filterable clearable style="width: 100%"
                     @change="handleGenWorkorderChange">
            <el-option v-for="w in workorderOptions" :key="w.workorderId"
                       :label="`${w.workorderCode}（${w.productName} ×${w.quantity}）`"
                       :value="w.workorderId" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次" prop="batchCode">
          <el-select v-model="genForm.batchCode" filterable style="width: 100%"
                     :disabled="!genForm.itemId" placeholder="先选产品，再选它的批次">
            <el-option v-for="b in genBatchOptions" :key="b.batchId"
                       :label="b.batchCode" :value="b.batchCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="生成数量" prop="count">
          <el-input-number v-model="genForm.count" :min="1" :max="500" />
          <span style="margin-left: 8px; color: var(--el-text-color-secondary)">1 ~ 500 个</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="genForm.remark" placeholder="默认带「演示数据」前缀便于回滚" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" :loading="genLoading" @click="handleGenerate">生成</el-button>
      </template>
    </el-dialog>

    <!-- 追溯：按 SN 编码查 -->
    <el-dialog v-model="scanVisible" title="SN 追溯" width="680px">
      <el-input v-model="scanSnCode" placeholder="输入 SN 编码（或扫码枪扫入）后回车"
                @keyup.enter="handleSearchTrace">
        <template #append>
          <el-button type="primary" @click="handleSearchTrace">追溯</el-button>
        </template>
      </el-input>

      <el-table v-if="traceRows.length" :data="traceRows" border style="margin-top: 16px">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="processName" label="工序" min-width="130" />
        <el-table-column prop="workstationName" label="工作站" min-width="130">
          <template #default="{ row }">{{ row.workstationName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100">
          <template #default="{ row }">{{ row.operatorName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="processTime" label="过站时间" width="160" />
      </el-table>
      <el-empty v-else-if="traceSearched" description="该 SN 暂无过站记录（A 线报工写入 pro_sn_process 后即可追溯）" />

      <template #footer>
        <el-button @click="scanVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

import { getSnByPage, generateSn, deleteSnById, traceSn } from '@/api/wm/sn.js'
import { getItemByPage } from '@/api/md/item.js'
import { getWorkorderByPage } from '@/api/pro/workorder.js'
import { getBatchByPage } from '@/api/wm/batch.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'

const list = ref([])
const query = reactive({ snKeyword: '', itemId: null, status: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const itemOptions = ref([])
const statusOptions = ref([])

// ⭐ el-input-number 陷阱：max 必须恒 ≥ min，这里 count 是纯入参无异步 max，直接静态 1~500

async function loadList() {
  const result = await getSnByPage({ ...query, pageNum: pagination.page, pageSize: pagination.size })
  list.value = result.data.list
  pagination.total = result.data.total
}
function handleSearch() { pagination.page = 1; loadList() }
function handlePageChange(p, s) { pagination.page = p; pagination.size = s; loadList() }

// ==================== 批量生成 ====================
const genVisible = ref(false)
const genLoading = ref(false)
const genFormRef = ref(null)
const workorderOptions = ref([])
const genBatchOptions = computed(() =>
  batchCache.value.filter(b => b.itemId === genForm.itemId))
const batchCache = ref([])
const genForm = reactive({ itemId: null, workorderId: null, batchCode: '', count: 10, remark: '' })
const genRules = {
  itemId: [{ required: true, message: '请选择产品' }],
  batchCode: [{ required: true, message: '请选择批次（生成即绑定批次）' }],
  count: [{ required: true, message: '请填写生成数量' }]
}

function handleGenItemChange() { genForm.batchCode = '' }

/** 选了工单自动带出产品（工单上就有 product_id），顺手把该产品的批次筛出来 */
function handleGenWorkorderChange(workorderId) {
  if (!workorderId) return
  const w = workorderOptions.value.find(x => x.workorderId === workorderId)
  if (w && w.productId) {
    genForm.itemId = w.productId
    genForm.batchCode = ''
  }
}

async function handleGenerate() {
  genFormRef.value.validate(async (valid) => {
    if (!valid) return
    genLoading.value = true
    try {
      const result = await generateSn({ ...genForm, remark: genForm.remark || '演示数据-SN' })
      ElMessage.success(`已生成 ${result.data.length} 个 SN，首个：${result.data[0].snCode}`)
      genVisible.value = false
      loadList()
    } finally {
      genLoading.value = false
    }
  })
}

// ==================== 追溯 / 删除 ====================
const scanVisible = ref(false)
const scanSnCode = ref('')
const traceRows = ref([])
const traceSearched = ref(false)

async function handleSearchTrace() {
  if (!scanSnCode.value.trim()) { ElMessage.warning('请输入 SN 编码'); return }
  // 追溯接口按 snId 查，先模糊搜出这条 SN
  const result = await getSnByPage({ snCode: scanSnCode.value.trim(), pageNum: 1, pageSize: 1 })
  const hit = (result.data.list || [])[0]
  if (!hit) { ElMessage.warning('没有找到该 SN'); return }
  await openTrace(hit)
  scanVisible.value = true
}

async function openTrace(row) {
  traceSearched.value = true
  scanSnCode.value = row.snCode
  const result = await traceSn(row.snId)
  traceRows.value = result.data || []
  scanVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除 SN「${row.snCode}」？只有「在库」状态的 SN 允许删除。`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteSnById(row.snId)
      ElMessage.success('删除成功')
      loadList()
    }).catch(() => {})
}

const statusTagType = s => ({ IN_STOCK: 'primary', SHIPPED: 'success', FROZEN: 'info' }[s] || 'info')
const statusLabel = s => {
  const hit = statusOptions.value.find(d => d.dictValue === s)
  return hit ? hit.dictLabel : s
}

onMounted(async () => {
  loadList()
  const [items, works, batches, dict] = await Promise.all([
    getItemByPage({ pageNum: 1, pageSize: 500 }),
    getWorkorderByPage({ pageNum: 1, pageSize: 200 }),
    getBatchByPage({ pageNum: 1, pageSize: 500 }),
    getDictDataListByType('wm_sn_status')
  ])
  itemOptions.value = items.data.list || []
  workorderOptions.value = works.data.list || []
  batchCache.value = batches.data.list || []
  statusOptions.value = dict.data || []
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 10px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
