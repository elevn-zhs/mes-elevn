<template>
  <div class="wm-doc">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 工具按钮 ============ -->
      <template #header>
        <div class="card-header">
          <span>出入库单据</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增单据</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="单据类型">
          <el-select v-model="queryForm.docType" style="width: 170px" @change="handleSearch">
            <el-option v-for="d in docTypeOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="单据编号">
          <el-input v-model="queryForm.docCode" placeholder="请输入单据编号" clearable />
        </el-form-item>
        <el-form-item label="单据状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="d in statusOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="queryForm.warehouseId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="w in warehouseOptions" :key="w.warehouseId"
                       :label="w.warehouseName" :value="w.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="[new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 当前类型的出入库方向提示：方向由单据类型决定，页面上不给选 -->
      <el-alert
        v-if="ioFlagTip"
        :type="ioFlagTipType"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
        :title="ioFlagTip"
      />

      <!-- ============ 批量操作工具栏 ============ -->
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
        <el-table-column type="selection" width="50" :selectable="row => row.status === 'PREPARE'" />
        <el-table-column prop="docId" label="ID" width="70" />
        <el-table-column prop="docCode" label="单据编号" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="handleDetail(row)">{{ row.docCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="单据类型" width="120">
          <template #default="{ row }">{{ row.docTypeName || dictLabel(docTypeOptions, row.docType) }}</template>
        </el-table-column>
        <el-table-column prop="bizDate" label="业务日期" width="160" />
        <el-table-column label="仓库" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.warehouseName || '-' }}
            <span v-if="row.toWarehouseName" class="to-target"> → {{ row.toWarehouseName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="往来对象" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.partnerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="生产工单" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.workorderCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="dictTagType(statusOptions, row.status)" size="small">
              {{ dictLabel(statusOptions, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PREPARE'" link type="primary"
                       :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'PREPARE'" link type="warning"
                       :icon="Finished" @click="handleExecute(row)">过账</el-button>
            <el-button v-if="row.status === 'PREPARE'" link type="info"
                       @click="handleCancel(row)">取消</el-button>
            <el-button v-if="row.docType === 'TRANSFER' && row.status === 'CONFIRMED'" link type="success"
                       @click="handleFinish(row)">送达确认</el-button>
            <el-button v-if="row.status === 'PREPARE'" link type="danger"
                       :icon="Delete" @click="handleDelete(row)">删除</el-button>
            <el-tooltip v-if="row.status === 'CONFIRMED'" content="已过账的单据不能再改，如需反悔请开一张反方向的单据" placement="top">
              <span class="locked-tip">已锁定</span>
            </el-tooltip>
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
    </el-card>

    <!-- ============ 新增/编辑单据弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1080px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="docFormRef" :model="docForm" :rules="docRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="单据编号">
              <el-input v-model="docForm.docCode" disabled placeholder="保存后按编码规则自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单据类型" prop="docType">
              <el-select v-model="docForm.docType" :disabled="isEdit" style="width: 100%"
                         @change="handleDocTypeChange">
                <el-option v-for="d in docTypeOptions" :key="d.dictValue"
                           :label="d.dictLabel" :value="d.dictValue" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务日期" prop="bizDate">
              <el-date-picker v-model="docForm.bizDate" type="datetime"
                              value-format="YYYY-MM-DD HH:mm:ss" placeholder="默认当前时间"
                              style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="docForm.warehouseId" placeholder="请选择仓库" filterable
                         style="width: 100%" @change="handleWarehouseChange">
                <el-option v-for="w in warehouseOptions" :key="w.warehouseId"
                           :label="`${w.warehouseCode} ${w.warehouseName}`" :value="w.warehouseId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库区">
              <el-select v-model="docForm.areaId" placeholder="可选，默认由库位带出" clearable
                         style="width: 100%">
                <el-option v-for="a in areaOptionsFor(docForm.warehouseId)" :key="a.locationId"
                           :label="a.locationName" :value="a.locationId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库位">
              <el-select v-model="docForm.locationId" placeholder="可选，不填则由后端按库存匹配" clearable
                         style="width: 100%">
                <el-option v-for="l in locationOptionsFor(docForm.warehouseId)" :key="l.locationId"
                           :label="l.locationName" :value="l.locationId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 调拨：目标仓 -->
        <el-row v-if="isTransfer" :gutter="16">
          <el-col :span="8">
            <el-form-item label="目标仓库" prop="toWarehouseId">
              <el-select v-model="docForm.toWarehouseId" placeholder="请选择目标仓库" filterable
                         style="width: 100%">
                <el-option v-for="w in warehouseOptions" :key="w.warehouseId"
                           :label="`${w.warehouseCode} ${w.warehouseName}`" :value="w.warehouseId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标库区">
              <el-select v-model="docForm.toAreaId" placeholder="可选" clearable style="width: 100%">
                <el-option v-for="a in areaOptionsFor(docForm.toWarehouseId)" :key="a.locationId"
                           :label="a.locationName" :value="a.locationId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标库位">
              <el-select v-model="docForm.toLocationId" placeholder="可选" clearable style="width: 100%">
                <el-option v-for="l in locationOptionsFor(docForm.toWarehouseId)" :key="l.locationId"
                           :label="l.locationName" :value="l.locationId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 供应商类单据 -->
        <el-row v-if="showVendor" :gutter="16">
          <el-col :span="8">
            <el-form-item label="供应商">
              <el-select v-model="docForm.partnerId" placeholder="请选择供应商" clearable filterable
                         style="width: 100%" @change="handlePartnerChange">
                <el-option v-for="v in vendorOptions" :key="v.vendorId"
                           :label="v.vendorName" :value="v.vendorId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采购订单号">
              <el-input v-model="docForm.poCode" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 客户类单据 -->
        <el-row v-if="showClient" :gutter="16">
          <el-col :span="8">
            <el-form-item label="客户">
              <el-select v-model="docForm.partnerId" placeholder="请选择客户" clearable filterable
                         style="width: 100%" @change="handlePartnerChange">
                <el-option v-for="c in clientOptions" :key="c.clientId"
                           :label="c.clientName" :value="c.clientId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="销售订单号">
              <el-input v-model="docForm.soCode" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 生产相关单据 -->
        <el-row v-if="showWorkorder" :gutter="16">
          <el-col :span="12">
            <el-form-item label="生产工单" prop="workorderId">
              <el-select v-model="docForm.workorderId" placeholder="请选择生产工单" clearable filterable
                         style="width: 100%" @change="handleWorkorderChange">
                <el-option v-for="w in workorderOptions" :key="w.workorderId"
                           :label="`${w.workorderCode} ${w.workorderName || ''}`" :value="w.workorderId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="showTask" :span="12">
            <el-form-item label="生产任务" prop="taskId">
              <el-select v-model="docForm.taskId" placeholder="请选择生产任务" clearable filterable
                         style="width: 100%" @change="handleTaskChange">
                <el-option v-for="t in taskOptions" :key="t.taskId"
                           :label="`${t.taskCode} ${t.taskName || ''}`" :value="t.taskId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="docForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <!-- ============ 明细行 ============ -->
      <el-divider content-position="left">
        明细行（{{ docForm.lineList.length }} 行）
      </el-divider>

      <div class="line-toolbar">
        <el-button type="primary" plain size="small" :icon="Plus" @click="handleAddLine">添加一行</el-button>
        <span class="line-tip">
          出库/调拨类不填库位时，后端按「物料 + 批次」在仓库里先进先出自动找货；
          入库类不填库位时，入到单据头上选的库位
        </span>
      </div>

      <el-table :data="docForm.lineList" border size="small" max-height="300">
        <el-table-column type="index" label="行号" width="60" align="center" />
        <el-table-column label="物料" min-width="220">
          <template #default="{ row }">
            <el-select v-model="row.itemId" placeholder="请选择物料" filterable size="small"
                       style="width: 100%" @change="handleLineItemChange(row)">
              <el-option v-for="i in itemOptions" :key="i.itemId"
                         :label="`${i.itemCode} ${i.itemName}`" :value="i.itemId" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="批次" min-width="180">
          <template #default="{ row }">
            <el-select v-model="row.batchId" placeholder="不启用批次管理的物料可不填"
                       filterable clearable size="small" style="width: 100%">
              <el-option v-for="b in batchOptionsFor(row.itemId)" :key="b.batchId"
                         :label="b.batchCode" :value="b.batchId" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="0.000001" :precision="2"
                             size="small" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="库位" min-width="160">
          <template #default="{ row }">
            <el-select v-model="row.locationId" placeholder="默认单据库位" clearable
                       size="small" style="width: 100%">
              <el-option v-for="l in locationOptionsFor(row.warehouseId || docForm.warehouseId)"
                         :key="l.locationId" :label="l.locationName" :value="l.locationId" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="140">
          <template #default="{ row }">
            <el-input v-model="row.remark" size="small" placeholder="选填" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" :icon="Delete" @click="handleRemoveLine($index)" />
          </template>
        </el-table-column>
      </el-table>

      <el-alert
        style="margin-top: 12px"
        type="warning"
        :closable="false"
        show-icon
        title="保存后的单据处于【待过账】状态，此时还可以改；一旦点了过账，库存就真的变了，单据也会被锁死不能再改。"
      />

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 详情弹窗 ============ -->
    <el-dialog v-model="detailVisible" title="单据详情" width="980px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="单据编号" :span="2">{{ detailData.docCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="dictTagType(statusOptions, detailData.status)" size="small">
            {{ dictLabel(statusOptions, detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="单据类型">
          {{ detailData.docTypeName || dictLabel(docTypeOptions, detailData.docType) }}
        </el-descriptions-item>
        <el-descriptions-item label="业务日期">{{ detailData.bizDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出入库方向">
          {{ dictLabel(ioFlagOptions, detailData.ioFlag) }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库">{{ detailData.warehouseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="库区">{{ detailData.areaName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="库位">{{ detailData.locationName || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.toWarehouseName" label="目标仓库">
          {{ detailData.toWarehouseName }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.toLocationName" label="目标库位">
          {{ detailData.toLocationName }}
        </el-descriptions-item>
        <el-descriptions-item label="往来对象">{{ detailData.partnerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产工单">{{ detailData.workorderCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产任务">{{ detailData.taskCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采购订单号">{{ detailData.poCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销售订单号">{{ detailData.soCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs style="margin-top: 14px">
        <el-tab-pane label="明细行">
          <el-table :data="detailData.lineList || []" border size="small" max-height="300">
            <el-table-column prop="lineNo" label="行号" width="60" align="center" />
            <el-table-column prop="itemCode" label="物料编码" min-width="130" />
            <el-table-column prop="itemName" label="物料名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="specification" label="规格型号" min-width="120" show-overflow-tooltip />
            <el-table-column prop="batchCode" label="批次" width="150" show-overflow-tooltip />
            <el-table-column prop="quantity" label="数量" width="100" align="right" />
            <el-table-column prop="unitName" label="单位" width="80" />
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>

        <el-tab-pane :label="`落位明细（${(detailData.detailList || []).length}）`">
          <el-alert
            v-if="detailData.status === 'PREPARE'"
            type="info"
            :closable="false"
            show-icon
            title="单据还没过账，所以还没有落位记录 —— 落位是过账时才产生的"
          />
          <el-table v-else :data="detailData.detailList || []" border size="small" max-height="300">
            <el-table-column prop="itemCode" label="物料编码" min-width="130" />
            <el-table-column prop="itemName" label="物料名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="batchCode" label="批次" width="150" show-overflow-tooltip />
            <el-table-column prop="quantity" label="数量" width="100" align="right" />
            <el-table-column prop="warehouseName" label="仓库" min-width="130" />
            <el-table-column prop="areaName" label="库区" min-width="110" />
            <el-table-column prop="locationName" label="库位" min-width="110" />
          </el-table>
          <el-empty v-if="detailData.status !== 'PREPARE' && !(detailData.detailList || []).length"
                    description="没有落位记录" :image-size="60" />
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Close, View, Finished } from '@element-plus/icons-vue'

import {
  getDocByPage,
  getDocById,
  createDoc,
  updateDoc,
  executeDoc,
  cancelDoc,
  finishDoc,
  deleteDoc,
  deleteDocBatch
} from '@/api/wm/doc.js'
import { getAllWarehouseList } from '@/api/wm/warehouse.js'
import { getLocationTree } from '@/api/wm/location.js'
import { getBatchByPage } from '@/api/wm/batch.js'
import { getItemByPage } from '@/api/md/item.js'
import { getVendorByPage } from '@/api/md/vendor.js'
import { getClientByPage } from '@/api/md/client.js'
import { getWorkorderByPage } from '@/api/pro/workorder.js'
import { getTaskListByWorkorder } from '@/api/pro/task.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'

// ==================== 单据类型分组（决定弹窗显示哪些字段） ====================
// 这些是"哪类单据需要哪些字段"的业务规则，与后端 checkBizRules 一一对应。
// 前端只管显示与提示，真正拦得住的是后端。

/** 有供应商的单据 */
const VENDOR_TYPES = ['ITEM_RECPT', 'OUTSOURCE_RECPT', 'RT_VENDOR']
/** 有客户的单据 */
const CLIENT_TYPES = ['PRODUCT_SALES', 'RT_SALES']
/** 会带生产工单的单据 */
const WORKORDER_TYPES = ['ISSUE', 'PRODUCT_PRODUCE', 'PRODUCT_RECPT',
  'RT_ISSUE', 'OUTSOURCE_ISSUE', 'OUTSOURCE_RECPT']
/** 工单必填的单据（与后端一致） */
const WORKORDER_REQUIRED_TYPES = ['PRODUCT_PRODUCE', 'PRODUCT_RECPT']
/** 会带生产任务的单据 */
const TASK_TYPES = ['ISSUE']

/** 出入库方向：仅用于页面提示，真实方向由后端按 docType 推导 */
const IO_FLAG_BY_TYPE = {
  ITEM_RECPT: 'I', PRODUCT_RECPT: 'I', PRODUCT_PRODUCE: 'I', RT_ISSUE: 'I',
  RT_SALES: 'I', OUTSOURCE_RECPT: 'I', MISC_RECPT: 'I',
  RT_VENDOR: 'O', ISSUE: 'O', ITEM_CONSUME: 'O',
  PRODUCT_SALES: 'O', OUTSOURCE_ISSUE: 'O', MISC_ISSUE: 'O',
  TRANSFER: 'T'
}

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  docType: 'ITEM_RECPT',
  docCode: '',
  status: null,
  warehouseId: null
})

const dateRange = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ==================== 数据源 ====================

const tableData = ref([])
const selectedRows = ref([])
const tableRef = ref(null)

const warehouseOptions = ref([])
const itemOptions = ref([])
const batchOptions = ref([])
const vendorOptions = ref([])
const clientOptions = ref([])
const workorderOptions = ref([])
const taskOptions = ref([])

/** 库区与库位：从树里拆出来，按下拉用 */
const areaList = ref([])
const locationList = ref([])

// 字典
const docTypeOptions = ref([])
const statusOptions = ref([])
const ioFlagOptions = ref([])

async function loadDict(type, target) {
  const result = await getDictDataListByType(type)
  target.value = result.data || []
}

function dictLabel(options, value, fallback) {
  const hit = unref(options).find(d => d.dictValue === value)
  return hit ? hit.dictLabel : (fallback || value || '—')
}

function dictTagType(options, value) {
  const hit = unref(options).find(d => d.dictValue === value)
  return hit ? (hit.listClass || 'info') : 'info'
}

/** 当前查询类型的方向提示 */
const ioFlagTip = computed(() => {
  const flag = IO_FLAG_BY_TYPE[queryForm.docType]
  const typeName = dictLabel(docTypeOptions, queryForm.docType, queryForm.docType)
  if (flag === 'I') return `${typeName}是【入库】类单据：过账后库存增加，会生成一条入库流水`
  if (flag === 'O') return `${typeName}是【出库】类单据：过账后库存减少，会生成一条出库流水；可用库存不足会被拒绝`
  if (flag === 'T') return `${typeName}是【调拨】类单据：过账后源仓减少、目标仓增加，生成一出一进配对的流水`
  return ''
})

const ioFlagTipType = computed(() => {
  const flag = IO_FLAG_BY_TYPE[queryForm.docType]
  if (flag === 'O') return 'warning'
  if (flag === 'T') return 'info'
  return 'success'
})

// ==================== 新增/编辑弹窗 ====================

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const docFormRef = ref(null)

const defaultLine = () => ({
  lineId: null,
  itemId: null,
  batchId: null,
  batchCode: '',
  quantity: 1,
  warehouseId: null,
  locationId: null,
  areaId: null,
  remark: ''
})

const defaultDoc = () => ({
  docId: null,
  docCode: '',
  docType: 'ITEM_RECPT',
  bizDate: null,
  warehouseId: null,
  areaId: null,
  locationId: null,
  toWarehouseId: null,
  toAreaId: null,
  toLocationId: null,
  partnerType: null,
  partnerId: null,
  partnerCode: '',
  partnerName: '',
  partnerNick: '',
  poCode: '',
  soCode: '',
  workorderId: null,
  workorderCode: '',
  workorderName: '',
  taskId: null,
  taskCode: '',
  taskName: '',
  remark: '',
  lineList: [defaultLine()]
})

const docForm = reactive(defaultDoc())

const dialogTitle = computed(() => isEdit.value ? '编辑单据' : '新增单据')

const isTransfer = computed(() => docForm.docType === 'TRANSFER')
const showVendor = computed(() => VENDOR_TYPES.includes(docForm.docType))
const showClient = computed(() => CLIENT_TYPES.includes(docForm.docType))
const showWorkorder = computed(() => WORKORDER_TYPES.includes(docForm.docType))
const showTask = computed(() => TASK_TYPES.includes(docForm.docType))

/** 动态校验规则：必填项取决于单据类型 */
const docRules = computed(() => ({
  docType: [{ required: true, message: '请选择单据类型', trigger: 'change' }],
  bizDate: [{ required: true, message: '请选择业务日期', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  toWarehouseId: isTransfer.value
    ? [{ required: true, message: '调拨单必须选择目标仓库', trigger: 'change' }]
    : [],
  workorderId: WORKORDER_REQUIRED_TYPES.includes(docForm.docType)
    ? [{ required: true, message: '该单据必须选择生产工单', trigger: 'change' }]
    : [],
  taskId: TASK_TYPES.includes(docForm.docType)
    ? [{ required: true, message: '生产领料单必须选择生产任务', trigger: 'change' }]
    : []
}))

// ==================== 详情弹窗 ====================

const detailVisible = ref(false)
const detailData = ref({})

// ==================== 库区库位下拉 ====================

function areaOptionsFor(warehouseId) {
  if (!warehouseId) return []
  return areaList.value.filter(a => a.warehouseId === warehouseId)
}

function locationOptionsFor(warehouseId) {
  if (!warehouseId) return []
  return locationList.value.filter(l => l.warehouseId === warehouseId)
}

function batchOptionsFor(itemId) {
  if (!itemId) return []
  return batchOptions.value.filter(b => b.itemId === itemId)
}

// ==================== 数据加载 ====================

const loadDocList = async function () {
  const params = {
    ...queryForm,
    pageNum: pagination.page,
    pageSize: pagination.size
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.bizDateStart = dateRange.value[0]
    params.bizDateEnd = dateRange.value[1]
  }
  const result = await getDocByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

const loadWarehouseOptions = async function () {
  const result = await getAllWarehouseList()
  warehouseOptions.value = result.data || []
}

/** 库区库位树：库区是父节点，库位是子节点，这里拍平成两个下拉 */
const loadLocationTree = async function () {
  const result = await getLocationTree({})
  const areas = []
  const locations = []
  for (const node of result.data || []) {
    // 顶层节点就是库区
    areas.push(node)
    for (const child of node.children || []) {
      locations.push({
        locationId: child.locationId,
        locationCode: child.locationCode,
        locationName: child.locationName,
        warehouseId: child.warehouseId ?? node.warehouseId,
        areaId: node.locationId,
        areaCode: node.locationCode,
        areaName: node.locationName
      })
    }
  }
  areaList.value = areas
  locationList.value = locations
}

const loadItemOptions = async function () {
  const result = await getItemByPage({ pageNum: 1, pageSize: 1000 })
  itemOptions.value = result.data.list || []
}

const loadBatchOptions = async function () {
  const result = await getBatchByPage({ pageNum: 1, pageSize: 1000 })
  batchOptions.value = result.data.list || []
}

const loadVendorOptions = async function () {
  const result = await getVendorByPage({ pageNum: 1, pageSize: 500 })
  vendorOptions.value = result.data.list || []
}

const loadClientOptions = async function () {
  const result = await getClientByPage({ pageNum: 1, pageSize: 500 })
  clientOptions.value = result.data.list || []
}

const loadWorkorderOptions = async function () {
  const result = await getWorkorderByPage({ pageNum: 1, pageSize: 500 })
  workorderOptions.value = result.data.list || []
}

/** 选中工单后拉它下面的任务（生产领料要挂到具体任务上） */
const loadTaskOptions = async function (workorderId) {
  if (!workorderId) {
    taskOptions.value = []
    return
  }
  const result = await getTaskListByWorkorder(workorderId)
  taskOptions.value = result.data || []
}

// ==================== 表格多选 ====================

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function clearSelection() {
  tableRef.value?.clearSelection()
}

// ==================== 搜索区操作 ====================

function handleSearch() {
  pagination.page = 1
  loadDocList()
}

function handleReset() {
  queryForm.docCode = ''
  queryForm.status = null
  queryForm.warehouseId = null
  dateRange.value = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadDocList()
}

// ==================== 新增 / 编辑 ====================

function handleAdd() {
  isEdit.value = false
  Object.assign(docForm, defaultDoc())
  // 新增时默认跟着列表当前选中的类型走
  docForm.docType = queryForm.docType
  docForm.bizDate = formatNow()
  docForm.warehouseId = queryForm.warehouseId
  dialogVisible.value = true
}

async function handleEdit(row) {
  const result = await getDocById(row.docId)
  if (!result.data) {
    ElMessage.info('数据有误，请刷新页面重试')
    return
  }
  Object.assign(docForm, defaultDoc(), result.data)
  // 行的库位跟单据头一致时才清掉，避免"单据头改了仓库、行还留着旧库位"
  docForm.lineList = (result.data.lineList || []).map(l => ({ ...defaultLine(), ...l }))
  if (!docForm.lineList.length) {
    docForm.lineList = [defaultLine()]
  }
  isEdit.value = true
  dialogVisible.value = true
  await loadTaskOptions(docForm.workorderId)
}

function handleDocTypeChange() {
  // 换了类型，方向和需要的字段都变了，把不相干的快照清掉
  docForm.partnerType = null
  docForm.partnerId = null
  docForm.partnerCode = ''
  docForm.partnerName = ''
  docForm.partnerNick = ''
  docForm.workorderId = null
  docForm.workorderCode = ''
  docForm.workorderName = ''
  docForm.taskId = null
  docForm.taskCode = ''
  docForm.taskName = ''
  docForm.toWarehouseId = null
  docForm.toAreaId = null
  docForm.toLocationId = null
  taskOptions.value = []
}

function handleWarehouseChange(warehouseId) {
  // 换仓库后原来的库区库位可能不在这个仓里，清掉让用户重选
  docForm.areaId = null
  docForm.locationId = null
  docForm.lineList.forEach(line => {
    line.warehouseId = warehouseId
    line.areaId = null
    line.locationId = null
  })
}

/** 选中供应商/客户：把编码、名称、简称一起带上（后端只认这几个冗余字段） */
function handlePartnerChange(partnerId) {
  if (showVendor.value) {
    const vendor = vendorOptions.value.find(v => v.vendorId === partnerId)
    docForm.partnerType = partnerId ? 'VENDOR' : null
    docForm.partnerCode = vendor?.vendorCode || ''
    docForm.partnerName = vendor?.vendorName || ''
    docForm.partnerNick = vendor?.vendorNick || ''
  } else if (showClient.value) {
    const client = clientOptions.value.find(c => c.clientId === partnerId)
    docForm.partnerType = partnerId ? 'CLIENT' : null
    docForm.partnerCode = client?.clientCode || ''
    docForm.partnerName = client?.clientName || ''
    docForm.partnerNick = client?.clientNick || ''
  }
}

function handleWorkorderChange(workorderId) {
  const workorder = workorderOptions.value.find(w => w.workorderId === workorderId)
  docForm.workorderCode = workorder?.workorderCode || ''
  docForm.workorderName = workorder?.workorderName || ''
  // 工单一换，原先选的任务多半也不对了，清掉重选
  docForm.taskId = null
  docForm.taskCode = ''
  docForm.taskName = ''
  loadTaskOptions(workorderId)
}

function handleTaskChange(taskId) {
  const task = taskOptions.value.find(t => t.taskId === taskId)
  docForm.taskCode = task?.taskCode || ''
  docForm.taskName = task?.taskName || ''
}

// ==================== 明细行 ====================

function handleAddLine() {
  const line = defaultLine()
  line.warehouseId = docForm.warehouseId
  // 单据头上选了库区库位，新行默认跟着走
  line.areaId = docForm.areaId
  line.locationId = docForm.locationId
  docForm.lineList.push(line)
}

function handleRemoveLine(index) {
  if (docForm.lineList.length <= 1) {
    ElMessage.warning('至少要保留一行明细')
    return
  }
  docForm.lineList.splice(index, 1)
}

/** 换物料后清掉批次（上一行物料的批次不一定适用于新物料） */
function handleLineItemChange(row) {
  row.batchId = null
  row.batchCode = ''
  if (!row.warehouseId) {
    row.warehouseId = docForm.warehouseId
  }
}

// ==================== 详情 ====================

async function handleDetail(row) {
  const result = await getDocById(row.docId)
  if (result.data) {
    detailData.value = result.data
    detailVisible.value = true
  } else {
    ElMessage.info('数据有误，请刷新页面重试')
  }
}

// ==================== 过账 / 取消 / 送达确认 / 删除 ====================

function handleExecute(row) {
  const typeName = row.docTypeName || dictLabel(docTypeOptions, row.docType, row.docType)
  ElMessageBox.confirm(
    `确定要过账单据「${row.docCode}」吗？\n\n` +
    `过账 = ${typeName}这批货真的动了：库存会立刻更新并产生流水。\n` +
    `过账后这张单据不能再改、也不能删除。`,
    '过账确认',
    { confirmButtonText: '确定过账', cancelButtonText: '再想想', type: 'warning' }
  ).then(async () => {
    await executeDoc(row.docId)
    ElMessage.success('过账成功，库存已更新')
    loadDocList()
  }).catch(() => {})
}

function handleCancel(row) {
  ElMessageBox.prompt('请填写取消原因（会留痕）', `取消单据「${row.docCode}」`, {
    confirmButtonText: '确定取消',
    cancelButtonText: '返回',
    inputPlaceholder: '例如：单据填错，重新开一张',
    inputValidator: (value) => (value && value.trim() ? true : '取消原因不能为空')
  }).then(async ({ value }) => {
    await cancelDoc(row.docId, value.trim())
    ElMessage.success('单据已取消')
    loadDocList()
  }).catch(() => {})
}

function handleFinish(row) {
  ElMessageBox.confirm(
    `确认单据「${row.docCode}」的货物已经送达目标仓库了吗？确认后单据状态变为【已完成】。`,
    '送达确认',
    { confirmButtonText: '确认送达', cancelButtonText: '取消', type: 'info' }
  ).then(async () => {
    await finishDoc(row.docId)
    ElMessage.success('送达确认完成')
    loadDocList()
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除单据「${row.docCode}」吗？该单据还处于待过账状态，没有动过库存。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteDoc(row.docId)
    ElMessage.success('删除成功')
    loadDocList()
  }).catch(() => {})
}

function handleBatchDelete() {
  if (!selectedRows.value.length) return
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 张单据吗？（只有待过账的能删）`,
    '批量删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const ids = selectedRows.value.map(r => r.docId)
    await deleteDocBatch(ids)
    clearSelection()
    ElMessage.success(`已删除 ${ids.length} 张`)
    loadDocList()
  }).catch(() => {})
}

// ==================== 表单提交 ====================

function handleSubmit() {
  docFormRef.value.validate(async (valid) => {
    if (!valid) return

    // 行校验：这些规则后端也会再查一遍，但先在前端拦住能少一次往返
    const lines = docForm.lineList
    if (!lines.length) {
      ElMessage.warning('请至少添加一行明细')
      return
    }
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      if (!line.itemId) {
        ElMessage.warning(`第 ${i + 1} 行：请选择物料`)
        return
      }
      if (!line.quantity || Number(line.quantity) <= 0) {
        ElMessage.warning(`第 ${i + 1} 行：数量必须大于 0`)
        return
      }
      // 物料启用了批次管理就必须选批次（后端会拦，这里提前提示）
      const item = itemOptions.value.find(it => it.itemId === line.itemId)
      if (item && item.batchFlag === 'Y' && !line.batchId) {
        ElMessage.warning(`第 ${i + 1} 行：物料[${item.itemCode}]启用了批次管理，必须选择批次`)
        return
      }
    }

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateDoc(docForm)
        ElMessage.success('保存成功')
      } else {
        const result = await createDoc(docForm)
        ElMessage.success(`新增成功，单据编号：${result.data?.docCode || ''}`)
      }
      dialogVisible.value = false
      loadDocList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDialogClose() {
  docFormRef.value?.resetFields()
  Object.assign(docForm, defaultDoc())
  taskOptions.value = []
}

// ==================== 小工具 ====================

function formatNow() {
  const d = new Date()
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} `
    + `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

onMounted(() => {
  loadDocList()
  loadWarehouseOptions()
  loadLocationTree()
  loadItemOptions()
  loadBatchOptions()
  loadVendorOptions()
  loadClientOptions()
  loadWorkorderOptions()
  loadDict('wm_doc_type', docTypeOptions)
  loadDict('wm_doc_status', statusOptions)
  loadDict('wm_io_flag', ioFlagOptions)
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

.to-target {
  color: #909399;
  font-size: 12px;
}

.locked-tip {
  color: #c0c4cc;
  font-size: 12px;
}

.line-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.line-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
