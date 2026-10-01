<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button
      type="primary"
      :icon="Refresh"
      size="small"
      :loading="rebuilding"
      :disabled="!canRebuild"
      @click="handleRebuild"
    >重新展开用料</el-button>
    <span class="batch-tip">
      预计使用量 = BOM单位用量 × 工单生产数量，下达工单时自动生成；
      制程BOM（或产品BOM）调整后点「重新展开用料」同步（会整批覆盖，不是追加）
    </span>
  </div>

  <!-- 未下达时的提示：用料还没生成，避免看的人以为是bug -->
  <el-alert
    v-if="!canRebuild && rows.length === 0"
    :title="emptyTip"
    type="info"
    :closable="false"
    show-icon
    style="margin-bottom: 12px"
  />

  <!-- ============ 工单用料表格（只读） ============ -->
  <el-table :data="rows" border stripe v-loading="loading">
    <el-table-column label="序号" width="70" align="center">
      <template #default="{ $index }">
        <b>{{ $index + 1 }}</b>
      </template>
    </el-table-column>
    <el-table-column prop="itemCode" label="物料编号" min-width="150" show-overflow-tooltip />
    <el-table-column prop="itemName" label="物料名称" min-width="180" show-overflow-tooltip />
    <el-table-column prop="itemSpc" label="规格型号" min-width="150" show-overflow-tooltip>
      <template #default="{ row }">
        {{ row.itemSpc || '—' }}
      </template>
    </el-table-column>
    <el-table-column label="物料类型" width="100" align="center">
      <template #default="{ row }">
        <el-tag v-if="row.itemOrProduct === 'PRODUCT'" type="warning" size="small" effect="plain">半成品</el-tag>
        <el-tag v-else type="info" size="small" effect="plain">物料</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="预计使用量" width="150" align="right">
      <template #default="{ row }">
        <b>{{ formatQty(row.quantity) }}</b>
        <span class="unit-text">{{ row.unitName || row.unitOfMeasure || '' }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
      <template #default="{ row }">
        {{ row.remark || '—' }}
      </template>
    </el-table-column>
  </el-table>

  <!-- 合计行单独放，让"这张单总共要多少料"一眼可见 -->
  <div v-if="rows.length" class="total-bar">
    共 <b>{{ rows.length }}</b> 种用料；产品数量
    <b>{{ formatQty(props.workorder?.quantity) }}</b>
    {{ props.workorder?.unitOfMeasure || '' }}
  </div>
</template>

<script setup>
import {ref, computed, watch} from 'vue';
import {Refresh} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {getWorkorderBomList, rebuildWorkorderBom} from "@/api/pro/workorder.js";

const props = defineProps(["workorder"]);
const emit = defineEmits(["rebuild"]);

/** 用料明细行 */
const rows = ref([]);
const loading = ref(false);
const rebuilding = ref(false);

/**
 * 能不能重算用料：
 * 已完工/已取消的单子后端会拒绝（重算没有意义还会打乱历史），
 * 前端先把按钮禁掉，别让用户点出一个必然失败的请求
 */
const canRebuild = computed(() => {
  const status = props.workorder?.status;
  return status === 'CONFIRMED';
});

/** 空数据的提示语按状态区分，说清楚"为什么没数据" */
const emptyTip = computed(() => {
  const status = props.workorder?.status;
  if (status === 'PREPARE') {
    return '工单还没有下达，用料要等下达时自动展开（优先按制程BOM，没挂制程则按产品BOM）。请回列表页点「下达」。';
  }
  if (status === 'FINISHED' || status === 'CANCELED') {
    return '工单已' + (status === 'FINISHED' ? '完工' : '取消') + '，没有用料记录。';
  }
  return '暂无用料数据。';
});

/** 加载用料明细 */
const loadList = async function () {
  if (!props.workorder?.workorderId) return;
  loading.value = true;
  try {
    const response = await getWorkorderBomList(props.workorder.workorderId);
    rows.value = response.data || [];
  } finally {
    loading.value = false;
  }
};

/** 重新展开用料：整批覆盖，先提醒清楚 */
const handleRebuild = function () {
  ElMessageBox.confirm(
    '重新展开会按当前的制程BOM（没挂制程则按产品BOM）整批覆盖现有用料行，手工调整过的用量会丢失。确定继续吗？',
    '重新展开用料',
    {confirmButtonText: '确定重新展开', cancelButtonText: '取消', type: 'warning'}
  ).then(async () => {
    rebuilding.value = true;
    try {
      const response = await rebuildWorkorderBom(props.workorder.workorderId);
      ElMessage.success('已重新展开 ' + response.data + ' 行用料');
      await loadList();
      // 通知父组件：主表数据本身没变，但保持一个刷新出口，后续加工单数量统计时不用再改子组件
      emit('rebuild');
    } finally {
      rebuilding.value = false;
    }
  }).catch(() => {});
};

/** 数量去掉无意义的尾部 0，1.000000 显示成 1 */
const formatQty = function (v) {
  if (v === null || v === undefined || v === '') return '0';
  const n = Number(v);
  if (Number.isNaN(n)) return String(v);
  return String(Number(n.toFixed(6)));
};

/** 监听父组件传进来的工单，工单一变就重新加载 */
watch(() => props.workorder, (newVal) => {
  if (newVal?.workorderId) {
    loadList();
  }
}, {deep: true, immediate: true});
</script>

<style scoped>
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
  font-size: 13px;
}

.unit-text {
  color: #909399;
  margin-left: 4px;
}

.total-bar {
  margin-top: 10px;
  padding: 8px 4px;
  color: #606266;
  font-size: 13px;
}

.total-bar b {
  color: #409EFF;
  margin: 0 2px;
}
</style>
