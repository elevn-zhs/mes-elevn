<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="small">绑定设备</el-button>
    <el-button type="danger" :icon="Delete" size="small" @click="handleBatchDelete">批量删除</el-button>
    <span class="batch-tip">
      同一台设备绑定一次即可，数量里填台数；重复绑定会被后端拒绝
    </span>
  </div>

  <!-- ============ 设备绑定表格 ============ -->
  <el-table ref="tableRef" :data="tableData" border stripe @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50" />
    <el-table-column prop="machineryCode" label="设备编码" min-width="130" show-overflow-tooltip />
    <el-table-column prop="machineryName" label="设备名称" min-width="180" show-overflow-tooltip />
    <el-table-column prop="quantity" label="数量(台)" width="100" align="right" />
    <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
    <el-table-column prop="createTime" label="创建时间" width="180" />
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
        <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- ============ 选择设备的弹窗 ============ -->
  <el-dialog v-model="selectDialogVisible" title="选择设备" width="70%">
    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item label="设备编码">
        <el-input v-model="queryForm.machineryCode" placeholder="请输入设备编码" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item label="设备名称">
        <el-input v-model="queryForm.machineryName" placeholder="请输入设备名称" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="machineryList" border stripe>
      <el-table-column label="选择" width="70" align="center">
        <template #default="{ row }">
          <el-radio v-model="selectedId" :value="row.machineryId" @change="handleRadioChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="machineryCode" label="设备编码" min-width="130" show-overflow-tooltip />
      <el-table-column prop="machineryName" label="设备名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="machinerySpec" label="规格型号" min-width="130" show-overflow-tooltip />
      <el-table-column prop="machineryTypeName" label="设备类型" min-width="120" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'RUN' ? 'success' : 'info'">
            {{ row.status === 'RUN' ? '运行' : row.status === 'MAINT' ? '保养中' : row.status === 'SCRAP' ? '报废' : '停机' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="machineryPagination.page"
          v-model:page-size="machineryPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="machineryPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="handleMachineryPageChange"
      />
    </div>

    <template #footer>
      <el-button @click="handleSelectOk" type="primary">确定</el-button>
      <el-button @click="selectDialogVisible = false" type="info">取消</el-button>
    </template>
  </el-dialog>

  <!-- ============ 填写绑定数量的表单弹窗 ============ -->
  <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑设备绑定' : '绑定设备'" width="520px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="设备">
        <el-input :value="form.machineryCode + ' / ' + form.machineryName" disabled
                  :placeholder="isEdit ? '' : '请先在弹窗中选择设备'" />
      </el-form-item>
      <el-form-item label="数量(台)">
        <el-input-number v-model="form.quantity" :min="1" :step="1" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleSubmit" type="primary">确定</el-button>
      <el-button @click="formDialogVisible = false" type="info">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, reactive, watch} from 'vue';
import {Plus, Delete, Edit, Search, Refresh} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {
  getWorkstationMachineById,
  getWorkstationMachineListByWorkstationId,
  createWorkstationMachine,
  updateWorkstationMachine,
  deleteWorkstationMachine,
  deleteWorkstationMachineBatch
} from "@/api/md/workstationMachine.js";
import {getMachineryByPage} from "@/api/dv/machinery.js";

const props = defineProps(["workstation"]);
const tableRef = ref();

/** 列表数据（按工作站一次性查全量，子表数据量小，不分页） */
const tableData = ref([]);
const selectedRows = ref([]);

/** 选设备弹窗相关 */
const selectDialogVisible = ref(false);
const machineryList = ref([]);
const machineryPagination = reactive({page: 1, size: 10, total: 0});
const selectedId = ref();
const selectedMachinery = ref({});
const queryForm = reactive({
  machineryCode: '',
  machineryName: '',
});

/** 表单弹窗相关 */
const formDialogVisible = ref(false);
const isEdit = ref(false);
const defaultForm = () => ({
  recordId: null,
  workstationId: null,
  machineryId: null,
  machineryCode: '',
  machineryName: '',
  quantity: 1,
  remark: ''
});
const form = ref(defaultForm());

/** 重置表单：先删掉所有键再灌默认值，避免残留上一次编辑的字段 */
const resetForm = function () {
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, defaultForm());
};

/** 加载当前工作站的设备绑定列表 */
const loadList = async function () {
  if (!props.workstation?.workstationId) return;
  const response = await getWorkstationMachineListByWorkstationId(props.workstation.workstationId);
  tableData.value = response.data || [];
};

/** 加载设备候选列表 */
const loadMachineryList = async function () {
  const params = {
    ...queryForm,
    pageNum: machineryPagination.page,
    pageSize: machineryPagination.size
  };
  const result = await getMachineryByPage(params);
  machineryList.value = result.data.list;
  machineryPagination.total = result.data.total;
};

const handleMachineryPageChange = function (newPage) {
  machineryPagination.page = newPage;
  loadMachineryList();
};

const handleSearch = function () {
  machineryPagination.page = 1;
  loadMachineryList();
};

const handleReset = function () {
  queryForm.machineryCode = '';
  queryForm.machineryName = '';
  handleSearch();
};

const handleRadioChange = function (row) {
  selectedMachinery.value = row;
};

const handleAdd = function () {
  isEdit.value = false;
  resetForm();
  selectedId.value = undefined;
  selectedMachinery.value = {};
  selectDialogVisible.value = true;
  loadMachineryList();
};

const handleSelectOk = function () {
  if (!selectedId.value) {
    ElMessage({type: "warning", message: "请先选择一台设备"});
    return;
  }
  // 冗余字段前端先带上，后端保存时还会重新校验
  form.value.machineryId = selectedMachinery.value.machineryId;
  form.value.machineryCode = selectedMachinery.value.machineryCode;
  form.value.machineryName = selectedMachinery.value.machineryName;
  selectDialogVisible.value = false;
  formDialogVisible.value = true;
};

const handleEdit = async function (row) {
  const response = await getWorkstationMachineById(row.recordId);
  if (!response.data) {
    ElMessage({type: "warning", message: "数据有误，请刷新页面重试"});
    return;
  }
  resetForm();
  Object.assign(form.value, response.data);
  isEdit.value = true;
  formDialogVisible.value = true;
};

const handleSubmit = async function () {
  form.value.workstationId = props.workstation.workstationId;
  if (isEdit.value) {
    await updateWorkstationMachine(form.value);
    ElMessage({type: "success", message: "修改成功"});
  } else {
    await createWorkstationMachine(form.value);
    ElMessage({type: "success", message: "绑定成功"});
  }
  formDialogVisible.value = false;
  loadList();
};

const handleDelete = function (row) {
  ElMessageBox.confirm("确定要解除设备[" + row.machineryName + "]的绑定吗？", '删除警告', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    await deleteWorkstationMachine(row.recordId);
    ElMessage({type: "info", message: "删除完成"});
    loadList();
  });
};

const handleSelectionChange = function (rows) {
  selectedRows.value = rows;
};

const handleBatchDelete = function () {
  if (selectedRows.value.length === 0) {
    ElMessage({type: "warning", message: "请先选择要删除的数据"});
    return;
  }
  ElMessageBox.confirm("确定要解除选中的 " + selectedRows.value.length + " 条设备绑定吗？", '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    const ids = selectedRows.value.map(r => r.recordId);
    await deleteWorkstationMachineBatch(ids);
    ElMessage({type: "info", message: "删除完成"});
    loadList();
  });
};

/** 监听父组件传进来的工作站，工作站一变就重新加载 */
watch(() => props.workstation, (newVal) => {
  if (newVal?.workstationId) {
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

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
