<template>
  <div class="pro-task">
    <el-card shadow="never">

      <!-- ============ 顶部标题栏 + 视图切换 ============ -->
      <template #header>
        <div class="card-header">
          <span>生产任务</span>
          <div class="header-actions">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button value="table">列表</el-radio-button>
              <el-radio-button value="gantt">甘特图</el-radio-button>
            </el-radio-group>
            <el-button :icon="Refresh" size="small" @click="loadTaskList">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- ============ 搜索筛选表单 ============ -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="工单编码">
          <el-input v-model="queryForm.workorderCode" placeholder="请输入工单编码" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="任务编号">
          <el-input v-model="queryForm.taskCode" placeholder="请输入任务编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="工序">
          <el-select v-model="queryForm.processId" placeholder="全部" clearable filterable style="width: 140px">
            <el-option v-for="p in processOptions" :key="p.processId"
                       :label="p.processCode + ' ' + p.processName" :value="p.processId" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作站">
          <el-select v-model="queryForm.workstationId" placeholder="全部" clearable filterable style="width: 160px">
            <el-option v-for="w in workstationOptions" :key="w.workstationId"
                       :label="w.workstationCode + ' ' + w.workstationName" :value="w.workstationId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in statusOptions" :key="item.dictValue"
                       :label="item.dictLabel" :value="item.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- ================================================================
           视图一：列表
           ================================================================ -->
      <template v-if="viewMode === 'table'">
        <el-table :data="tableData" border stripe>
          <el-table-column prop="taskCode" label="任务编号" min-width="170">
            <template #default="scope">
              <el-link type="primary" @click="handleShowDetail(scope.row)">{{ scope.row.taskCode }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="workorderCode" label="工单编码" min-width="150" show-overflow-tooltip />
          <el-table-column label="工序" width="130">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.processCode }}</el-tag>
              <span class="ml4">{{ row.processName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="workstationName" label="工作站" min-width="140" show-overflow-tooltip />
          <el-table-column prop="itemName" label="产品" min-width="160" show-overflow-tooltip />
          <el-table-column label="排产数量" width="110" align="right">
            <template #default="{ row }">
              {{ formatQty(row.quantity) }} {{ row.unitName || row.unitOfMeasure || '' }}
            </template>
          </el-table-column>
          <el-table-column label="已生产/合格" width="130" align="right">
            <template #default="{ row }">
              <span class="text-muted">{{ formatQty(row.quantityProduced) }}</span>
              <span class="text-muted"> / </span>
              <span class="qty-ok">{{ formatQty(row.quantityQualified) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="开始时间" width="160">
            <template #default="{ row }">{{ shortTime(row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="时长" width="100" align="right">
            <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
          </el-table-column>
          <el-table-column label="结束时间" width="160">
            <template #default="{ row }">{{ shortTime(row.endTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="View" @click="handleShowDetail(row)">详情</el-button>
              <!--
                报工入口：已取消的任务不能报；已完工的仍可点进来补看/补报（后端会拦超报）
                排满的任务按钮置灰，避免点进去才发现"没得报"（同排产按钮的处理）
              -->
              <el-button
                v-if="row.status !== 'CANCELED'"
                link type="success" :icon="DocumentAdd"
                :disabled="isTaskFullyProduced(row)"
                :title="isTaskFullyProduced(row) ? '该工序已报满，没有可报数量' : ''"
                @click="handleFeedback(row)"
              >报工</el-button>
              <el-button
                v-if="row.status === 'NORMAL'"
                link type="warning" :icon="Edit"
                @click="handleEdit(row)"
              >调整</el-button>
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
      </template>

      <!-- ================================================================
           视图二：甘特图
           自绘 SVG：每道工序一行，横轴为时间。
           不引第三方图表库 —— 排产甘特图的核心就是"条的位置和长度"，
           自绘能让时间比例、颜色、提示完全跟着数据走，也少一个依赖。
           ================================================================ -->
      <template v-else>
        <div class="gantt-toolbar">
          <span class="batch-tip">
            每行是一道工序，条的长度是按工时算出的时长；
            同一水平位置表示并行进行。数据来自当前筛选条件下的任务。
          </span>
        </div>

        <el-empty v-if="!gantt.rows.length" description="当前条件下没有任务，请调整筛选条件或先做排产" />

        <div v-else class="gantt-wrap">
          <!-- 左侧工序标签列 -->
          <div class="gantt-labels">
            <div class="gantt-label-head">工序 / 工作站</div>
            <div v-for="row in gantt.rows" :key="row.taskId" class="gantt-label-row" :title="row.taskCode">
              <span class="gantt-label-code">{{ row.processCode }}</span>
              <span class="gantt-label-name">{{ row.processName }}</span>
              <span class="gantt-label-ws">{{ row.workstationName }}</span>
            </div>
          </div>

          <!-- 右侧时间轴与条形 -->
          <div class="gantt-body">
            <!-- 时间刻度 -->
            <div class="gantt-axis">
              <div v-for="(tick, i) in gantt.ticks" :key="i" class="gantt-tick"
                   :style="{ left: tick.left + '%' }">
                <span>{{ tick.label }}</span>
              </div>
            </div>

            <!-- 行与条 -->
            <div class="gantt-rows">
              <!-- 竖向网格线，跟着刻度走 -->
              <div v-for="(tick, i) in gantt.ticks" :key="'grid' + i" class="gantt-grid"
                   :style="{ left: tick.left + '%' }"></div>

              <div v-for="row in gantt.rows" :key="row.taskId" class="gantt-row">
                <el-tooltip
                  placement="top"
                  :content="tooltipOf(row)"
                  raw-content
                >
                  <div class="gantt-bar"
                       :style="{ left: row.left + '%', width: row.width + '%', background: row.color }">
                    <span v-if="row.width > 8" class="gantt-bar-text">
                      {{ formatQty(row.quantity) }}
                    </span>
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-card>

    <!-- ============ 任务详情弹窗 ============ -->
    <el-dialog v-model="detailVisible" title="生产任务详情" width="760px">
      <el-descriptions :column="2" border v-if="detailRow">
        <el-descriptions-item label="任务编号">{{ detailRow.taskCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailRow.status)" size="small">{{ statusLabel(detailRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工单编码">{{ detailRow.workorderCode }}</el-descriptions-item>
        <el-descriptions-item label="工单名称">{{ detailRow.workorderName }}</el-descriptions-item>
        <el-descriptions-item label="工序">
          {{ detailRow.processCode }} {{ detailRow.processName }}
        </el-descriptions-item>
        <el-descriptions-item label="工作站">
          {{ detailRow.workstationCode }} {{ detailRow.workstationName }}
        </el-descriptions-item>
        <el-descriptions-item label="产品">{{ detailRow.itemCode }} {{ detailRow.itemName }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ detailRow.specification || '—' }}</el-descriptions-item>
        <el-descriptions-item label="排产数量">
          {{ formatQty(detailRow.quantity) }} {{ detailRow.unitName || detailRow.unitOfMeasure || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="已生产数量">{{ formatQty(detailRow.quantityProduced) }}</el-descriptions-item>
        <el-descriptions-item label="合格品数量">
          <span class="qty-ok">{{ formatQty(detailRow.quantityQualified) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="不良品数量">
          <span class="qty-bad">{{ formatQty(detailRow.quantityUnqualified) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detailRow.startTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="生产时长">
          {{ formatDuration(detailRow.duration) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detailRow.endTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求日期">{{ detailRow.requestDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detailRow.clientName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="完成日期">{{ detailRow.finishDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ============ 任务调整弹窗（换工作站 / 改计划时间） ============ -->
    <el-dialog
      v-model="editVisible"
      title="调整生产任务"
      width="560px"
      :close-on-click-modal="false"
      @close="handleEditClose"
    >
      <el-alert
        title="只允许调整未开工的任务。换工作站时，新工作站必须能承担这道工序。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="任务编号">
          <el-input :model-value="editForm.taskCode" disabled />
        </el-form-item>
        <el-form-item label="工序">
          <el-input :model-value="editForm.processName" disabled />
        </el-form-item>
        <el-form-item label="工作站" prop="workstationId">
          <el-select v-model="editForm.workstationId" placeholder="请选择工作站" filterable style="width: 100%">
            <el-option v-for="w in editWorkstationOptions" :key="w.workstationId"
                       :label="w.workstationCode + ' ' + w.workstationName" :value="w.workstationId" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始" prop="startTime">
          <el-date-picker
            v-model="editForm.startTime"
            type="datetime"
            placeholder="请选择计划开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- ================================================================
         报工弹窗
         报工是唯一产生真实产量的动作，所以这里把"上限"和"三方数量关系"
         都直接摆出来，让报工人在提交前就看清能不能报、报多少。
         ================================================================ -->
    <el-dialog
      v-model="feedbackVisible"
      title="生产报工"
      width="720px"
      :close-on-click-modal="false"
      @close="handleFeedbackClose"
    >
      <template v-if="feedbackPreview">
        <!-- 任务与剩余可报数量摘要 -->
        <el-descriptions :column="3" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="任务编号">{{ feedbackPreview.task.taskCode }}</el-descriptions-item>
          <el-descriptions-item label="工序">
            {{ feedbackPreview.task.processCode }} {{ feedbackPreview.task.processName }}
          </el-descriptions-item>
          <el-descriptions-item label="工作站">{{ feedbackPreview.task.workstationName }}</el-descriptions-item>
          <el-descriptions-item label="产品">
            {{ feedbackPreview.task.itemName }}
          </el-descriptions-item>
          <el-descriptions-item label="排产数量">
            {{ formatQty(feedbackPreview.quantityScheduled) }}
            {{ feedbackPreview.task.unitName || feedbackPreview.task.unitOfMeasure || '' }}
          </el-descriptions-item>
          <el-descriptions-item label="已报工">
            <span class="qty-scheduled">{{ formatQty(feedbackPreview.quantityProduced) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="还可报" :span="3">
            <b class="qty-remain">{{ formatQty(feedbackPreview.quantityRemain) }}</b>
            {{ feedbackPreview.task.unitName || feedbackPreview.task.unitOfMeasure || '' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-form :model="feedbackForm" label-width="110px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="报工类型">
                <el-select v-model="feedbackForm.feedbackType" style="width: 100%">
                  <el-option v-for="d in feedbackTypeOptions" :key="d.dictValue"
                             :label="d.dictLabel" :value="d.dictValue" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="报工途径">
                <el-select v-model="feedbackForm.feedbackChannel" style="width: 100%">
                  <el-option v-for="d in feedbackChannelOptions" :key="d.dictValue"
                             :label="d.dictLabel" :value="d.dictValue" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="本次报工数量" required>
                <!--
                  max 必须恒 >= min。quantityRemain 为 0 时组件会抛
                  "min should not be greater than max"，用 computed 兜底。
                -->
                <el-input-number
                  v-model="feedbackForm.quantityFeedback"
                  :min="0.000001"
                  :max="feedbackMaxQuantity"
                  :precision="2"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="合格品数量">
                <el-input-number
                  v-model="feedbackForm.quantityQualified"
                  :min="0"
                  :max="feedbackMaxQuantity"
                  :precision="2"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="不良品数量">
                <el-input-number
                  v-model="feedbackForm.quantityUnqualified"
                  :min="0"
                  :max="feedbackMaxQuantity"
                  :precision="2"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="待检测数量">
                <!-- 只读：由"本次报工 - 合格 - 不良"自动算，推给质量模块检验 -->
                <el-input :model-value="formatQty(autoUncheck)" disabled>
                  <template #append>自动算</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="生产批号">
                <el-input v-model="feedbackForm.lotNumber" placeholder="不填则沿用工单批次" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="报工时间">
                <el-date-picker
                  v-model="feedbackForm.feedbackTime"
                  type="datetime"
                  placeholder="默认当前时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="备注">
            <el-input v-model="feedbackForm.remark" type="textarea" :rows="2" />
          </el-form-item>
        </el-form>

        <!-- 三方数量对不上的话提前提示，省一次往返 -->
        <el-alert
          v-if="autoUncheck < 0"
          title="合格品 + 不良品 已超过本次报工数量，请调整"
          type="error"
          :closable="false"
          show-icon
        />

        <!-- 本批将消耗的料：倒冲（backflush）的可视化 ——
             报工人报多少，就能看到这一批要吃掉哪些料、吃多少。
             报工数量一改，本批消耗跟着变，学生一眼看懂"料是怎么被扣掉的"。 -->
        <template v-if="materialRequires.length">
          <el-divider content-position="left">本批将消耗的料（按 BOM 单位用量倒冲）</el-divider>
          <el-table :data="materialRequires" border size="small" max-height="200">
            <el-table-column prop="itemCode" label="物料编码" width="120" />
            <el-table-column prop="itemName" label="物料名称" min-width="150" show-overflow-tooltip />
            <el-table-column label="单位用量" width="95" align="right">
              <template #default="{ row }">{{ formatQty(row.unitQty) }}</template>
            </el-table-column>
            <el-table-column label="本批消耗" width="120" align="right">
              <template #default="{ row }">
                <b class="qty-remain">{{ formatQty(thisBatchQty(row.unitQty)) }}</b>
                <span class="text-muted"> {{ row.unitOfMeasure || '' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="累计已耗" width="100" align="right">
              <template #default="{ row }">{{ formatQty(row.consumedQty) }}</template>
            </el-table-column>
            <el-table-column label="应耗(满产)" width="105" align="right">
              <template #default="{ row }">{{ formatQty(row.planQty) }}</template>
            </el-table-column>
          </el-table>
        </template>

        <!-- 历史报工明细：让报工人知道之前报过什么。
             已冲销的行置灰并标注，否则容易把"已经撤掉的报工"当成有效记录 -->
        <template v-if="feedbackPreview.feedbackList && feedbackPreview.feedbackList.length">
          <el-divider content-position="left">历史报工</el-divider>
          <el-table :data="feedbackPreview.feedbackList" border size="small" max-height="200"
                    :row-class-name="feedbackRowClass">
            <el-table-column prop="feedbackCode" label="报工单号" min-width="150" />
            <el-table-column prop="feedbackType" label="类型" width="90" align="center">
              <template #default="{ row }">{{ feedbackTypeLabel(row.feedbackType) }}</template>
            </el-table-column>
            <el-table-column prop="quantityFeedback" label="报工数量" width="100" align="right">
              <template #default="{ row }">
                <span :class="{ 'qty-reversed': row.status === 'REVERSED' }">
                  {{ formatQty(row.quantityFeedback) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="quantityQualified" label="合格" width="80" align="right" />
            <el-table-column prop="quantityUnqualified" label="不良" width="80" align="right" />
            <el-table-column prop="quantityUncheck" label="待检" width="80" align="right" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'REVERSED'" type="danger" size="small" effect="plain">已冲销</el-tag>
                <span v-else class="text-muted">有效</span>
              </template>
            </el-table-column>
            <el-table-column label="报工时间" width="160">
              <template #default="{ row }">{{ shortTime(row.feedbackTime) }}</template>
            </el-table-column>
          </el-table>
        </template>
      </template>
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="feedbackLoading"
          :disabled="autoUncheck < 0"
          @click="handleFeedbackSubmit"
        >提交报工</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View, Edit, DocumentAdd } from '@element-plus/icons-vue'

import {
  getTaskByPage,
  getTaskList,
  updateTask,
  getTaskById
} from '@/api/pro/task.js'
import { getAllProcessList } from '@/api/pro/process.js'
import { getWorkstationByProcessId, getWorkstationByPage } from '@/api/md/workstation.js'
import { getDictDataListByType } from '@/api/sys/dictData.js'
import { getFeedbackPreview, submitFeedback } from '@/api/pro/feedback.js'
import { getMaterialRequire } from '@/api/pro/consume.js'

// ==================== 字典与下拉数据源 ====================

const statusOptions = ref([])
const processOptions = ref([])
const workstationOptions = ref([])
const feedbackTypeOptions = ref([])
const feedbackChannelOptions = ref([])

/** 按字典类型拉数据，只取启用的 */
async function loadDict(type, target) {
  const result = await getDictDataListByType(type)
  target.value = (result.data || []).filter(d => d.status === '0')
}

/**
 * 字典值转标签。
 * 参数用 unref 兜底：模板里传 ref 会自动解包成数组，脚本里传的是 ref 本体，
 * unref 对两者都兼容 —— 直接读 options.value 在模板调用场景下是 undefined。
 */
function dictLabel(options, value, fallback) {
  const hit = unref(options).find(d => d.dictValue === value)
  return hit ? hit.dictLabel : (fallback || value || '—')
}

const statusLabel = (v) => dictLabel(statusOptions, v)
const feedbackTypeLabel = (v) => dictLabel(feedbackTypeOptions, v)

/** 标签颜色跟字典的 list_class 走 */
function dictTagType(options, value) {
  const hit = unref(options).find(d => d.dictValue === value)
  const cls = hit?.listClass
  if (cls === 'primary' || !cls) return ''
  if (['success', 'info', 'warning', 'danger'].includes(cls)) return cls
  return ''
}

const statusTagType = (v) => dictTagType(statusOptions, v)

// ==================== 搜索与分页 ====================

const queryForm = reactive({
  workorderCode: '',
  taskCode: '',
  processId: null,
  workstationId: null,
  status: null
})

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const viewMode = ref('table')

// ==================== 数据加载 ====================

async function loadTaskList() {
  const params = { ...queryForm, pageNum: pagination.page, pageSize: pagination.size }
  const result = await getTaskByPage(params)
  tableData.value = result.data.list
  pagination.total = result.data.total
}

function handleSearch() {
  pagination.page = 1
  loadTaskList()
  if (viewMode.value === 'gantt') loadGantt()
}

function handleReset() {
  queryForm.workorderCode = ''
  queryForm.taskCode = ''
  queryForm.processId = null
  queryForm.workstationId = null
  queryForm.status = null
  handleSearch()
}

function handlePageChange(newPageNum, newPageSize) {
  pagination.page = newPageNum
  pagination.size = newPageSize
  loadTaskList()
}

// ==================== 甘特图 ====================
//
// 自绘思路：
//   1. 一次拉全当前筛选条件下的任务（不分页，分页会把同一工单的工序拆散）
//   2. 求整个时间区间的 min / max，作为横轴范围
//   3. 每条任务算 left% 与 width%，直接定位绝对定位的条形
//   4. 刻度按区间长度自适应：跨度小时按小时，跨度大时按天

const ganttRows = ref([])

/** 把 "yyyy-MM-dd HH:mm:ss" 解析成毫秒时间戳 */
function parseTime(s) {
  if (!s) return null
  const t = new Date(String(s).replace(/-/g, '/')).getTime()
  return Number.isNaN(t) ? null : t
}

async function loadGantt() {
  const result = await getTaskList({ ...queryForm })
  ganttRows.value = result.data || []
}

const gantt = computed(() => {
  const rows = ganttRows.value.filter(r => r.startTime && r.endTime)
  if (!rows.length) return { rows: [], ticks: [] }

  let min = Infinity
  let max = -Infinity
  for (const r of rows) {
    const s = parseTime(r.startTime)
    const e = parseTime(r.endTime)
    if (s === null || e === null) continue
    if (s < min) min = s
    // 结束时间用「开始 + 时长」更可靠（endTime 可能为空的边界情况）
    const byDuration = r.duration ? s + r.duration * 60000 : e
    if (byDuration > max) max = byDuration
  }
  if (!Number.isFinite(min) || !Number.isFinite(max)) return { rows: [], ticks: [] }

  // 左右各留 2% 余量，条不贴边
  const span = Math.max(max - min, 1)
  const pad = span * 0.02
  const axisMin = min - pad
  const axisSpan = span + pad * 2

  const bars = rows
    .map(r => {
      const s = parseTime(r.startTime)
      const e = r.duration ? s + r.duration * 60000 : parseTime(r.endTime)
      return {
        ...r,
        left: ((s - axisMin) / axisSpan) * 100,
        width: Math.max(((e - s) / axisSpan) * 100, 0.6),
        color: r.colorCode || '#409EFF'
      }
    })
    .sort((a, b) => parseTime(a.startTime) - parseTime(b.startTime))

  // 刻度：按总跨度选粒度，最多 6 个刻度
  const ticks = buildTicks(axisMin, axisSpan, 6)
  return { rows: bars, ticks }
})

/** 按跨度自适应生成刻度（小时 / 天） */
function buildTicks(axisMin, axisSpan, count) {
  const step = axisSpan / count
  const out = []
  for (let i = 0; i <= count; i++) {
    const t = axisMin + step * i
    const d = new Date(t)
    const sameDay = new Date(axisMin).toDateString() === new Date(axisMin + axisSpan).toDateString()
    const label = sameDay
      // 同一天内：精确到时分
      ? pad2(d.getHours()) + ':' + pad2(d.getMinutes())
      // 跨天：精确到日期
      : (d.getMonth() + 1) + '/' + d.getDate()
    out.push({ left: (step * i / axisSpan) * 100, label })
  }
  return out
}

function pad2(n) {
  return n < 10 ? '0' + n : String(n)
}

/** 甘特图条的悬浮提示 */
function tooltipOf(row) {
  return [
    `${row.taskCode}`,
    `${row.processName} · ${row.workstationName}`,
    `数量：${formatQty(row.quantity)} ${row.unitName || row.unitOfMeasure || ''}`,
    `开始：${row.startTime}`,
    `结束：${row.endTime}`,
    `时长：${formatDuration(row.duration)}`
  ].join('<br/>')
}

// ==================== 任务详情 ====================

const detailVisible = ref(false)
const detailRow = ref(null)

async function handleShowDetail(row) {
  // 重新查一次，保证拿到最新数据（列表里可能没带全字段）
  const result = await getTaskById(row.taskId)
  detailRow.value = result.data || row
  detailVisible.value = true
}

// ==================== 任务调整 ====================

const editVisible = ref(false)
const submitLoading = ref(false)
const editFormRef = ref(null)
const editWorkstationOptions = ref([])

const editForm = reactive({
  taskId: null,
  taskCode: '',
  processId: null,
  processName: '',
  workstationId: null,
  startTime: '',
  remark: ''
})

const editRules = {
  workstationId: [{ required: true, message: '请选择工作站', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择计划开始时间', trigger: 'change' }]
}

async function handleEdit(row) {
  Object.assign(editForm, {
    taskId: row.taskId,
    taskCode: row.taskCode,
    processId: row.processId,
    processName: (row.processCode || '') + ' ' + (row.processName || ''),
    workstationId: row.workstationId,
    startTime: row.startTime,
    remark: row.remark || ''
  })
  // 只列出能承担这道工序的启用工作站，防止选错
  const result = await getWorkstationByProcessId(row.processId)
  editWorkstationOptions.value = result.data || []
  editVisible.value = true
}

function handleEditClose() {
  editFormRef.value?.resetFields()
}

function handleEditSubmit() {
  editFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await updateTask({
        taskId: editForm.taskId,
        workstationId: editForm.workstationId,
        startTime: editForm.startTime,
        remark: editForm.remark
      })
      ElMessage.success('调整成功')
      editVisible.value = false
      loadTaskList()
      if (viewMode.value === 'gantt') loadGantt()
    } finally {
      submitLoading.value = false
    }
  })
}

// ==================== 报工 ====================
//
// 报工是"唯一产生真实产量"的动作：
//   写 pro_feedback  ->  累加 pro_task.quantity_produced
//                    ->  任务全部工序报满后累加 pro_workorder.quantity_produced
// 这里只负责收集用户真填的字段，其余冗余字段（工单号/产品/工序/工作站）由后端按 taskId 现查回填。

const feedbackVisible = ref(false)
const feedbackLoading = ref(false)
/** 报工预览：排产数量 / 已报工 / 还可报 / 历史报工 */
const feedbackPreview = ref(null)
/** 本道工序的用料清单（含单位用量与累计已耗），报工弹窗里展示"本批将消耗的料" */
const materialRequires = ref([])

const feedbackForm = reactive({
  taskId: null,
  feedbackType: 'PROCESS',
  feedbackChannel: 'PC',
  quantityFeedback: 1,
  quantityQualified: 0,
  quantityUnqualified: 0,
  lotNumber: '',
  feedbackTime: '',
  remark: ''
})

/**
 * 本次最多能报多少 —— 必须是 computed 而不是直接绑 preview。
 * el-input-number 在初始化时就校验 min <= max，
 * 如果 max 直接取异步值，弹窗打开瞬间是 undefined / 0，会直接抛 ElementPlusError 弹不出来。
 */
const feedbackMaxQuantity = computed(() => {
  const remain = Number(feedbackPreview.value?.quantityRemain)
  return Number.isFinite(remain) && remain > 0 ? remain : 1
})

/** 待检测数 = 本次报工 - 合格品 - 不良品，自动算，不让用户填（避免三方对不上） */
const autoUncheck = computed(() => {
  const total = Number(feedbackForm.quantityFeedback) || 0
  const q = Number(feedbackForm.quantityQualified) || 0
  const u = Number(feedbackForm.quantityUnqualified) || 0
  return Number((total - q - u).toFixed(6))
})

/** 任务是否已报满（排产数量都报完了），报满的按钮置灰 */
function isTaskFullyProduced(row) {
  const qty = Number(row.quantity)
  const produced = Number(row.quantityProduced) || 0
  return Number.isFinite(qty) && produced >= qty
}

async function handleFeedback(row) {
  // 先查预览拿到"还可报数量"，为 0 就直接拦，不进弹窗
  const result = await getFeedbackPreview(row.taskId)
  const data = result.data
  if (!data) {
    ElMessage.error('获取报工信息失败')
    return
  }
  if (!(Number(data.quantityRemain) > 0)) {
    ElMessage.warning('该任务已报满，没有可报工的数量了')
    return
  }
  feedbackPreview.value = data
  Object.assign(feedbackForm, {
    taskId: row.taskId,
    feedbackType: 'PROCESS',
    feedbackChannel: 'PC',
    // 默认报 1 件；若剩余不足 1 件就报剩余的量
    quantityFeedback: Math.min(1, Number(data.quantityRemain)),
    // 合格品默认全给合格，最省事的路径
    quantityQualified: Math.min(1, Number(data.quantityRemain)),
    quantityUnqualified: 0,
    lotNumber: '',
    feedbackTime: nowStr(),
    remark: ''
  })
  feedbackVisible.value = true
  // 顺带把本道工序的用料清单拉出来，弹窗里展示"本批将消耗的料"。
  // 取不到（测试/老化这类不投料的工序）就是空数组，模板里 v-if 自然不渲染。
  materialRequires.value = []
  try {
    const mr = await getMaterialRequire(row.taskId)
    materialRequires.value = mr.data || []
  } catch (e) {
    materialRequires.value = []
  }
}

function handleFeedbackClose() {
  feedbackPreview.value = null
  materialRequires.value = []
}

/**
 * 本批消耗 = 本次报工数量 × 单位用量
 * 与后端 ProTransConsumeServiceImpl 的倒冲口径保持一致（只算展示，真实数量以后端为准）
 */
function thisBatchQty(unitQty) {
  const total = Number(feedbackForm.quantityFeedback) || 0
  const unit = Number(unitQty) || 0
  return Number((total * unit).toFixed(6))
}

async function handleFeedbackSubmit() {
  const total = Number(feedbackForm.quantityFeedback)
  if (!(total > 0)) {
    ElMessage.warning('本次报工数量必须大于 0')
    return
  }
  if (total > feedbackMaxQuantity.value) {
    ElMessage.warning(`本次最多还能报 ${formatQty(feedbackMaxQuantity.value)}`)
    return
  }
  if (autoUncheck.value < 0) {
    ElMessage.warning('合格品数 + 不良品数不能超过本次报工数量')
    return
  }
  if (!feedbackForm.feedbackTime) {
    ElMessage.warning('请选择报工时间')
    return
  }
  feedbackLoading.value = true
  try {
    const result = await submitFeedback({ ...feedbackForm })
    const data = result.data || {}
    ElMessage.success(
      `报工成功：任务已累计生产 ${formatQty(data.taskProduced)}` +
      (Number(data.consumeRows) > 0 ? `，同时倒冲扣减了 ${data.consumeRows} 条用料` : '') +
      (data.workorderFinished === true ? '，该工单已全部完工' : '')
    )
    feedbackVisible.value = false
    feedbackPreview.value = null
    loadTaskList()
    if (viewMode.value === 'gantt') loadGantt()
  } finally {
    feedbackLoading.value = false
  }
}

/** 当前时间：yyyy-MM-dd HH:mm:ss（el-date-picker 要这个格式） */
function nowStr() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ` +
         `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// ==================== 小工具 ====================

/** 数量去掉无意义的尾部 0 */
function formatQty(v) {
  if (v === null || v === undefined || v === '') return '0'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return String(Number(n.toFixed(6)))
}

/** 报工弹窗里的历史报工表：已冲销的行置灰（那条报工的量已经扣回了） */
function feedbackRowClass({ row }) {
  return row && row.status === 'REVERSED' ? 'row-reversed' : ''
}

/** 分钟转可读时长：<60 分钟显示分钟，否则显示"X小时Y分钟" */
function formatDuration(minutes) {
  if (minutes === null || minutes === undefined) return '—'
  const m = Number(minutes)
  if (Number.isNaN(m)) return '—'
  if (m < 60) return m + ' 分钟'
  const h = Math.floor(m / 60)
  const rest = m % 60
  return rest === 0 ? h + ' 小时' : h + ' 小时 ' + rest + ' 分'
}

/** 只显示到分钟的短时间（列表列宽有限） */
function shortTime(s) {
  if (!s) return '—'
  return String(s).length > 16 ? String(s).substring(0, 16) : String(s)
}

onMounted(async () => {
  await Promise.all([
    loadDict('pro_task_status', statusOptions),
    loadDict('feedback_type', feedbackTypeOptions),
    loadDict('feedback_channel', feedbackChannelOptions),
    getAllProcessList().then(r => { processOptions.value = r.data || [] }),
    getWorkstationByPage({ pageNum: 1, pageSize: 500 }).then(r => {
      workstationOptions.value = (r.data?.list || []).filter(w => w.enableFlag === 'Y')
    })
  ])
  loadTaskList()
  loadGantt()
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
  align-items: center;
}

.search-form {
  margin-bottom: 16px;
}

.ml4 {
  margin-left: 4px;
}

.text-muted {
  color: #909399;
}

.qty-ok {
  color: #67C23A;
}

.qty-bad {
  color: #F56C6C;
}

/* 报工弹窗里的数量强调 */
.qty-scheduled {
  color: #409EFF;
}

.qty-remain {
  color: #E6A23C;
  font-size: 16px;
}

/* 已冲销的报工数量画删除线 */
.qty-reversed {
  text-decoration: line-through;
  color: #A8ABB2;
}

/* 历史报工表里已冲销的行整行置灰（el-table 的 tr 要 :deep 才能穿透） */
:deep(.row-reversed) {
  color: #A8ABB2;
}

:deep(.row-reversed) td {
  background-color: #FBFBFB;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* ==================== 甘特图 ==================== */

.gantt-toolbar {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  margin-bottom: 12px;
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
}

.batch-tip {
  color: #409EFF;
  font-size: 13px;
}

.gantt-wrap {
  display: flex;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  overflow: hidden;
}

/* 左侧工序标签列 */
.gantt-labels {
  flex: 0 0 240px;
  border-right: 1px solid #EBEEF5;
  background: #FAFAFA;
}

.gantt-label-head {
  height: 34px;
  line-height: 34px;
  padding: 0 10px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  border-bottom: 1px solid #EBEEF5;
}

.gantt-label-row {
  height: 38px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  border-bottom: 1px solid #F5F7FA;
  font-size: 12px;
  overflow: hidden;
  white-space: nowrap;
}

.gantt-label-code {
  color: #409EFF;
  font-family: monospace;
  flex: 0 0 auto;
}

.gantt-label-name {
  color: #303133;
  flex: 0 0 auto;
}

.gantt-label-ws {
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧时间轴 + 条形区 */
.gantt-body {
  flex: 1 1 auto;
  overflow-x: auto;
  min-width: 0;
}

.gantt-axis {
  position: relative;
  height: 34px;
  border-bottom: 1px solid #EBEEF5;
  background: #FAFAFA;
}

.gantt-tick {
  position: absolute;
  top: 0;
  height: 34px;
  line-height: 34px;
  font-size: 12px;
  color: #909399;
  border-left: 1px dashed #DCDFE6;
  padding-left: 4px;
  white-space: nowrap;
}

.gantt-rows {
  position: relative;
}

.gantt-grid {
  position: absolute;
  top: 0;
  bottom: 0;
  border-left: 1px dashed #F2F6FC;
  pointer-events: none;
}

.gantt-row {
  position: relative;
  height: 38px;
  border-bottom: 1px solid #F5F7FA;
}

.gantt-bar {
  position: absolute;
  top: 8px;
  height: 22px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  min-width: 3px;
  opacity: 0.9;
}

.gantt-bar:hover {
  opacity: 1;
}

.gantt-bar-text {
  color: #FFFFFF;
  font-size: 11px;
  white-space: nowrap;
  padding: 0 4px;
}
</style>
