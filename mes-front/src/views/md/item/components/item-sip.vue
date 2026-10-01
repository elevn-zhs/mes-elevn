<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="small">新增SIP</el-button>
    <el-button type="danger" :icon="Delete" size="small" @click="handleBatchDelete">批量删除</el-button>
    <span class="batch-tip">
      SIP 是按工序编写的检验标准（来料 / 过程 / 出厂）；换版本时不要删旧的，直接停用留着追溯
    </span>
  </div>

  <!-- ============ SIP 数据表格 ============ -->
  <el-table ref="tableRef" :data="tableData" border stripe @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50" />
    <el-table-column prop="orderNum" label="顺序" width="70" align="center" />
    <el-table-column prop="processName" label="工序名称" min-width="130" show-overflow-tooltip />
    <el-table-column prop="sipTitle" label="标题" min-width="180" show-overflow-tooltip />
    <el-table-column prop="sipDescription" label="详细描述" min-width="220" show-overflow-tooltip />
    <el-table-column label="附件" width="110" align="center">
      <template #default="{ row }">
        <el-link v-if="row.sipUrl" :href="row.sipUrl" target="_blank" type="primary">查看</el-link>
        <span v-else>-</span>
      </template>
    </el-table-column>
    <el-table-column prop="enableFlag" label="是否启用" width="100" align="center">
      <template #default="{ row }">
        <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
          {{ row.enableFlag === 'Y' ? '是' : '否' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="createTime" label="创建时间" width="180" />
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
        <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

  <!-- ============ 新增 / 编辑 SIP 的表单弹窗 ============ -->
  <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑SIP' : '新增SIP'" width="640">
    <el-form :model="form" label-width="100px">
      <el-form-item label="排列顺序">
        <el-input-number v-model="form.orderNum" :min="1" :step="1" />
        <span class="form-tip">同一工序内按此顺序展示</span>
      </el-form-item>
      <el-form-item label="工序名称">
        <!-- pro 模块的工序接口还没提供，这里先手填；后续接上工序字典后改成下拉选择 -->
        <el-input v-model="form.processName" placeholder="如：来料检验、过程检验、出厂检验" style="width: 240px" />
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.sipTitle" placeholder="如：控制柜出厂检验标准" />
      </el-form-item>
      <el-form-item label="详细描述">
        <el-input v-model="form.sipDescription" type="textarea" :rows="3" placeholder="检验项目、判定标准、AQL 值" />
      </el-form-item>
      <el-form-item label="附件">
        <el-upload
            v-model:file-list="fileList"
            class="upload-demo"
            name="imageFile"
            :action="uploadPath"
            :on-success="handleUploadSuccess"
        >
          <el-button type="primary">上传附件</el-button>
          <template #tip>
            <div class="el-upload__tip">
              PDF文件或者图片
            </div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.enableFlag" active-value="Y" inactive-value="N" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" />
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
import {Plus, Delete, Edit} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {
  getProductSipByPage,
  getProductSipById,
  createProductSip,
  updateProductSip,
  deleteProductSip,
  deleteProductSipBatch
} from "@/api/md/productSip.js";

const props = defineProps(["item"]);
const tableRef = ref();

/** 列表数据 */
const tableData = ref([]);
/** 分页参数 */
const pagination = reactive({page: 1, size: 10, total: 0});
/** 多选选中的行 */
const selectedRows = ref([]);

// 文件上传地址
const uploadPath = ref("http://localhost:8081/upload/image");
// 上传成功后的文件地址
const fileList = ref([])
// 上传完成后的处理事件
const handleUploadSuccess = function(response){
  fileList.value = [];
  fileList.value.push({
    name:response.msg,
    url:response.data
  });
}

/** 表单弹窗相关 */
const formDialogVisible = ref(false);
const isEdit = ref(false);
const defaultForm = () => ({
  sipId: null,
  itemId: null,
  orderNum: 1,
  processId: null,
  processCode: '',
  processName: '',
  sipTitle: '',
  sipDescription: '',
  sipUrl: '',
  enableFlag: 'Y',
  remark: ''
});
const form = ref(defaultForm());

/** 重置表单：先删掉所有键再灌默认值，避免残留上一次编辑的内容 */
const resetForm = function () {
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, defaultForm());
};

/** 加载 SIP 列表 */
const loadList = async function () {
  const response = await getProductSipByPage({
    itemId: props.item.itemId,
    pageNum: pagination.page,
    pageSize: pagination.size
  });
  tableData.value = response.data.list;
  pagination.total = response.data.total;
};

const handlePageChange = function (newPage) {
  pagination.page = newPage;
  loadList();
};

const handleAdd = function () {
  isEdit.value = false;
  resetForm();
  formDialogVisible.value = true;
};

const handleEdit = async function (row) {
  const response = await getProductSipById(row.sipId);
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
  form.value.itemId = props.item.itemId;
  if(fileList.value.length > 0){
    if (fileList.value[0].url){
      form.value.sipUrl = fileList.value[0].url;
    }
  }
  if (isEdit.value) {
    await updateProductSip(form.value);
    ElMessage({type: "success", message: "修改成功"});
  } else {
    await createProductSip(form.value);
    ElMessage({type: "success", message: "添加成功"});
  }
  formDialogVisible.value = false;
  loadList();
};

const handleDelete = function (row) {
  ElMessageBox.confirm("确定要删除SIP[" + row.sipTitle + "]吗？", '删除警告', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    await deleteProductSip(row.sipId);
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
  ElMessageBox.confirm("确定要删除选中的 " + selectedRows.value.length + " 条SIP吗？", '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    const ids = selectedRows.value.map(r => r.sipId);
    await deleteProductSipBatch(ids);
    ElMessage({type: "info", message: "删除完成"});
    loadList();
  });
};

/** 监听父组件传进来的物料，物料一变就重新加载 */
watch(() => props.item, (newVal) => {
  if (newVal?.itemId) {
    pagination.page = 1;
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
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
