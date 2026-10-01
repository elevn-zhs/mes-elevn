<template>
  <!-- 未排产时的提示：说清为什么没数据，避免看的人以为是 bug -->
  <el-alert
    v-if="!rows.length"
    :title="emptyTip"
    type="info"
    :closable="false"
    show-icon
    style="margin-bottom: 12px"
  />

  <!-- ============ 工序任务表格 ============ -->
  <el-table v-else :data="rows" border stripe v-loading="loading">
    <el-table-column label="序位" width="70" align="center">
      <template #default="{ $index }">
        <b>{{ $index + 1 }}</b>
      </template>
    </el-table-column>
    <el-table-column prop="taskCode" label="任务编号" min-width="170" show-overflow-tooltip />
    <el-table-column label="工序" min-width="130">
      <template #default="{ row }">
        <el-tag effect="plain" size="small">{{ row.processCode }}</el-tag>
        <span class="ml4">{{ row.processName }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="workstationName" label="工作站" min-width="140" show-overflow-tooltip />
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
    <el-table-column label="时长" width="110" align="right">
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
  </el-table>

  <!-- 工序链说明：让"为什么时间是这么排的"可解释 -->
  <div v-if="rows.length" class="chain-tip">
    共 <b>{{ rows.length }}</b> 道工序任务，从
    <b>{{ shortTime(rows[0].startTime) }}</b> 开始，到
    <b>{{ shortTime(lastEndTime) }}</b> 结束（按每道工序的工时依次推算）
  </div>
</template>

<script setup>
import {ref, computed, watch} from 'vue';
import {getTaskListByWorkorder} from "@/api/pro/task.js";
import {getDictDataListByType} from "@/api/sys/dictData.js";

const props = defineProps(["workorder"]);

/** 任务行 */
const rows = ref([]);
const loading = ref(false);

/** 任务状态字典 */
const statusOptions = ref([]);

const statusLabel = (v) => {
  const hit = statusOptions.value.find(d => d.dictValue === v);
  return hit ? hit.dictLabel : (v || '—');
};

const statusTagType = (v) => {
  const hit = statusOptions.value.find(d => d.dictValue === v);
  const cls = hit?.listClass;
  if (cls === 'primary' || !cls) return '';
  if (['success', 'info', 'warning', 'danger'].includes(cls)) return cls;
  return '';
};

/** 空数据提示按工单状态与类型区分，说清"为什么没有任务" */
const emptyTip = computed(() => {
  const status = props.workorder?.status;
  const type = props.workorder?.workorderType;
  if (type && type !== 'SELF') {
    return '这是外协/外购工单，不需要排产。只有自产工单才拆工序任务。';
  }
  if (status === 'PREPARE') {
    return '工单还没有下达，需要先「下达」才能排产。';
  }
  if (status === 'CONFIRMED') {
    return '该工单还没有排产。请回工单列表点「排产」，为每道工序指定工作站。';
  }
  if (status === 'FINISHED') {
    return '工单已完工，任务数据已归档（若需查看请到生产任务列表按工单编码筛选）。';
  }
  if (status === 'CANCELED') {
    return '工单已取消，没有排产任务。';
  }
  return '暂无工序任务。';
});

/** 末道工序的结束时间 = 整张工单的计划完工时间 */
const lastEndTime = computed(() => {
  if (!rows.value.length) return '';
  return rows.value[rows.value.length - 1].endTime || '';
});

/** 加载该工单的任务列表 */
const loadList = async function () {
  if (!props.workorder?.workorderId) return;
  loading.value = true;
  try {
    const response = await getTaskListByWorkorder(props.workorder.workorderId);
    rows.value = response.data || [];
  } finally {
    loading.value = false;
  }
};

/** 数量去掉无意义的尾部 0 */
const formatQty = function (v) {
  if (v === null || v === undefined || v === '') return '0';
  const n = Number(v);
  if (Number.isNaN(n)) return String(v);
  return String(Number(n.toFixed(6)));
};

/** 分钟转可读时长 */
const formatDuration = function (minutes) {
  if (minutes === null || minutes === undefined) return '—';
  const m = Number(minutes);
  if (Number.isNaN(m)) return '—';
  if (m < 60) return m + ' 分钟';
  const h = Math.floor(m / 60);
  const rest = m % 60;
  return rest === 0 ? h + ' 小时' : h + ' 小时 ' + rest + ' 分';
};

/** 只显示到分钟的短时间 */
const shortTime = function (s) {
  if (!s) return '—';
  return String(s).length > 16 ? String(s).substring(0, 16) : String(s);
};

getDictDataListByType('pro_task_status').then(r => {
  statusOptions.value = (r.data || []).filter(d => d.status === '0');
});

/** 监听父组件传进来的工单，工单一变就重新加载 */
watch(() => props.workorder, (newVal) => {
  if (newVal?.workorderId) {
    loadList();
  }
}, {deep: true, immediate: true});
</script>

<style scoped>
.ml4 {
  margin-left: 4px;
}

.text-muted {
  color: #909399;
}

.qty-ok {
  color: #67C23A;
}

.chain-tip {
  margin-top: 10px;
  padding: 8px 4px;
  color: #606266;
  font-size: 13px;
}

.chain-tip b {
  color: #409EFF;
  margin: 0 2px;
}
</style>
