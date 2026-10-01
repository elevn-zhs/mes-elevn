<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="small">新增供应商</el-button>
    <el-button type="danger" :icon="Delete" size="small" @click="handleBatchDelete">批量删除</el-button>
    <span class="batch-tip">
      同一个物料可以挂多家供应商；主供应商只能有一个，设为「主供」时后端会把其它记录的主供标志清掉
    </span>
  </div>

  <!-- ============ 供货关系表格 ============ -->
  <el-table ref="tableRef" :data="tableData" border stripe @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50" />
    <el-table-column prop="vendorCode" label="供应商编码" min-width="120" show-overflow-tooltip />
    <el-table-column prop="vendorName" label="供应商名称" min-width="180" show-overflow-tooltip />
    <el-table-column prop="vendorItemCode" label="供应商料号" min-width="130" show-overflow-tooltip />
    <el-table-column prop="vendorItemName" label="供应商品名" min-width="140" show-overflow-tooltip />
    <el-table-column prop="purchasePrice" label="采购单价" width="110" align="right" />
    <el-table-column prop="minOrderQty" label="起订量" width="100" align="right" />
    <el-table-column prop="leadTime" label="交期(天)" width="100" align="center" />
    <el-table-column prop="primaryFlag" label="主供应商" width="100" align="center">
      <template #default="{ row }">
        <el-tag :type="row.primaryFlag === 'Y' ? 'danger' : 'info'">
          {{ row.primaryFlag === 'Y' ? '主供' : '备选' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="priority" label="优先级" width="80" align="center" />
    <el-table-column prop="enableFlag" label="是否启用" width="100" align="center">
      <template #default="{ row }">
        <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
          {{ row.enableFlag === 'Y' ? '是' : '否' }}
        </el-tag>
      </template>
    </el-table-column>
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

  <!-- ============ 选择供应商的弹窗 ============ -->
  <el-dialog v-model="selectDialogVisible" title="选择供应商" width="70%">
    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item label="供应商编码">
        <el-input v-model="queryForm.vendorCode" placeholder="请输入供应商编码" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item label="供应商名称">
        <el-input v-model="queryForm.vendorName" placeholder="请输入供应商名称" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="vendorList" border stripe>
      <el-table-column label="选择" width="70" align="center">
        <template #default="{ row }">
          <el-radio v-model="selectedId" :value="row.vendorId" @change="handleRadioChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="vendorCode" label="供应商编码" min-width="120" show-overflow-tooltip />
      <el-table-column prop="vendorName" label="供应商名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="vendorLevel" label="等级" width="80" align="center" />
      <el-table-column prop="vendorScore" label="评分" width="80" align="center" />
      <el-table-column prop="tel" label="联系电话" min-width="140" show-overflow-tooltip />
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="vendorPagination.page"
          v-model:page-size="vendorPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="vendorPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="handleVendorPageChange"
      />
    </div>

    <template #footer>
      <el-button @click="handleSelectOk" type="primary">确定</el-button>
      <el-button @click="selectDialogVisible = false" type="info">取消</el-button>
    </template>
  </el-dialog>

  <!-- ============ 新增 / 编辑供货关系的表单弹窗 ============ -->
  <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑供应商' : '新增供应商'" width="600">
    <el-form :model="form" label-width="110px">
      <el-form-item label="供应商">
        <el-input :value="form.vendorName" disabled placeholder="请先在弹窗中选择供应商" />
      </el-form-item>
      <el-form-item label="供应商料号">
        <el-input v-model="form.vendorItemCode" placeholder="对方系统里的物料编码" />
      </el-form-item>
      <el-form-item label="供应商品名">
        <el-input v-model="form.vendorItemName" placeholder="对方系统里的物料名称" />
      </el-form-item>
      <el-form-item label="采购单价">
        <el-input-number v-model="form.purchasePrice" :min="0" :step="0.5" :precision="2" />
      </el-form-item>
      <el-form-item label="币种">
        <el-select v-model="form.currency" style="width: 140px">
          <el-option label="人民币 CNY" value="CNY" />
          <el-option label="美元 USD" value="USD" />
          <el-option label="欧元 EUR" value="EUR" />
        </el-select>
      </el-form-item>
      <el-form-item label="最小起订量">
        <el-input-number v-model="form.minOrderQty" :min="0" :step="10" />
      </el-form-item>
      <el-form-item label="交货周期">
        <el-input-number v-model="form.leadTime" :min="0" :step="1" />
        <span class="form-tip">天</span>
      </el-form-item>
      <el-form-item label="主供应商">
        <el-switch v-model="form.primaryFlag" active-value="Y" inactive-value="N" />
        <span class="form-tip">设为「是」时，该物料其它记录会自动变成备选</span>
      </el-form-item>
      <el-form-item label="优先级">
        <el-input-number v-model="form.priority" :min="1" :step="1" />
        <span class="form-tip">数字越小越优先</span>
      </el-form-item>
      <el-form-item label="生效日期">
        <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="留空表示长期有效" />
      </el-form-item>
      <el-form-item label="失效日期">
        <el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="留空表示长期有效" />
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
import {Plus, Delete, Edit, Search, Refresh} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {
  getItemVendorByPage,
  getItemVendorById,
  createItemVendor,
  updateItemVendor,
  deleteItemVendor,
  deleteItemVendorBatch
} from "@/api/md/itemVendor.js";
import {getVendorByPage} from "@/api/md/vendor.js";

const props = defineProps(["item"]);
const tableRef = ref();

/** 列表数据 */
const tableData = ref([]);
/** 分页参数 */
const pagination = reactive({page: 1, size: 10, total: 0});
/** 多选选中的行 */
const selectedRows = ref([]);

/** 选供应商弹窗相关 */
const selectDialogVisible = ref(false);
const vendorList = ref([]);
const vendorPagination = reactive({page: 1, size: 10, total: 0});
const selectedId = ref();
const selectedVendor = ref({});
const queryForm = reactive({
  vendorCode: '',
  vendorName: '',
});

/** 表单弹窗相关 */
const formDialogVisible = ref(false);
const isEdit = ref(false);
const defaultForm = () => ({
  itemVendorId: null,
  itemId: null,
  itemCode:'',
  itemName:'',
  vendorId: null,
  vendorCode: '',
  vendorName: '',
  vendorItemCode: '',
  vendorItemName: '',
  purchasePrice: 0,
  currency: 'CNY',
  minOrderQty: 0,
  leadTime: 0,
  primaryFlag: 'N',
  priority: 1,
  effectiveDate: '',
  expireDate: '',
  enableFlag: 'Y',
  remark: ''
});
const form = ref(defaultForm());

/** 重置表单：先删掉所有键再灌默认值，避免残留上一次编辑的字段 */
const resetForm = function () {
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, defaultForm());
};

/** 加载供货关系列表 */
const loadList = async function () {
  const response = await getItemVendorByPage({
    itemId: props.item.itemId,
    pageNum: pagination.page,
    pageSize: pagination.size
  });
  tableData.value = response.data.list;
  pagination.total = response.data.total;
};

/** 加载供应商档案列表 */
const loadVendorList = async function () {
  const params = {
    ...queryForm,
    pageNum: vendorPagination.page,
    pageSize: vendorPagination.size
  };
  const result = await getVendorByPage(params);
  vendorList.value = result.data.list;
  vendorPagination.total = result.data.total;
};

const handlePageChange = function (newPage) {
  pagination.page = newPage;
  loadList();
};

const handleVendorPageChange = function (newPage) {
  vendorPagination.page = newPage;
  loadVendorList();
};

const handleSearch = function () {
  vendorPagination.page = 1;
  loadVendorList();
};

const handleReset = function () {
  queryForm.vendorCode = '';
  queryForm.vendorName = '';
  vendorPagination.page = 1;
  loadVendorList();
};

const handleRadioChange = function (row) {
  selectedVendor.value = row;
};

const handleAdd = function () {
  isEdit.value = false;
  resetForm();
  selectedId.value = undefined;
  selectedVendor.value = {};
  selectDialogVisible.value = true;
  loadVendorList();
};

const handleSelectOk = function () {
  if (!selectedId.value) {
    ElMessage({type: "warning", message: "请先选择一个供应商"});
    return;
  }
  // 冗余字段前端先带上，后端保存时还会从 md_vendor 重新取一遍
  form.value.vendorItemCode = props.item.itemCode;
  form.value.vendorItemName = props.item.itemName;
  form.value.vendorId = selectedVendor.value.vendorId;
  form.value.vendorCode = selectedVendor.value.vendorCode;
  form.value.vendorName = selectedVendor.value.vendorName;
  form.value.itemName = props.item.itemName;
  form.value.itemCode = props.item.itemCode;
  console.log(form.value)
  selectDialogVisible.value = false;
  formDialogVisible.value = true;
};

const handleEdit = async function (row) {
  const response = await getItemVendorById(row.itemVendorId);
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
  if (isEdit.value) {
    await updateItemVendor(form.value);
    ElMessage({type: "success", message: "修改成功"});
  } else {
    await createItemVendor(form.value);
    ElMessage({type: "success", message: "添加成功"});
  }
  formDialogVisible.value = false;
  loadList();
};

const handleDelete = function (row) {
  ElMessageBox.confirm("确定要删除供应商[" + row.vendorName + "]的供货关系吗？", '删除警告', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    await deleteItemVendor(row.itemVendorId);
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
  ElMessageBox.confirm("确定要删除选中的 " + selectedRows.value.length + " 条供货关系吗？", '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    const ids = selectedRows.value.map(r => r.itemVendorId);
    await deleteItemVendorBatch(ids);
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
