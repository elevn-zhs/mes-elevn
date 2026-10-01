<template>
  <div class="wm-notice">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 ============ -->
      <template #header>
        <div class="card-header">
          <span>仓储通知单</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增通知单</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="通知类型">
          <el-select v-model="queryForm.noticeType" style="width: 170px" @change="handleSearch">
            <el-option v-for="d in noticeTypeOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知单编号">
          <el-input v-model="queryForm.noticeCode" placeholder="请输入编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="d in statusOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="往来对象">
          <el-input v-model="queryForm.partnerName" placeholder="供应商/客户名称" clearable />
        </el-form-item>
        <el-form-item label="通知日期">
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

      <!-- 通知单不动库存，这一点在页面上反复强调，学生最容易搞混 -->
      <el-alert
        v-if="noticeTypeTip"
        :type="noticeTypeTipType"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
        :title="noticeTypeTip"
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
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" :selectable="row => row.status === 'PREPARE'" />
        <el-table-column prop="noticeId" label="ID" width="70" />
        <el-table-column prop="noticeCode" label="通知单编号" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="handleDetail(row)">{{ row.noticeCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="noticeName" label="通知单名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.noticeName || '—' }}</template>
        </el-table-column>
        <el-table-column label="通知类型" width="120">
          <template #default="{ row }">
            {{ row.noticeTypeName || dictLabel(noticeTypeOptions, row.noticeType) }}
          </template>
        </el-table-column>
        <el-table-column label="往来对象" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.partnerName">
              {{ row.partnerName }}
              <el-tag v-if="row.partnerType" size="small" effect="plain" type="info">
                {{ row.partnerType === 'VENDOR' ? '供应商' : '客户' }}
              </el-tag>
            </span>
            <span v-else class="sub-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="生产工单" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.workorderCode || '—' }}</template>
        </el-table-column>
        <el-table-column prop="noticeDate" label="通知日期" width="160">
          <template #default="{ row }">{{ row.noticeDate || '—' }}</template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="110">
          <template #default="{ row }">{{ row.contact || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="dictTagType(statusOptions, row.status)" size="small">
              {{ dictLabel(statusOptions, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PREPARE'" link type="primary"
                       :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canTriggerQc(row)" link type="success"
                       :icon="CircleCheck" @click="handleTriggerQc(row)">触发检验</el-button>
            <el-button v-if="row.status === 'PREPARE'" link type="danger"
                       :icon="Delete" @click="handleDelete(row)">删除</el-button>
            <el-tooltip v-if="row.status === 'CONFIRMED'" placement="top"
                        content="已报检，不能再改也不能再删">
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
          @change="loadNoticeList"
        />
      </div>
    </el-card>

    <!-- ============ 新增/编辑通知单弹窗 ============ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1080px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="noticeFormRef" :model="noticeForm" :rules="noticeRules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="通知单编号">
              <el-input v-model="noticeForm.noticeCode" disabled placeholder="保存后按编码规则自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="通知类型" prop="noticeType">
              <el-select v-model="noticeForm.noticeType" style="width: 100%"
                         :disabled="isEdit" @change="handleTypeChange">
                <el-option v-for="d in noticeTypeOptions" :key="d.dictValue"
                           :label="d.dictLabel" :value="d.dictValue" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="通知单名称" prop="noticeName">
              <el-input v-model="noticeForm.noticeName" placeholder="如：XX 供应商 9 月钢材到货" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <!-- 到货通知：必须有供应商 -->
          <el-col :span="8" v-if="noticeForm.noticeType === 'ARRIVAL'">
            <el-form-item label="供应商" prop="partnerId">
              <el-select v-model="noticeForm.partnerId" filterable style="width: 100%"
                         placeholder="请选择供应商" @change="handleVendorChange">
                <el-option v-for="v in vendorOptions" :key="v.vendorId"
                           :label="v.vendorName" :value="v.vendorId" />
              </el-select>
            </el-form-item>
          </el-col>
          <!-- 发货通知：必须有客户 -->
          <el-col :span="8" v-if="noticeForm.noticeType === 'SALES'">
            <el-form-item label="客户" prop="partnerId">
              <el-select v-model="noticeForm.partnerId" filterable style="width: 100%"
                         placeholder="请选择客户" @change="handleClientChange">
                <el-option v-for="c in clientOptions" :key="c.clientId"
                           :label="c.clientName" :value="c.clientId" />
              </el-select>
            </el-form-item>
          </el-col>
          <!-- 备料申请：必须有生产工单 -->
          <el-col :span="8" v-if="noticeForm.noticeType === 'MATERIAL_REQUEST'">
            <el-form-item label="生产工单" prop="workorderId">
              <el-select v-model="noticeForm.workorderId" filterable style="width: 100%"
                         placeholder="请选择生产工单" @change="handleWorkorderChange">
                <el-option v-for="w in workorderOptions" :key="w.workorderId"
                           :label="w.workorderCode" :value="w.workorderId" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8" v-if="noticeForm.noticeType === 'ARRIVAL'">
            <el-form-item label="采购订单号">
              <el-input v-model="noticeForm.poCode" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="noticeForm.noticeType === 'SALES'">
            <el-form-item label="销售订单号">
              <el-input v-model="noticeForm.soCode" placeholder="选填" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="通知日期">
              <el-date-picker
                v-model="noticeForm.noticeDate"
                type="datetime"
                placeholder="到货 / 发货日期"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="联系人">
              <el-input v-model="noticeForm.contact" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系方式">
              <el-input v-model="noticeForm.tel" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收货地址">
              <el-input v-model="noticeForm.address" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 备料申请额外的时间窗：产线什么时候要、最晚什么时候到 -->
        <el-row :gutter="16" v-if="noticeForm.noticeType === 'MATERIAL_REQUEST'">
          <el-col :span="8">
            <el-form-item label="需求时间">
              <el-date-picker
                v-model="noticeForm.requestTime"
                type="datetime"
                placeholder="申请 / 需求时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="要求开始">
              <el-date-picker
                v-model="noticeForm.startTime"
                type="datetime"
                placeholder="要求开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="要求完成">
              <el-date-picker
                v-model="noticeForm.endTime"
                type="datetime"
                placeholder="要求完成时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="noticeForm.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- ---------- 明细行 ---------- -->
        <el-divider content-position="left">通知明细（这次要送 / 要提 / 要领哪些料）</el-divider>

        <div class="line-toolbar">
          <el-button type="primary" plain size="small" :icon="Plus" @click="addLine">添加明细行</el-button>
          <span class="line-tip">数量必须大于 0；选物料时若该物料启用了批次管理，批次也要选。</span>
        </div>

        <el-table :data="noticeForm.lineList" border size="small">
          <el-table-column label="行号" width="60" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column label="物料" min-width="200">
            <template #default="{ row }">
              <el-select v-model="row.itemId" filterable placeholder="请选择物料"
                         style="width: 100%" @change="val => handleLineItemChange(row, val)">
                <el-option v-for="i in itemOptions" :key="i.itemId"
                           :label="i.itemName" :value="i.itemId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="批次" min-width="150">
            <template #default="{ row }">
              <el-select v-model="row.batchId" filterable clearable placeholder="无批次可不选"
                         style="width: 100%">
                <el-option v-for="b in batchOptionsOf(row.itemId)" :key="b.batchId"
                           :label="b.batchCode" :value="b.batchId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="通知数量" width="160">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="0.000001" :precision="6"
                               :controls="false" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">{{ row.unitName || '—' }}</template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.remark" placeholder="选填" maxlength="500" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" :icon="Delete" @click="removeLine($index)" />
            </template>
          </el-table-column>
        </el-table>

        <div class="line-tip" style="margin-top: 8px">
          共 {{ noticeForm.lineList.length }} 行
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 详情弹窗 ============ -->
    <el-dialog v-model="detailVisible" title="通知单详情" width="1000px">
      <template v-if="detailData">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="通知单编号">{{ detailData.noticeCode }}</el-descriptions-item>
          <el-descriptions-item label="通知单名称">{{ detailData.noticeName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="通知类型">
            {{ detailData.noticeTypeName || dictLabel(noticeTypeOptions, detailData.noticeType) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="dictTagType(statusOptions, detailData.status)" size="small">
              {{ dictLabel(statusOptions, detailData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="往来对象">
            {{ detailData.partnerName || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="往来对象类型">
            <span v-if="detailData.partnerType">
              {{ detailData.partnerType === 'VENDOR' ? '供应商' : '客户' }}
            </span>
            <span v-else>—</span>
          </el-descriptions-item>
          <el-descriptions-item label="采购订单号">{{ detailData.poCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="销售订单号">{{ detailData.soCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="生产工单">{{ detailData.workorderCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="工作站">
            {{ detailData.workstationName || detailData.workstationCode || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ detailData.applicantNick || detailData.applicantName || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="通知日期">{{ detailData.noticeDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="需求时间">{{ detailData.requestTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="要求开始">{{ detailData.startTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="要求完成">{{ detailData.endTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detailData.contact || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ detailData.tel || '—' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="3">{{ detailData.address || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ detailData.remark || '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detailData.createBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detailData.updateTime || '—' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">通知明细</el-divider>
        <el-table :data="detailLines" border size="small">
          <el-table-column prop="lineNo" label="行号" width="60" align="center" />
          <el-table-column prop="itemCode" label="物料编码" width="130" />
          <el-table-column prop="itemName" label="物料名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="specification" label="规格" min-width="120" show-overflow-tooltip />
          <el-table-column label="批次号" width="130">
            <template #default="{ row }">{{ row.batchCode || '—' }}</template>
          </el-table-column>
          <el-table-column label="通知数量" width="110" align="right">
            <template #default="{ row }">{{ formatQty(row.quantity) }}</template>
          </el-table-column>
          <el-table-column label="合格数量" width="110" align="right">
            <template #default="{ row }">{{ row.quantityQualified == null ? '—' : formatQty(row.quantityQualified) }}</template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">{{ row.unitName || row.unitOfMeasure || '—' }}</template>
          </el-table-column>
          <el-table-column label="报检" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.qcFlag === 'Y'" type="warning" size="small">已报检</el-tag>
              <span v-else class="sub-text">未报检</span>
            </template>
          </el-table-column>
          <el-table-column label="检验单" width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.qcCode">{{ row.qcCode }}</span>
              <span v-else class="sub-text">待 C 线回填</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="130" show-overflow-tooltip />
        </el-table>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Edit, Delete, Close, View, CircleCheck
} from '@element-plus/icons-vue'

import {
  getNoticeByPage,
  getNoticeById,
  createNotice,
  updateNotice,
  triggerQc,
  deleteNotice,
  deleteNoticeBatch
} from '@/api/wm/notice.js'
import { getItemByPage } from '@/api/md/item.js'
import { getVendorByPage } from '@/api/md/vendor.js'
import { getClientByPage } from '@/api/md/client.js'
import { getWorkorderByPage } from '@/api/pro/workorder.js'
import { getBatchByPage } from '@/api/wm/batch.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'

// ==================== 类型提示 ====================
// 三类通知的必填字段不一样，这条规则写在后端 checkBizRules 里，前端只是跟着显示与提示。
// 真正拦得住的是后端 —— 前端提示只是让人少走弯路。
const NOTICE_TYPE_TIP = {
  ARRIVAL: '到货通知：供应商送货前的预告。到货后触发 C 线【来料检验 IQC】，因此必须填供应商；本单不动库存。',
  SALES: '发货通知：客户要货的预告。发货前触发 C 线【出货检验 OQC】，因此必须填客户；本单不动库存。',
  MATERIAL_REQUEST: '备料申请：产线按工单申请备料，是生产领料单的上游来源，因此必须填生产工单；本单不触发检验、不动库存。'
}

const noticeTypeTip = computed(() => NOTICE_TYPE_TIP[queryForm.noticeType] || '')
const noticeTypeTipType = computed(() => {
  if (queryForm.noticeType === 'MATERIAL_REQUEST') return 'warning'
  if (queryForm.noticeType === 'SALES') return 'success'
  return 'info'
})

// ==================== 搜索与分页 ====================
const queryForm = reactive({
  noticeType: 'ARRIVAL',
  noticeCode: '',
  status: null,
  partnerName: ''
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
const loading = ref(false)

const itemOptions = ref([])
const vendorOptions = ref([])
const clientOptions = ref([])
const workorderOptions = ref([])

// 批次下拉按物料缓存：{ [itemId]: [批次...] }
const batchCache = reactive({})

const noticeTypeOptions = ref([])
const statusOptions = ref([])

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

function formatQty(v) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  if (Number.isNaN(n)) return v
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 6 })
}

/** 明细行里按物料取批次下拉数据（同步返回，异步加载在 handleLineItemChange 里做） */
function batchOptionsOf(itemId) {
  return batchCache[itemId] || []
}

// ==================== 新增/编辑弹窗 ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const noticeFormRef = ref(null)

const defaultLine = () => ({
  lineId: null,
  itemId: null,
  itemCode: '',
  itemName: '',
  unitName: '',
  unitOfMeasure: '',
  batchId: null,
  batchCode: '',
  quantity: 1,
  remark: ''
})

const noticeForm = reactive({
  noticeId: null,
  noticeType: 'ARRIVAL',
  noticeCode: '',
  noticeName: '',
  poCode: '',
  soCode: '',
  partnerType: '',
  partnerId: null,
  partnerName: '',
  partnerCode: '',
  workorderId: null,
  workorderCode: '',
  requestTime: '',
  startTime: '',
  endTime: '',
  noticeDate: '',
  contact: '',
  tel: '',
  address: '',
  remark: '',
  lineList: [defaultLine()]
})

// 校验规则用 computed 生成：哪个字段必填取决于 noticeType。
// 用固定 rules 的话，切类型时会出现"改了可见性但校验没跟着变"的错位。
const noticeRules = computed(() => ({
  noticeType: [{ required: true, message: '通知类型不能为空', trigger: 'change' }],
  noticeName: [{ max: 255, message: '名称长度不能超过 255 个字符', trigger: 'blur' }],
  partnerId: (noticeForm.noticeType === 'ARRIVAL' || noticeForm.noticeType === 'SALES')
    ? [{
      required: true,
      validator: (rule, value, cb) => {
        if (value) return cb()
        cb(new Error(noticeForm.noticeType === 'ARRIVAL' ? '到货通知必须选择供应商' : '发货通知必须选择客户'))
      },
      trigger: 'change'
    }]
    : [],
  workorderId: noticeForm.noticeType === 'MATERIAL_REQUEST'
    ? [{ required: true, message: '备料申请必须选择生产工单', trigger: 'change' }]
    : []
}))

const dialogTitle = computed(() => (isEdit.value ? '编辑通知单' : '新增通知单'))

function resetForm() {
  noticeForm.noticeId = null
  noticeForm.noticeType = queryForm.noticeType || 'ARRIVAL'
  noticeForm.noticeCode = ''
  noticeForm.noticeName = ''
  noticeForm.poCode = ''
  noticeForm.soCode = ''
  noticeForm.partnerType = ''
  noticeForm.partnerId = null
  noticeForm.partnerName = ''
  noticeForm.partnerCode = ''
  noticeForm.workorderId = null
  noticeForm.workorderCode = ''
  noticeForm.requestTime = ''
  noticeForm.startTime = ''
  noticeForm.endTime = ''
  noticeForm.noticeDate = ''
  noticeForm.contact = ''
  noticeForm.tel = ''
  noticeForm.address = ''
  noticeForm.remark = ''
  noticeForm.lineList = [defaultLine()]
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  // 到货通知给个默认通知日期，省得每次点日历
  if (!noticeForm.noticeDate) {
    const d = new Date()
    const pad = n => String(n).padStart(2, '0')
    noticeForm.noticeDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} `
      + `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  const result = await getNoticeById(row.noticeId)
  const data = result.data
  if (!data) {
    ElMessage.error('通知单不存在或已被删除')
    return
  }
  noticeForm.noticeId = data.noticeId
  noticeForm.noticeType = data.noticeType
  noticeForm.noticeCode = data.noticeCode
  noticeForm.noticeName = data.noticeName || ''
  noticeForm.poCode = data.poCode || ''
  noticeForm.soCode = data.soCode || ''
  noticeForm.partnerType = data.partnerType || ''
  noticeForm.partnerId = data.partnerId || null
  noticeForm.partnerName = data.partnerName || ''
  noticeForm.partnerCode = data.partnerCode || ''
  noticeForm.workorderId = data.workorderId || null
  noticeForm.workorderCode = data.workorderCode || ''
  noticeForm.requestTime = data.requestTime || ''
  noticeForm.startTime = data.startTime || ''
  noticeForm.endTime = data.endTime || ''
  noticeForm.noticeDate = data.noticeDate || ''
  noticeForm.contact = data.contact || ''
  noticeForm.tel = data.tel || ''
  noticeForm.address = data.address || ''
  noticeForm.remark = data.remark || ''
  noticeForm.lineList = (data.lineList && data.lineList.length)
    ? data.lineList.map(l => ({
      lineId: l.lineId,
      itemId: l.itemId,
      itemCode: l.itemCode || '',
      itemName: l.itemName || '',
      unitName: l.unitName || '',
      unitOfMeasure: l.unitOfMeasure || '',
      batchId: l.batchId || null,
      batchCode: l.batchCode || '',
      quantity: l.quantity == null ? 1 : Number(l.quantity),
      remark: l.remark || ''
    }))
    : [defaultLine()]
  // 编辑时把行上的批次下拉也拉进缓存
  noticeForm.lineList.forEach(l => {
    if (l.itemId && !batchCache[l.itemId]) loadBatchOptions(l.itemId)
  })
  dialogVisible.value = true
}

function handleDialogClose() {
  submitLoading.value = false
  noticeFormRef.value?.clearValidate()
}

/** 切换通知类型：清掉与旧类型相关的字段，避免带着上次的供应商去当客户 */
function handleTypeChange(val) {
  noticeForm.partnerId = null
  noticeForm.partnerName = ''
  noticeForm.partnerCode = ''
  noticeForm.workorderId = null
  noticeForm.workorderCode = ''
  if (val === 'ARRIVAL') noticeForm.partnerType = 'VENDOR'
  else if (val === 'SALES') noticeForm.partnerType = 'CLIENT'
  else noticeForm.partnerType = ''
  noticeFormRef.value?.clearValidate(['partnerId', 'workorderId'])
}

function handleVendorChange(vendorId) {
  const hit = unref(vendorOptions).find(v => v.vendorId === vendorId)
  noticeForm.partnerType = 'VENDOR'
  noticeForm.partnerName = hit ? hit.vendorName : ''
  noticeForm.partnerCode = hit ? hit.vendorCode : ''
}

function handleClientChange(clientId) {
  const hit = unref(clientOptions).find(c => c.clientId === clientId)
  noticeForm.partnerType = 'CLIENT'
  noticeForm.partnerName = hit ? hit.clientName : ''
  noticeForm.partnerCode = hit ? hit.clientCode : ''
}

function handleWorkorderChange(workorderId) {
  const hit = unref(workorderOptions).find(w => w.workorderId === workorderId)
  noticeForm.workorderCode = hit ? hit.workorderCode : ''
}

// ==================== 明细行 ====================

function addLine() {
  noticeForm.lineList.push(defaultLine())
}

function removeLine(index) {
  if (noticeForm.lineList.length <= 1) {
    ElMessage.warning('至少要保留一行明细')
    return
  }
  noticeForm.lineList.splice(index, 1)
}

function handleLineItemChange(row, itemId) {
  const hit = unref(itemOptions).find(i => i.itemId === itemId)
  row.itemCode = hit ? hit.itemCode : ''
  row.itemName = hit ? hit.itemName : ''
  row.unitName = hit ? (hit.unitName || '') : ''
  row.unitOfMeasure = hit ? (hit.unitOfMeasure || '') : ''
  // 换物料后原来的批次就不属于这个物料了，必须清掉
  row.batchId = null
  row.batchCode = ''
  if (itemId) loadBatchOptions(itemId)
}

async function loadBatchOptions(itemId) {
  if (!itemId || batchCache[itemId]) return
  const result = await getBatchByPage({ itemId, pageNum: 1, pageSize: 500 })
  batchCache[itemId] = result.data?.list || []
}

// ==================== 提交 ====================

async function handleSubmit() {
  await noticeFormRef.value.validate()

  const lines = noticeForm.lineList.filter(l => l.itemId)
  if (!lines.length) {
    ElMessage.warning('至少要有明细行，并选择物料')
    return
  }
  const invalid = lines.find(l => !l.quantity || Number(l.quantity) <= 0)
  if (invalid) {
    ElMessage.warning('明细行的数量必须大于 0')
    return
  }

  const payload = {
    noticeType: noticeForm.noticeType,
    noticeName: noticeForm.noticeName,
    poCode: noticeForm.poCode,
    soCode: noticeForm.soCode,
    partnerType: noticeForm.partnerType,
    partnerId: noticeForm.partnerId,
    partnerName: noticeForm.partnerName,
    partnerCode: noticeForm.partnerCode,
    workorderId: noticeForm.workorderId,
    workorderCode: noticeForm.workorderCode,
    requestTime: noticeForm.requestTime || null,
    startTime: noticeForm.startTime || null,
    endTime: noticeForm.endTime || null,
    noticeDate: noticeForm.noticeDate || null,
    contact: noticeForm.contact,
    tel: noticeForm.tel,
    address: noticeForm.address,
    remark: noticeForm.remark,
    lineList: lines.map((l, idx) => ({
      lineId: l.lineId,
      itemId: l.itemId,
      itemCode: l.itemCode,
      itemName: l.itemName,
      unitOfMeasure: l.unitOfMeasure,
      unitName: l.unitName,
      batchId: l.batchId || null,
      batchCode: l.batchCode || '',
      quantity: l.quantity,
      remark: l.remark,
      lineNo: idx + 1
    }))
  }

  submitLoading.value = true
  try {
    const result = isEdit.value
      ? await updateNotice({ noticeId: noticeForm.noticeId, ...payload })
      : await createNotice(payload)
    if (result.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadNoticeList()
    } else {
      ElMessage.error(result.msg || '保存失败')
    }
  } catch (e) {
    // 后端的业务错误提示（比如"到货通知必须有供应商"）已在拦截器里弹过，这里不重复弹
  } finally {
    submitLoading.value = false
  }
}

// ==================== 触发检验 ====================

/** 备料申请不触发检验，按钮不显示 */
function canTriggerQc(row) {
  return row.status === 'PREPARE' && row.noticeType !== 'MATERIAL_REQUEST'
}

async function handleTriggerQc(row) {
  const qcType = row.noticeType === 'ARRIVAL' ? 'IQC（来料检验）' : 'OQC（出货检验）'
  try {
    await ElMessageBox.confirm(
      `将对通知单 ${row.noticeCode} 发起 ${qcType} 申请。\n\n`
      + '触发后单据变为【已触发检验】，不能再修改、不能再删除。\n'
      + '注意：C 线的检验单接口目前还没就绪，本次只会把明细行标记为待检并记录日志，不会真的生成检验单。',
      '确认触发检验',
      { type: 'warning', confirmButtonText: '确认触发', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  try {
    const result = await triggerQc(row.noticeId)
    if (result.code === 200) {
      ElMessage.success(result.msg || '已提交检验申请')
      loadNoticeList()
    } else {
      ElMessage.error(result.msg || '触发检验失败')
    }
  } catch (e) {
    // 错误提示由拦截器统一处理
  }
}

// ==================== 删除 ====================

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除通知单 ${row.noticeCode} 吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  const result = await deleteNotice(row.noticeId)
  if (result.code === 200) {
    ElMessage.success('删除成功')
    loadNoticeList()
  } else {
    ElMessage.error(result.msg || '删除失败')
  }
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function clearSelection() {
  tableRef.value?.clearSelection()
  selectedRows.value = []
}

async function handleBatchDelete() {
  const ids = selectedRows.value.map(r => r.noticeId)
  if (!ids.length) return
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${ids.length} 张通知单吗？删除后不可恢复。`,
      '批量删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  const result = await deleteNoticeBatch(ids)
  if (result.code === 200) {
    ElMessage.success('删除成功')
    clearSelection()
    loadNoticeList()
  } else {
    ElMessage.error(result.msg || '删除失败')
  }
}

// ==================== 详情 ====================
const detailVisible = ref(false)
const detailData = ref(null)
const detailLines = ref([])

async function handleDetail(row) {
  const result = await getNoticeById(row.noticeId)
  if (!result.data) {
    ElMessage.error('通知单不存在或已被删除')
    return
  }
  detailData.value = result.data
  detailLines.value = result.data.lineList || []
  detailVisible.value = true
}

// ==================== 列表加载 ====================

async function loadNoticeList() {
  loading.value = true
  try {
    const params = {
      noticeType: queryForm.noticeType,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    if (queryForm.noticeCode) params.noticeCode = queryForm.noticeCode
    if (queryForm.status) params.status = queryForm.status
    if (queryForm.partnerName) params.partnerName = queryForm.partnerName
    if (dateRange.value && dateRange.value.length === 2) {
      params.noticeDateStart = dateRange.value[0]
      params.noticeDateEnd = dateRange.value[1]
    }
    const result = await getNoticeByPage(params)
    tableData.value = result.data?.list || []
    pagination.total = result.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadNoticeList()
}

function handleReset() {
  queryForm.noticeType = 'ARRIVAL'
  queryForm.noticeCode = ''
  queryForm.status = null
  queryForm.partnerName = ''
  dateRange.value = null
  pagination.page = 1
  loadNoticeList()
}

// ==================== 下拉数据 ====================

async function loadItemOptions() {
  const result = await getItemByPage({ pageNum: 1, pageSize: 500 })
  itemOptions.value = result.data?.list || []
}

async function loadVendorOptions() {
  const result = await getVendorByPage({ pageNum: 1, pageSize: 500 })
  vendorOptions.value = result.data?.list || []
}

async function loadClientOptions() {
  const result = await getClientByPage({ pageNum: 1, pageSize: 500 })
  clientOptions.value = result.data?.list || []
}

async function loadWorkorderOptions() {
  const result = await getWorkorderByPage({ pageNum: 1, pageSize: 500 })
  workorderOptions.value = result.data?.list || []
}

onMounted(() => {
  loadNoticeList()
  loadItemOptions()
  loadVendorOptions()
  loadClientOptions()
  loadWorkorderOptions()
  loadDict('wm_notice_type', noticeTypeOptions)
  loadDict('wm_notice_status', statusOptions)
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
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

.sub-text {
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
</style>
