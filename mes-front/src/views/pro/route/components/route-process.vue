<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button type="primary" @click="handleAddRow" :icon="Plus" size="small">添加工序</el-button>
    <el-button type="success" @click="handleSaveAll" :icon="Check" size="small" :loading="saving">保存整表</el-button>
    <span class="batch-tip">
      下一道工序 = 表格的下一行，末行就是最后一道；改完顺序点「保存整表」一次性提交
    </span>
  </div>

  <!-- ============ 路线工序编辑表格 ============ -->
  <el-table ref="tableRef" :data="rows" border stripe>
    <el-table-column label="顺序" width="70" align="center">
      <template #default="{ $index }">
        <b>{{ $index + 1 }}</b>
      </template>
    </el-table-column>
    <el-table-column label="工序" min-width="200">
      <template #default="{ row }">
        <el-select v-model="row.processId" placeholder="选择工序" filterable style="width: 100%">
          <el-option v-for="p in processOptions" :key="p.processId"
                     :label="p.processCode + ' / ' + p.processName" :value="p.processId" />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="下一道工序" min-width="140">
      <template #default="{ $index }">
        <span v-if="$index < rows.length - 1">{{ processNameOf(rows[$index + 1].processId) }}</span>
        <el-tag v-else type="info" size="small">末道</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="衔接方式" width="130">
      <template #default="{ row }">
        <el-select v-model="row.linkType" placeholder="正常流转" clearable style="width: 100%">
          <el-option label="正常流转" value="NORMAL" />
          <el-option label="并行" value="PARALLEL" />
          <el-option label="可跳过" value="SKIP" />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="准备(分)" width="120">
      <template #default="{ row }">
        <el-input-number v-model="row.defaultPreTime" :min="0" :step="1" controls-position="right" style="width: 90px" />
      </template>
    </el-table-column>
    <el-table-column label="等待(分)" width="120">
      <template #default="{ row }">
        <el-input-number v-model="row.defaultSufTime" :min="0" :step="1" controls-position="right" style="width: 90px" />
      </template>
    </el-table-column>
    <el-table-column label="甘特颜色" width="110" align="center">
      <template #default="{ row }">
        <el-color-picker v-model="row.colorCode" />
      </template>
    </el-table-column>
    <el-table-column label="关键工序" width="100" align="center">
      <template #default="{ row }">
        <el-switch v-model="row.isKey" />
      </template>
    </el-table-column>
    <el-table-column label="需检验" width="90" align="center">
      <template #default="{ row }">
        <el-switch v-model="row.needCheck" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="130" fixed="right">
      <template #default="{ $index }">
        <el-button link type="primary" :icon="Top" :disabled="$index === 0" @click="moveRow($index, -1)" />
        <el-button link type="primary" :icon="Bottom" :disabled="$index === rows.length - 1" @click="moveRow($index, 1)" />
        <el-button link type="danger" :icon="Delete" @click="removeRow($index)" />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import {ref, watch} from 'vue';
import {Plus, Check, Delete, Top, Bottom} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {getRouteProcessListByRouteId, batchSaveRouteProcess} from "@/api/pro/routeProcess.js";
import {getAllProcessList} from "@/api/pro/process.js";

const props = defineProps(["route"]);

/** 可编辑行列表。本地行里用 isKey / needCheck 两个布尔值驱动开关，
 *  提交时再换算成后端的 keyFlag / isCheck（Y/N 或字典值），避免直接绑字符串 */
const rows = ref([]);
/** 全部启用工序（下拉数据源） */
const processOptions = ref([]);
const saving = ref(false);

/** 工序ID转显示名：下一道工序列用 */
const processNameOf = function (processId) {
  const p = processOptions.value.find(o => o.processId === processId);
  return p ? p.processCode + ' / ' + p.processName : '未知工序';
};

/** 本地行 -> 后端明细对象（顺序号在提交时按行序重排） */
const toBackend = function () {
  return rows.value.map((row, index) => ({
    processId: row.processId,
    orderNum: index + 1,
    linkType: row.linkType || null,
    defaultPreTime: row.defaultPreTime,
    defaultSufTime: row.defaultSufTime,
    colorCode: row.colorCode || null,
    keyFlag: row.isKey ? 'KEY_PROCESS' : 'KEY',
    isCheck: row.needCheck ? 'Y' : 'N'
  }));
};

/** 加载已有明细，转成本地可编辑行 */
const loadList = async function () {
  if (!props.route?.routeId) return;
  const response = await getRouteProcessListByRouteId(props.route.routeId);
  rows.value = (response.data || []).map(item => ({
    processId: item.processId,
    linkType: item.linkType,
    defaultPreTime: item.defaultPreTime ?? 0,
    defaultSufTime: item.defaultSufTime ?? 0,
    colorCode: item.colorCode,
    isKey: item.keyFlag === 'KEY_PROCESS',
    needCheck: item.isCheck === 'Y'
  }));
};

/** 加载工序下拉 */
const loadProcessOptions = async function () {
  const response = await getAllProcessList();
  processOptions.value = response.data || [];
};

/** 添加一行：默认排在最后 */
const handleAddRow = function () {
  rows.value.push({
    processId: null,
    linkType: 'NORMAL',
    defaultPreTime: 0,
    defaultSufTime: 0,
    colorCode: '#409EFF',
    isKey: false,
    needCheck: false
  });
};

/** 上移/下移：直接交换数组元素，顺序号提交时按行序生成 */
const moveRow = function (index, offset) {
  const target = index + offset;
  if (target < 0 || target >= rows.value.length) return;
  const tmp = rows.value[index];
  rows.value[index] = rows.value[target];
  rows.value[target] = tmp;
};

const removeRow = function (index) {
  rows.value.splice(index, 1);
};

/** 整批保存：顺序号按行序生成，链的校验交给后端 */
const handleSaveAll = async function () {
  if (rows.value.length === 0) {
    ElMessage({type: "warning", message: "至少添加一道工序"});
    return;
  }
  const empty = rows.value.findIndex(r => !r.processId);
  if (empty >= 0) {
    ElMessage({type: "warning", message: "第 " + (empty + 1) + " 行还没有选择工序"});
    return;
  }
  saving.value = true;
  try {
    await batchSaveRouteProcess(props.route.routeId, toBackend());
    ElMessage({type: "success", message: "保存成功"});
    loadList();
  } finally {
    saving.value = false;
  }
};

loadProcessOptions();

/** 监听父组件传进来的路线，路线一变就重新加载 */
watch(() => props.route, (newVal) => {
  if (newVal?.routeId) {
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
</style>
