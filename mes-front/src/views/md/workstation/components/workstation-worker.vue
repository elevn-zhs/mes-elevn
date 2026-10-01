<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="small">配置岗位</el-button>
    <el-button type="danger" :icon="Delete" size="small" @click="handleBatchDelete">批量删除</el-button>
    <span class="batch-tip">
      这里配的是"这个工位需要几个什么岗位的人"，数量填人数；同一岗位重复配置会被后端拒绝
    </span>
  </div>

  <!-- ============ 人力配置表格 ============ -->
  <el-table ref="tableRef" :data="tableData" border stripe @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50" />
    <el-table-column prop="postCode" label="岗位编码" min-width="130" show-overflow-tooltip />
    <el-table-column prop="postName" label="岗位名称" min-width="180" show-overflow-tooltip />
    <el-table-column prop="quantity" label="需求人数" width="100" align="right" />
    <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
    <el-table-column prop="createTime" label="创建时间" width="180" />
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
        <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- ============ 选择岗位的弹窗 ============ -->
  <el-dialog v-model="selectDialogVisible" title="选择岗位" width="70%">
    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item label="岗位编码">
        <el-input v-model="queryForm.postCode" placeholder="请输入岗位编码" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item label="岗位名称">
        <el-input v-model="queryForm.postName" placeholder="请输入岗位名称" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="postList" border stripe>
      <el-table-column label="选择" width="70" align="center">
        <template #default="{ row }">
          <el-radio v-model="selectedId" :value="row.postId" @change="handleRadioChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="postCode" label="岗位编码" min-width="130" show-overflow-tooltip />
      <el-table-column prop="postName" label="岗位名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="postPagination.page"
          v-model:page-size="postPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="postPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="handlePostPageChange"
      />
    </div>

    <template #footer>
      <el-button @click="handleSelectOk" type="primary">确定</el-button>
      <el-button @click="selectDialogVisible = false" type="info">取消</el-button>
    </template>
  </el-dialog>

  <!-- ============ 填写人数的表单弹窗 ============ -->
  <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑人力配置' : '配置岗位'" width="520px">
    <el-form :model="form" label-width="110px">
      <el-form-item label="岗位">
        <el-input :value="form.postCode + ' / ' + form.postName" disabled
                  :placeholder="isEdit ? '' : '请先在弹窗中选择岗位'" />
      </el-form-item>
      <el-form-item label="需求人数">
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
  getWorkstationWorkerById,
  getWorkstationWorkerListByWorkstationId,
  createWorkstationWorker,
  updateWorkstationWorker,
  deleteWorkstationWorker,
  deleteWorkstationWorkerBatch
} from "@/api/md/workstationWorker.js";
import {getPostByPage} from "@/api/sys/post.js";

const props = defineProps(["workstation"]);
const tableRef = ref();

/** 列表数据（按工作站一次性查全量） */
const tableData = ref([]);
const selectedRows = ref([]);

/** 选岗位弹窗相关 */
const selectDialogVisible = ref(false);
const postList = ref([]);
const postPagination = reactive({page: 1, size: 10, total: 0});
const selectedId = ref();
const selectedPost = ref({});
const queryForm = reactive({
  postCode: '',
  postName: '',
});

/** 表单弹窗相关 */
const formDialogVisible = ref(false);
const isEdit = ref(false);
const defaultForm = () => ({
  recordId: null,
  workstationId: null,
  postId: null,
  postCode: '',
  postName: '',
  quantity: 1,
  remark: ''
});
const form = ref(defaultForm());

/** 重置表单：先删掉所有键再灌默认值，避免残留上一次编辑的字段 */
const resetForm = function () {
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, defaultForm());
};

/** 加载当前工作站的人力配置列表 */
const loadList = async function () {
  if (!props.workstation?.workstationId) return;
  const response = await getWorkstationWorkerListByWorkstationId(props.workstation.workstationId);
  tableData.value = response.data || [];
};

/** 加载岗位候选列表（走系统管理已有的 /api/post/page） */
const loadPostList = async function () {
  const params = {
    ...queryForm,
    pageNum: postPagination.page,
    pageSize: postPagination.size
  };
  const result = await getPostByPage(params);
  postList.value = result.data.list;
  postPagination.total = result.data.total;
};

const handlePostPageChange = function (newPage) {
  postPagination.page = newPage;
  loadPostList();
};

const handleSearch = function () {
  postPagination.page = 1;
  loadPostList();
};

const handleReset = function () {
  queryForm.postCode = '';
  queryForm.postName = '';
  handleSearch();
};

const handleRadioChange = function (row) {
  selectedPost.value = row;
};

const handleAdd = function () {
  isEdit.value = false;
  resetForm();
  selectedId.value = undefined;
  selectedPost.value = {};
  selectDialogVisible.value = true;
  loadPostList();
};

const handleSelectOk = function () {
  if (!selectedId.value) {
    ElMessage({type: "warning", message: "请先选择一个岗位"});
    return;
  }
  form.value.postId = selectedPost.value.postId;
  form.value.postCode = selectedPost.value.postCode;
  form.value.postName = selectedPost.value.postName;
  selectDialogVisible.value = false;
  formDialogVisible.value = true;
};

const handleEdit = async function (row) {
  const response = await getWorkstationWorkerById(row.recordId);
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
    await updateWorkstationWorker(form.value);
    ElMessage({type: "success", message: "修改成功"});
  } else {
    await createWorkstationWorker(form.value);
    ElMessage({type: "success", message: "配置成功"});
  }
  formDialogVisible.value = false;
  loadList();
};

const handleDelete = function (row) {
  ElMessageBox.confirm("确定要删除岗位[" + row.postName + "]的配置吗？", '删除警告', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    await deleteWorkstationWorker(row.recordId);
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
  ElMessageBox.confirm("确定要删除选中的 " + selectedRows.value.length + " 条人力配置吗？", '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    const ids = selectedRows.value.map(r => r.recordId);
    await deleteWorkstationWorkerBatch(ids);
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
