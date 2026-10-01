<template>
  <div class="wm-barcode">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="条码规则" name="config" />
            <el-tab-pane label="条码列表" name="barcode" />
          </el-tabs>
          <div>
            <el-button v-if="activeTab === 'config'" type="primary" :icon="Plus"
                       @click="handleAddConfig">新增规则</el-button>
            <template v-else>
              <el-button type="primary" @click="openGenDialog">生成条码</el-button>
              <el-button type="warning" :icon="Refresh"
                         @click="scanVisible = true">扫码解析</el-button>
            </template>
          </div>
        </div>
      </template>

      <!-- ==================== 条码规则 ==================== -->
      <template v-if="activeTab === 'config'">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 10px"
                  title="一条规则管一种码：模板里的 {itemCode} {batchCode} {packageCode} 是占位符，生成时换成真实值；同一类型只能启用一条规则" />
        <el-table :data="configData" border stripe>
          <el-table-column prop="configId" label="ID" width="70" />
          <el-table-column label="类型" width="100" align="center">
            <template #default="{ row }">{{ typeName(row.barcodeType) }}</template>
          </el-table-column>
          <el-table-column prop="barcodeFormat" label="码制" width="110" />
          <el-table-column prop="contentFormat" label="内容模板" min-width="180" />
          <el-table-column prop="contentExample" label="示例" min-width="180" />
          <el-table-column label="启用" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'" size="small">
                {{ row.enableFlag === 'Y' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEditConfig(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteConfig(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- ==================== 条码列表 ==================== -->
      <template v-else>
        <el-form :inline="true" class="search-form">
          <el-form-item label="条码内容">
            <el-input v-model="barcodeQuery.contentKeyword" placeholder="模糊查询" clearable />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="barcodeQuery.barcodeType" placeholder="全部" clearable style="width: 120px">
              <el-option label="物料码" value="ITEM" />
              <el-option label="批次码" value="BATCH" />
              <el-option label="箱码" value="PACKAGE" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadBarcodes">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="barcodeData" border stripe>
          <el-table-column prop="barcodeId" label="ID" width="70" />
          <el-table-column prop="barcodeContent" label="条码内容" min-width="180" />
          <el-table-column label="类型" width="100" align="center">
            <template #default="{ row }">{{ typeName(row.barcodeType) }}</template>
          </el-table-column>
          <el-table-column prop="bizCode" label="指向对象" min-width="150" />
          <el-table-column prop="bizName" label="对象名称" min-width="150" show-overflow-tooltip />
          <el-table-column label="图片" width="130" align="center">
            <template #default="{ row }">
              <el-image v-if="row.barcodeUrl" :src="row.barcodeUrl"
                        :preview-src-list="[row.barcodeUrl]"
                        preview-teleported style="height: 40px" fit="contain" />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="printOne(row)">打印</el-button>
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

    <!-- 规则弹窗 -->
    <el-dialog v-model="configDialogVisible" :title="isConfigEdit ? '编辑规则' : '新增规则'"
               width="620px" :close-on-click-modal="false">
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-width="100px">
        <el-form-item label="条码类型" prop="barcodeType">
          <el-select v-model="configForm.barcodeType" style="width: 100%">
            <el-option label="物料码" value="ITEM" />
            <el-option label="批次码" value="BATCH" />
            <el-option label="箱码" value="PACKAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="码制" prop="barcodeFormat">
          <el-select v-model="configForm.barcodeFormat" style="width: 100%">
            <el-option label="CODE128（一维码）" value="CODE128" />
            <el-option label="QR_CODE（二维码）" value="QR_CODE" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容模板" prop="contentFormat">
          <el-input v-model="configForm.contentFormat" placeholder="如 IT-{itemCode}" />
        </el-form-item>
        <el-form-item label="示例">
          <el-input v-model="configForm.contentExample" placeholder="给人看的示例，不参与逻辑" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="configForm.enableFlag" active-value="Y" inactive-value="N" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="configForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfigSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 生成条码：按类型选对象，一条一个码（任务 53） -->
    <el-dialog v-model="genVisible" title="生成条码" width="520px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 14px"
                title="先在「条码规则」里配好该类型的启用规则；生成时后端按模板拼内容、出图片，同一对象重复生成会复用已有条码" />
      <el-form label-width="90px">
        <el-form-item label="条码类型" required>
          <el-select v-model="genForm.barcodeType" style="width: 100%" @change="genForm.bizId = null">
            <el-option label="物料码" value="ITEM" />
            <el-option label="批次码" value="BATCH" />
            <el-option label="箱码" value="PACKAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="指向对象" required>
          <el-select v-model="genForm.bizId" filterable style="width: 100%"
                     :loading="genLoading" placeholder="先选类型再选对象">
            <el-option v-for="o in genBizOptions" :key="o.id"
                       :label="o.label" :value="o.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" :loading="genLoading" @click="handleGenerate">生成</el-button>
      </template>
    </el-dialog>

    <!-- 扫码解析 -->
    <el-dialog v-model="scanVisible" title="扫码解析" width="560px">
      <el-input v-model="scanContent" placeholder="扫一下，或手动输入条码内容后回车"
                @keyup.enter="handleParse">
        <template #append>
          <el-button type="primary" @click="handleParse">解析</el-button>
        </template>
      </el-input>
      <el-descriptions v-if="parsed" :column="2" border style="margin-top: 16px">
        <el-descriptions-item label="类型" :span="2">{{ typeName(parsed.barcodeType) }}</el-descriptions-item>
        <el-descriptions-item label="条码内容" :span="2">{{ parsed.barcodeContent }}</el-descriptions-item>
        <el-descriptions-item label="指向对象">{{ parsed.bizCode }}</el-descriptions-item>
        <el-descriptions-item label="对象名称">{{ parsed.bizName }}</el-descriptions-item>
      </el-descriptions>
      <el-alert v-if="parseError" type="error" :closable="false" show-icon style="margin-top: 16px"
                :title="parseError" />
      <template #footer>
        <el-button @click="scanVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'

import {
  getBarcodeConfigByPage, createBarcodeConfig, updateBarcodeConfig, deleteBarcodeConfig,
  getBarcodeByPage, parseBarcode, generateBarcode
} from '@/api/wm/barcode.js'
import { getItemByPage } from '@/api/md/item.js'
import { getBatchByPage } from '@/api/wm/batch.js'
import { getPackageByPage } from '@/api/wm/package.js'

const activeTab = ref('config')
const pagination = reactive({ page: 1, size: 10, total: 0 })
const configData = ref([])
const barcodeData = ref([])
const barcodeQuery = reactive({ contentKeyword: '', barcodeType: null })

const TYPE_LABELS = { ITEM: '物料码', BATCH: '批次码', PACKAGE: '箱码' }
const typeName = t => TYPE_LABELS[t] || t

function handleTabChange() { pagination.page = 1; activeTab.value === 'config' ? loadConfigs() : loadBarcodes() }
function handlePageChange(p, s) { pagination.page = p; pagination.size = s; handleTabChange() }

async function loadConfigs() {
  const result = await getBarcodeConfigByPage({ pageNum: pagination.page, pageSize: pagination.size })
  configData.value = result.data.list
  pagination.total = result.data.total
}
async function loadBarcodes() {
  const result = await getBarcodeByPage({ ...barcodeQuery, pageNum: pagination.page, pageSize: pagination.size })
  barcodeData.value = result.data.list
  pagination.total = result.data.total
}

// ==================== 规则 ====================
const configDialogVisible = ref(false)
const isConfigEdit = ref(false)
const configFormRef = ref(null)
const configForm = reactive({
  configId: null, barcodeType: 'ITEM', barcodeFormat: 'CODE128',
  contentFormat: '', contentExample: '', enableFlag: 'Y', remark: ''
})
const configRules = {
  barcodeType: [{ required: true, message: '请选择类型' }],
  barcodeFormat: [{ required: true, message: '请选择码制' }],
  contentFormat: [{ required: true, message: '请输入内容模板' }]
}

function handleAddConfig() {
  isConfigEdit.value = false
  Object.assign(configForm, {
    configId: null, barcodeType: 'ITEM', barcodeFormat: 'CODE128',
    contentFormat: '', contentExample: '', enableFlag: 'Y', remark: ''
  })
  configDialogVisible.value = true
}

function handleEditConfig(row) {
  isConfigEdit.value = true
  Object.assign(configForm, row)
  configDialogVisible.value = true
}

async function handleConfigSubmit() {
  configFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (isConfigEdit.value) {
      await updateBarcodeConfig(configForm)
      ElMessage.success('保存成功')
    } else {
      await createBarcodeConfig(configForm)
      ElMessage.success('规则已创建')
    }
    configDialogVisible.value = false
    loadConfigs()
  })
}

function handleDeleteConfig(row) {
  ElMessageBox.confirm('确定删除该条码规则？', '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteBarcodeConfig(row.configId)
      ElMessage.success('删除成功')
      loadConfigs()
    }).catch(() => {})
}

// ==================== 生成条码 ====================
const genVisible = ref(false)
const genLoading = ref(false)
const genForm = reactive({ barcodeType: 'ITEM', bizId: null })
const itemOptions = ref([])
const batchOptions = ref([])
const packageOptions = ref([])

/** 按类型给候选：物料码选物料、批次码选批次、箱码选箱 */
const genBizOptions = computed(() => {
  if (genForm.barcodeType === 'ITEM') {
    return itemOptions.value.map(i => ({ id: i.itemId, label: `${i.itemCode} ${i.itemName}` }))
  }
  if (genForm.barcodeType === 'BATCH') {
    return batchOptions.value.map(b => ({ id: b.batchId, label: `${b.batchCode}（${b.itemCode || ''}）` }))
  }
  return packageOptions.value.map(p => ({ id: p.packageId, label: `${p.packageCode}（${p.statusName || p.status}）` }))
})

/** 弹窗打开时懒加载三类候选（unref 解包陷阱：模板传来的已是数组，这里统一存 ref 再 unref） */
async function openGenDialog() {
  genVisible.value = true
  genForm.bizId = null
  genLoading.value = true
  try {
    const [items, batches, packages] = await Promise.all([
      getItemByPage({ pageNum: 1, pageSize: 500 }),
      getBatchByPage({ pageNum: 1, pageSize: 500 }),
      getPackageByPage({ pageNum: 1, pageSize: 500 })
    ])
    itemOptions.value = items.data.list || []
    batchOptions.value = batches.data.list || []
    packageOptions.value = packages.data.list || []
  } finally {
    genLoading.value = false
  }
}

async function handleGenerate() {
  if (!genForm.bizId) { ElMessage.warning('请先选择指向对象'); return }
  genLoading.value = true
  try {
    const result = await generateBarcode(genForm.barcodeType, genForm.bizId)
    ElMessage.success(`条码已生成：${result.data.barcodeContent}`)
    genVisible.value = false
    loadBarcodes()
  } finally {
    genLoading.value = false
  }
}

// ==================== 扫码解析 / 打印 ====================
const scanVisible = ref(false)
const scanContent = ref('')
const parsed = ref(null)
const parseError = ref('')

async function handleParse() {
  parsed.value = null
  parseError.value = ''
  try {
    const result = await parseBarcode(scanContent.value)
    parsed.value = result.data
  } catch (e) {
    parseError.value = (e && e.message) || '解析失败'
  }
}

/** 打印：开一个新窗口排图片，浏览器 Ctrl+P 出标签（任务表允许的简化口径） */
function printOne(row) {
  const win = window.open('', '_blank')
  win.document.write(`
    <html><head><title>条码打印 - ${row.barcodeContent}</title>
    <style>body{font-family:sans-serif;text-align:center} .label{margin:16px auto}
    .code{font-size:12px;margin-top:4px} .name{font-size:14px}</style></head>
    <body><div class="label">
      <div class="name">${row.bizName || ''}</div>
      <img src="${row.barcodeUrl}" />
      <div class="code">${row.barcodeContent}</div>
    </div></body></html>`)
  win.document.close()
  win.print()
}

onMounted(loadConfigs)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 10px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
