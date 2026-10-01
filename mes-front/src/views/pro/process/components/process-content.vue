<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="small">新增步骤</el-button>
    <el-button type="danger" :icon="Delete" size="small" @click="handleBatchDelete">批量删除</el-button>
    <span class="batch-tip">
      工序步骤按顺序号从小到大展示，作业步骤一条一条往里加
    </span>
  </div>

  <!-- ============ 工序内容表格 ============ -->
  <el-table ref="tableRef" :data="tableData" border stripe @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50" />
    <el-table-column prop="orderNum" label="顺序" width="80" align="center" />
    <el-table-column prop="contentText" label="内容说明" min-width="220" show-overflow-tooltip />
    <el-table-column prop="device" label="辅助设备" min-width="130" show-overflow-tooltip />
    <el-table-column prop="material" label="辅助材料" min-width="130" show-overflow-tooltip />
    <el-table-column prop="docUrl" label="文档地址" min-width="130" show-overflow-tooltip />
    <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
    <el-table-column prop="createTime" label="创建时间" width="180" />
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
        <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- ============ 新增/编辑内容的表单弹窗 ============ -->
  <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑工序内容' : '新增工序内容'" width="560px">
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
      <el-form-item label="顺序号" prop="orderNum">
        <el-input-number v-model="form.orderNum" :min="1" :step="1" />
      </el-form-item>
      <el-form-item label="内容说明" prop="contentText">
        <el-input v-model="form.contentText" type="textarea" :rows="3"
                  placeholder="这一步具体做什么，如：将主板放入治具，锁附 4 颗螺丝" />
      </el-form-item>
      <el-form-item label="辅助设备">
        <el-input v-model="form.device" placeholder="本步骤用到的辅助设备，可空" />
      </el-form-item>
      <el-form-item label="辅助材料">
        <el-input v-model="form.material" placeholder="本步骤用到的辅助材料，可空" />
      </el-form-item>
      <el-form-item label="文档地址">
        <el-input v-model="form.docUrl" placeholder="说明图或文档链接，可空" />
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
</template>

<script setup>
import {ref, watch} from 'vue';
import {Plus, Delete, Edit} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {
  getProcessContentListByProcessId,
  createProcessContent,
  updateProcessContent,
  deleteProcessContent,
  deleteProcessContentBatch
} from "@/api/pro/processContent.js";

const props = defineProps(["process"]);
const tableRef = ref();
const formRef = ref();

/** 列表数据（按工序一次性查全量，子表数据量小，不分页） */
const tableData = ref([]);
const selectedRows = ref([]);

/** 表单弹窗相关 */
const formDialogVisible = ref(false);
const isEdit = ref(false);
const defaultForm = () => ({
  contentId: null,
  processId: null,
  orderNum: 1,
  contentText: '',
  device: '',
  material: '',
  docUrl: '',
  remark: ''
});
const form = ref(defaultForm());

/** 重置表单：先删掉所有键再灌默认值，避免残留上一次编辑的字段 */
const resetForm = function () {
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, defaultForm());
};

const formRules = {
  orderNum: [
    { required: true, message: '请填写顺序号', trigger: 'blur' }
  ],
  contentText: [
    { required: true, message: '请填写内容说明', trigger: 'blur' },
    { max: 500, message: '内容说明长度不能超过 500 个字符', trigger: 'blur' }
  ]
};

/** 加载当前工序的内容列表 */
const loadList = async function () {
  if (!props.process?.processId) return;
  const response = await getProcessContentListByProcessId(props.process.processId);
  tableData.value = response.data || [];
};

const handleAdd = function () {
  isEdit.value = false;
  resetForm();
  // 默认顺序号 = 已有条数 + 1，省得用户自己数
  form.value.orderNum = tableData.value.length + 1;
  formDialogVisible.value = true;
};

const handleEdit = async function (row) {
  resetForm();
  Object.assign(form.value, row);
  isEdit.value = true;
  formDialogVisible.value = true;
};

const handleSubmit = async function () {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    form.value.processId = props.process.processId;
    if (isEdit.value) {
      await updateProcessContent(form.value);
      ElMessage({type: "success", message: "修改成功"});
    } else {
      await createProcessContent(form.value);
      ElMessage({type: "success", message: "新增成功"});
    }
    formDialogVisible.value = false;
    loadList();
  });
};

const handleDelete = function (row) {
  ElMessageBox.confirm("确定要删除第 " + row.orderNum + " 条内容吗？", '删除警告', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    await deleteProcessContent(row.contentId);
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
  ElMessageBox.confirm("确定要删除选中的 " + selectedRows.value.length + " 条内容吗？", '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    const ids = selectedRows.value.map(r => r.contentId);
    await deleteProcessContentBatch(ids);
    ElMessage({type: "info", message: "删除完成"});
    loadList();
  });
};

/** 监听父组件传进来的工序，工序一变就重新加载 */
watch(() => props.process, (newVal) => {
  if (newVal?.processId) {
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
