<template>
  <!-- ============ 操作工具栏 ============ -->
  <div class="batch-toolbar">
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="small">新增替代料</el-button>
    <el-button type="danger" :icon="Delete" size="small" @click="handleBatchDelete">批量删除</el-button>
    <span class="batch-tip">
      这里配置的是「当前物料可以被哪些物料顶替」；选料弹窗已经把当前物料自身排除了，后端还会再挡一次自引用
    </span>
  </div>

  <!-- ============ 替代品数据表格 ============ -->
  <el-table ref="tableRef" :data="tableData" border stripe @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50" />
    <el-table-column prop="subItemCode" label="替代物料编码" min-width="140" show-overflow-tooltip />
    <el-table-column prop="subItemName" label="替代物料名称" min-width="140" show-overflow-tooltip />
    <el-table-column prop="subItemSpec" label="规格" min-width="140" show-overflow-tooltip />
    <el-table-column prop="unitOfMeasure" label="单位" width="90" />
    <el-table-column prop="substituteType" label="替代类型" width="110" align="center">
      <template #default="{ row }">
        <el-tag :type="row.substituteType === 'TWO_WAY' ? 'warning' : 'primary'">
          {{ row.substituteType === 'TWO_WAY' ? '双向' : '单向' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="substituteRatio" label="用量比" width="90" align="center" />
    <el-table-column prop="priority" label="优先级" width="80" align="center" />
    <el-table-column prop="effectiveDate" label="生效日期" width="120" />
    <el-table-column prop="expireDate" label="失效日期" width="120" />
    <el-table-column prop="enableFlag" label="是否启用" width="100" align="center">
      <template #default="{ row }">
        <!-- enableFlag 是 char(1)：'Y' 启用，'N' 停用，不能拿布尔直接判断 -->
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

  <!-- ============ 选择替代物料的弹窗 ============ -->
  <el-dialog v-model="selectDialogVisible" title="选择替代物料" width="70%">
    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item label="物料编码">
        <el-input v-model="queryForm.itemCode" placeholder="请输入物料编码" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item label="物料名称">
        <el-input v-model="queryForm.itemName" placeholder="请输入物料名称" clearable style="width: 170px" />
      </el-form-item>
      <el-form-item label="产品物料标识">
        <el-select v-model="queryForm.itemOrProduct" placeholder="请选择" clearable style="width: 130px">
          <el-option label="物料" value="ITEM" />
          <el-option label="产品" value="PRODUCT" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="itemList" border stripe>
      <el-table-column label="选择" width="70" align="center">
        <template #default="{ row }">
          <!-- EP 2.14 里 el-radio 用 :value 绑定选中值，:label 已是废弃写法 -->
          <el-radio v-model="selectedId" :value="row.itemId" @change="handleRadioChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="itemCode" label="物料编码" min-width="140" show-overflow-tooltip />
      <el-table-column prop="itemName" label="物料名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="specification" label="规格型号" min-width="140" show-overflow-tooltip />
      <el-table-column prop="unitName" label="单位" width="90" />
      <el-table-column prop="itemTypeName" label="物料类型" min-width="120" show-overflow-tooltip />
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="itemPagination.page"
          v-model:page-size="itemPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="itemPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="handleItemPageChange"
      />
    </div>

    <template #footer>
      <el-button @click="handleSelectOk" type="primary">确定</el-button>
      <el-button @click="selectDialogVisible = false" type="info">取消</el-button>
    </template>
  </el-dialog>

  <!-- ============ 新增 / 编辑替代品的表单弹窗 ============ -->
  <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑替代料' : '新增替代料'" width="600">
    <el-form :model="form" label-width="110px">
      <el-form-item label="替代物料">
        <el-input :value="form.subItemName" disabled placeholder="请先在弹窗中选择替代物料" />
      </el-form-item>
      <el-form-item label="替代类型">
        <el-select v-model="form.substituteType" style="width: 100%">
          <el-option label="单向（只有当前物料能被它顶替）" value="ONE_WAY" />
          <el-option label="双向（两个物料互为替代）" value="TWO_WAY" />
        </el-select>
      </el-form-item>
      <el-form-item label="用量比">
        <el-input-number v-model="form.substituteRatio" :min="0.01" :step="0.05" :precision="2" />
        <span class="form-tip">消耗 1 个当前物料，需要用掉多少个替代物料</span>
      </el-form-item>
      <el-form-item label="优先级">
        <el-input-number v-model="form.priority" :min="1" :step="1" />
        <span class="form-tip">数字越小越优先</span>
      </el-form-item>
      <el-form-item label="生效日期">
        <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="留空表示立即生效" />
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
  getItemSubstituteByPage,
  getItemSubstituteById,
  createItemSubstitute,
  updateItemSubstitute,
  deleteItemSubstitute,
  deleteItemSubstituteBatch
} from "@/api/md/itemSubstitute.js";
import {getItemPageForBom} from "@/api/md/item.js";

const props = defineProps(["item"]);
const tableRef = ref();

/** 列表数据 */
const tableData = ref([]);
/** 分页参数 */
const pagination = reactive({page: 1, size: 10, total: 0});
/** 多选选中的行 */
const selectedRows = ref([]);

/** 选料弹窗相关 */
const selectDialogVisible = ref(false);
const itemList = ref([]);
const itemPagination = reactive({page: 1, size: 10, total: 0});
const selectedId = ref();
const selectedItem = ref({});
const queryForm = reactive({
  excludeItemId: null,
  itemCode: '',
  itemName: '',
  itemOrProduct: '',
});

/** 表单弹窗相关 */
const formDialogVisible = ref(false);
const isEdit = ref(false);
const defaultForm = () => ({
  substituteId: null,
  itemId: null,
  itemCode:'',
  itemName:'',
  subItemId: null,
  subItemCode: '',
  subItemName: '',
  substituteType: 'ONE_WAY',
  substituteRatio: 1,
  unitOfMeasure:'',
  priority: 1,
  effectiveDate: '',
  expireDate: '',
  enableFlag: 'Y',
  remark: ''
});
const form = ref(defaultForm());

/**
 * 重置表单：先把所有键删掉再灌默认值。
 * 直接 Object.assign 的话，上一次编辑留下的键（比如 remark）会残留到下一次新增里。
 */
const resetForm = function () {
  Object.keys(form.value).forEach(k => delete form.value[k]);
  Object.assign(form.value, defaultForm());
};

/** 加载替代品列表 */
const loadList = async function () {
  const response = await getItemSubstituteByPage({
    itemId: props.item.itemId,
    pageNum: pagination.page,
    pageSize: pagination.size
  });
  tableData.value = response.data.list;
  pagination.total = response.data.total;
};

/** 加载选料弹窗里的物料：excludeItemId 会把当前物料自身挡掉 */
const loadItemList = async function () {
  const params = {
    ...queryForm,
    excludeItemId: props.item.itemId,
    pageNum: itemPagination.page,
    pageSize: itemPagination.size
  };
  const result = await getItemPageForBom(params);
  itemList.value = result.data.list;
  itemPagination.total = result.data.total;
};

const handlePageChange = function (newPage) {
  pagination.page = newPage;
  loadList();
};

const handleItemPageChange = function (newPage) {
  itemPagination.page = newPage;
  loadItemList();
};

const handleSearch = function () {
  itemPagination.page = 1;
  loadItemList();
};

const handleReset = function () {
  queryForm.itemCode = '';
  queryForm.itemName = '';
  queryForm.itemOrProduct = '';
  itemPagination.page = 1;
  loadItemList();
};

const handleRadioChange = function (row) {
  selectedItem.value = row;
};

const handleAdd = function () {
  isEdit.value = false;
  resetForm();
  selectedId.value = undefined;
  selectedItem.value = {};
  selectDialogVisible.value = true;
  loadItemList();
};

const handleSelectOk = function () {
  if (!selectedId.value) {
    ElMessage({type: "warning", message: "请先选择一个替代物料"});
    return;
  }
  // 冗余字段在前端先带上，后端保存时还会从 md_item 重新取一遍，这里带错也没关系
  form.value.subItemId = selectedItem.value.itemId;
  form.value.subItemCode = selectedItem.value.itemCode;
  form.value.subItemName = selectedItem.value.itemName;
  selectDialogVisible.value = false;
  formDialogVisible.value = true;
};

const handleEdit = async function (row) {
  const response = await getItemSubstituteById(row.substituteId);
  if (!response.data) {
    ElMessage({type: "warning", message: "数据有误，请刷新页面重试"});
    return;
  }
  // 先清干净再灌值：避免上一次编辑残留的字段跟进来
  resetForm();
  Object.assign(form.value, response.data);
  isEdit.value = true;
  formDialogVisible.value = true;
};

const handleSubmit = async function () {
  // 给itemName设置值
  form.value.itemName = props.item.itemName;
  form.value.itemCode = props.item.itemCode;
  // 设置替代物料的单位
  form.value.unitOfMeasure = selectedItem.value.unitOfMeasure;
  if (isEdit.value) {
    // 编辑时不覆盖 itemId / subItemId：
    // 双向替代的记录可能站在反向视角（自己其实是库里的 sub_item_id），
    // 用当前物料的 ID 覆盖一下会把这条记录的方向翻过去。
    await updateItemSubstitute(form.value);
    ElMessage({type: "success", message: "修改成功"});
  } else {
    form.value.itemId = props.item.itemId;
    await createItemSubstitute(form.value);
    ElMessage({type: "success", message: "添加成功"});
  }
  formDialogVisible.value = false;
  loadList();
};

const handleDelete = function (row) {
  ElMessageBox.confirm("确定要删除替代料[" + row.subItemName + "]吗？", '删除警告', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    await deleteItemSubstitute(row.substituteId);
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
  ElMessageBox.confirm("确定要删除选中的 " + selectedRows.value.length + " 条替代关系吗？", '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: 'warning',
  }).then(async () => {
    const ids = selectedRows.value.map(r => r.substituteId);
    await deleteItemSubstituteBatch(ids);
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
