<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="small">挂接工艺路线</el-button>
    <span class="batch-tip">
      产品制程 = 这个产品生产时走哪条路线；工单下达时按它拆工序任务、按用料明细展开领料
    </span>
  </div>

  <!-- ============ 产品制程表格 ============ -->
  <el-table :data="tableData" border stripe>
    <el-table-column label="路线编码" min-width="130">
      <template #default="{ row }">{{ routeNameOf(row.routeId).routeCode || '—' }}</template>
    </el-table-column>
    <el-table-column label="路线名称" min-width="180">
      <template #default="{ row }">{{ routeNameOf(row.routeId).routeName || '路线已删除' }}</template>
    </el-table-column>
    <el-table-column prop="quantity" label="生产数量" width="110" align="right" />
    <el-table-column label="生产用时" width="130" align="right">
      <template #default="{ row }">{{ row.productionTime }} {{ timeUnitName(row.timeUnitType) }}</template>
    </el-table-column>
    <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
    <el-table-column prop="createTime" label="创建时间" width="180" />
    <el-table-column label="操作" width="220" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" :icon="SetUp" @click="handleBom(row)">用料明细</el-button>
        <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
        <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- ============ 挂接/编辑路线的表单弹窗 ============ -->
  <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑制程' : '挂接工艺路线'" width="560px">
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
      <el-form-item label="工艺路线" prop="routeId">
        <!-- 修改时不允许换路线：后端会忽略 routeId，前端直接禁用 -->
        <el-select v-model="form.routeId" placeholder="选择工艺路线" filterable :disabled="isEdit" style="width: 100%">
          <el-option v-for="r in routeOptions" :key="r.routeId"
                     :label="r.routeCode + ' / ' + r.routeName" :value="r.routeId" />
        </el-select>
      </el-form-item>
      <el-form-item label="生产数量" prop="quantity">
        <el-input-number v-model="form.quantity" :min="1" :step="1" controls-position="right" />
        <span class="form-tip">按这个数量组织一次生产</span>
      </el-form-item>
      <el-form-item label="生产用时" prop="productionTime">
        <el-input-number v-model="form.productionTime" :min="0.01" :precision="2" :step="0.5" controls-position="right" />
        <el-select v-model="form.timeUnitType" style="width: 90px; margin-left: 8px">
          <el-option label="小时" value="H" />
          <el-option label="分钟" value="M" />
          <el-option label="天" value="D" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formDialogVisible = false" type="info">取消</el-button>
      <el-button @click="handleSubmit" type="primary">确定</el-button>
    </template>
  </el-dialog>

  <!-- ============ 用料明细（制程BOM）整表编辑弹窗 ============ -->
  <el-dialog v-model="bomDialogVisible" :title="bomTitle" width="75%">
    <div class="batch-toolbar">
      <el-button type="primary" @click="handleAddBomRow" :icon="Plus" size="small">添加用料</el-button>
      <el-button type="success" @click="handleSaveBom" :icon="Check" size="small" :loading="bomSaving">保存用料明细</el-button>
      <span class="batch-tip">用量是「单套比例」：生产数量里每做一套，这道工序消耗这么多</span>
    </div>
    <el-table :data="bomRows" border stripe>
      <el-table-column label="工序" min-width="200">
        <template #default="{ row }">
          <el-select v-model="row.processId" placeholder="选择工序" filterable style="width: 100%">
            <el-option v-for="p in routeProcessOptions" :key="p.processId"
                       :label="'第' + p.orderNum + '步 ' + p.processCode + ' ' + p.processName" :value="p.processId" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="物料" min-width="200">
        <template #default="{ row }">
          <el-select v-model="row.itemId" placeholder="选择物料"  style="width: 100%">
            <el-option v-for="i in itemOptions" :key="i.itemId"
                       :label="i.itemCode + ' / ' + i.itemName" :value="i.itemId" >
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="单套用量" width="170">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="0.0001" :precision="6" :step="0.1"
                           controls-position="right" style="width: 140px" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ $index }">
          <el-button link type="danger" :icon="Delete" @click="bomRows.splice($index, 1)" />
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import {ref, reactive, computed, watch} from 'vue';
import {Plus, Check, Delete, Edit, SetUp} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {
  getRouteProductListByItemId,
  createRouteProduct,
  updateRouteProduct,
  deleteRouteProduct
} from "@/api/pro/routeProduct.js";
import {getRouteProductBomList, batchSaveRouteProductBom} from "@/api/pro/routeProductBom.js";
import {getAllRouteList} from "@/api/pro/route.js";
import {getRouteProcessListByRouteId} from "@/api/pro/routeProcess.js";
import {getItemPageForBom} from "@/api/md/item.js";

const props = defineProps(["item"]);

/** 制程列表 */
const tableData = ref([]);
/** 路线下拉数据源（同时用来把 routeId 翻译成名称） */
const routeOptions = ref([]);

/** 挂接/编辑表单弹窗 */
const formDialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref();
const defaultForm = () => ({
  recordId: null,
  routeId: null,
  quantity: 1,
  productionTime: 1,
  timeUnitType: 'H',
  remark: ''
});
const form = ref(defaultForm());
const formRules = {
  routeId: [{required: true, message: '请选择工艺路线', trigger: 'change'}],
  quantity: [{required: true, message: '请填写生产数量', trigger: 'blur'}],
  productionTime: [{required: true, message: '请填写生产用时', trigger: 'blur'}]
};

/** 用料明细弹窗 */
const bomDialogVisible = ref(false);
const bomSaving = ref(false);
const bomRows = ref([]);
const bomRoute = ref(null);
const routeProcessOptions = ref([]);
const itemOptions = ref([]);
const bomTitle = computed(() =>
    '用料明细 —— ' + (routeNameOf(bomRoute.value)?.routeName || ''));

/** routeId -> 路线对象（表格显示名称用） */
const routeNameOf = function (routeId) {
  return routeOptions.value.find(r => r.routeId === routeId) || {};
};

/** 用时单位编码转显示名 */
const timeUnitName = function (type) {
  return {H: '小时', M: '分钟', D: '天'}[type] || type;
};

/** 加载当前产品的制程列表 */
const loadList = async function () {
  if (!props.item?.itemId) return;
  const response = await getRouteProductListByItemId(props.item.itemId);
  tableData.value = response.data || [];
};

/** 加载路线下拉（一次拉全量，数据量小） */
const loadRouteOptions = async function () {
  const response = await getAllRouteList();
  routeOptions.value = response.data || [];
};

const handleAdd = function () {
  isEdit.value = false;
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, defaultForm());
  formDialogVisible.value = true;
};

const handleEdit = function (row) {
  isEdit.value = true;
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, row);
  formDialogVisible.value = true;
};

const handleSubmit = async function () {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    if (isEdit.value) {
      await updateRouteProduct(form.value);
      ElMessage({type: "success", message: "修改成功"});
    } else {
      // 产品ID由详情页传入；产品编码/名称等冗余字段由后端回填
      form.value.itemId = props.item.itemId;
      await createRouteProduct(form.value);
      ElMessage({type: "success", message: "挂接成功"});
    }
    formDialogVisible.value = false;
    loadList();
  });
};

const handleDelete = function (row) {
  ElMessageBox.confirm(
      "删除制程会连同该路线下的用料明细一起删除，确定吗？", '删除警告',
      {confirmButtonText: '确定', cancelButtonText: '点错了', type: 'warning'}
  ).then(async () => {
    await deleteRouteProduct(row.recordId);
    ElMessage({type: "info", message: "删除完成"});
    loadList();
  });
};

/** 打开用料明细弹窗：加载路线的工序、可选用料、已有BOM行 */
const handleBom = async function (row) {
  bomRoute.value = row.routeId;
  // 工序下拉 = 该路线的工序明细
  const rp = await getRouteProcessListByRouteId(row.routeId);
  routeProcessOptions.value = rp.data || [];
  // 物料下拉 = 排除产品自己的全部物料（复用 BOM 选料接口）
  const items = await getItemPageForBom({excludeItemId: props.item.itemId, pageNum: 1, pageSize: 500});
  itemOptions.value = items.data.list || [];
  console.log("itemOptions",itemOptions.value)
  // 已有BOM行
  const bom = await getRouteProductBomList(props.item.itemId, row.routeId);
  bomRows.value = (bom.data || []).map(b => ({processId: b.processId, itemId: b.itemId, quantity: Number(b.quantity)}));
  console.log("bomRows",bomRows.value)
  bomDialogVisible.value = true;
};

const handleAddBomRow = function () {
  bomRows.value.push({processId: null, itemId: null, quantity: 1});
};

const handleSaveBom = async function () {
  const empty = bomRows.value.findIndex(r => !r.processId || !r.itemId);
  if (empty >= 0) {
    ElMessage({type: "warning", message: "第 " + (empty + 1) + " 行的工序或物料还没选"});
    return;
  }
  bomSaving.value = true;
  try {
    await batchSaveRouteProductBom(props.item.itemId, bomRoute.value, bomRows.value);
    ElMessage({type: "success", message: "保存成功"});
    bomDialogVisible.value = false;
  } finally {
    bomSaving.value = false;
  }
};

loadRouteOptions();

/** 监听父组件传进来的产品，产品一变就重新加载 */
watch(() => props.item, (newVal) => {
  if (newVal?.itemId) {
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

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}
</style>
