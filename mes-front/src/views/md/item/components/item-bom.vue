<template>
  <!-- ============ 批量操作工具栏（选中行时才显示） ============ -->
  <div class="batch-toolbar" >
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="small">新增</el-button>
    <el-button type="danger" :icon="Delete" size="small" @click="handleBatchDelete">批量删除</el-button>
  </div>

  <!-- ============ 数据表格 ============ -->
  <el-table
      ref="tableRef"
      :data="tableData"
      border
      stripe
      @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="50" />
    <el-table-column prop="bomId" label="流水号" width="80" />
    <el-table-column prop="bomItemId" label="BOM物料ID" min-width="140" show-overflow-tooltip/>
    <el-table-column prop="bomItemCode" label="BOM物料编码" min-width="140" show-overflow-tooltip />
    <el-table-column prop="bomItemName" label="BOM物料名称" min-width="140" show-overflow-tooltip />
    <el-table-column prop="bomItemSpec" label="BOM物料规格" min-width="140" show-overflow-tooltip />
    <el-table-column prop="unitOfMeasure" label="BOM物料单位" min-width="140" show-overflow-tooltip />
    <el-table-column prop="quantity" label="物料使用比例" min-width="140" show-overflow-tooltip />
    <el-table-column prop="enableFlag" label="是否启用" width="100" align="center">
      <template #default="{ row }">
        <!-- enableFlag 是 char(1)：'Y' 是，'N' 否，不能直接拿布尔判断 -->
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
  <!-- 选择物料的弹窗 -->
  <el-dialog v-model="dialogVisible" title="选择BOM物料" width="80%">
    <!-- ============ 搜索筛选表单 ============ -->
    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item label="产品物料编码">
        <el-input v-model="queryForm.itemCode" placeholder="请输入产品物料编码" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="产品物料名称">
        <el-input v-model="queryForm.itemName" placeholder="请输入产品物料名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="产品物料标识">
        <el-select v-model="queryForm.itemOrProduct" placeholder="请选择" clearable style="width: 130px">
          <el-option label="物料" value="ITEM" />
          <el-option label="产品" value="PRODUCT" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-select v-model="queryForm.enableFlag" placeholder="请选择" clearable style="width: 110px">
          <el-option label="是" value="Y" />
          <el-option label="否" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <!-- ============ 数据表格 ============ -->
    <el-table
        ref="tableRef"
        :data="itemList"
        border

        stripe
        @selection-change="handleSelectionChange"
    >
      <el-table-column label="ID" width="80">
        <template #default="scope">
          <el-radio
              v-model="selectedId"
              :label="scope.row.itemId"
              @change="handleRadioChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="itemCode" label="产品物料编码" min-width="140" show-overflow-tooltip />
      <el-table-column prop="itemName" label="产品物料名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="specification" label="规格型号" min-width="140" show-overflow-tooltip />
      <el-table-column prop="unitOfMeasure" label="单位编码" min-width="140" show-overflow-tooltip />
      <el-table-column prop="itemTypeName" label="物料类型名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="itemOrProduct" label="产品物料标识" width="110" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.itemOrProduct === 'ITEM'" type="primary">物料</el-tag>
          <el-tag v-if="row.itemOrProduct === 'PRODUCT'" type="primary">产品</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enableFlag" label="是否启用" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enableFlag === 'Y' ? 'success' : 'info'">
            {{ row.enableFlag === 'Y' ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- ============ 分页组件 ============ -->
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
      <el-button @click="handleSelectOk" type="primary">添加</el-button>
      <el-button @click="handleCancleSelect" type="info">取消</el-button>
    </template>
  </el-dialog>

  <!-- 设置比例的弹窗 -->
  <el-dialog v-model="bomQuantityDialogVisible" title="设置BOM比例" width="600">
    <el-descriptions
        class="margin-top"
        title="物料/产品详情"
        :column="2"
        border
    >
      <el-descriptions-item>
        <template #label>
          <div class="cell-item">
            物料编号
          </div>
        </template>
        {{selectedItem.itemCode}}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <div class="cell-item">
            物料名称
          </div>
        </template>
        {{selectedItem.itemName}}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <div class="cell-item">
            产品物料标识
          </div>
        </template>
        {{selectedItem.itemOrProduct}}
      </el-descriptions-item>
      <el-descriptions-item :span="2">
        <template #label>
          <div class="cell-item">
            物料规格
          </div>
        </template>
        {{selectedItem.specification}}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <div class="cell-item">
            物料单位
          </div>
        </template>
        {{selectedItem.unitName}}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <div class="cell-item">
            物料类型
          </div>
        </template>
        {{selectedItem.itemTypeName}}
      </el-descriptions-item>
      <el-descriptions-item :span="3" >
        <template #label>
          <div class="cell-item">
            备注
          </div>
        </template>
        {{selectedItem.remark}}
      </el-descriptions-item>
    </el-descriptions>
    <el-card>
      <el-form-item label="组成比例">
        <el-input-number v-model="bomForm.quantity" min="1"/>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="bomForm.enableFlag" active-value="Y" inactive-value="N"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input type="textarea" v-model="bomForm.remark"/>
      </el-form-item>
    </el-card>
    <template #footer>
      <el-button @click="handleAddBomOk" type="primary">确定</el-button>
      <el-button @click="handleAddBomCancle" type="info">取消</el-button>
    </template>
  </el-dialog>
</template>
<script setup>
import {ref, reactive, watch} from 'vue';
import {getBomByPage} from "@/api/md/bom.js";
import {Close, Delete, Plus, Edit, Search, Refresh} from "@element-plus/icons-vue";
import {
  getItemByPage,
    getItemPageForBom,
    getItemById
} from '@/api/md/item.js'
import {createBom,getBomById,updateBom,deleteBom} from "@/api/md/bom.js";
import {ElMessage, ElMessageBox} from "element-plus";


/** 弹窗中的搜索筛选条件 */
const queryForm = reactive({
  excludeItemId:null,
  itemCode: '',
  itemName: '',
  itemOrProduct: '',
  enableFlag: '',
})
// 申明属性
const props = defineProps(["item"]);
console.log(props)
// ====新增BOM组成弹窗=======
const dialogVisible = ref(false);
// 标记是否是编辑状态
const isEdit = ref(false)
// 设置BOM比例弹窗标记
const bomQuantityDialogVisible = ref(false);
// 新增或者编辑的BOM对象
const bomForm = ref({
  bomId:null,
  itemId:null,
  bomItemId:null,
  bomItemCode:'',
  bomItemName:'',
  bomItemSpec:'',
  unitOfMeasure:'',
  itemOrProduct:'',
  quantity:1,
  enableFlag:'Y',
  remark:''
});

// BOM组成行编辑按钮事件
const handleEdit = async function(row){
  let response  = await getBomById(row.bomId);
  if(response.data){
    bomForm.value = response.data;
    let res = await getItemById(row.bomItemId);
    selectedItem.value = res.data;
    bomQuantityDialogVisible.value = true;
    isEdit.value = true;
    return;
  }
  ElMessage({
    type:"warning",
    message:"数据有误，请刷新页面重试"
  });
}

// 行级别删除BOM按钮事件
const handleDelete = function(row){
  ElMessageBox.confirm("确定要删除[" + row.bomItemName + "]BOM组成",'删除警告',{
    confirmButtonText: '确定',
    cancelButtonText: '点错了',
    type: '删除警告',
  })
      .then(async ()=>{
        let response = await deleteBom(row.bomId);
        ElMessage({
          type:"info",
          message:"删除完成"
        });
        loadBomList();
      })
}

const handleAdd = function(){
  isEdit.value = false;
  dialogVisible.value = true;
  loadItem();
}
// 选择BOM组成选择的ID
const selectedId = ref()
// 选择BOM组成的物料对象
const selectedItem = ref({});
// 弹窗中的物料列表
const itemList = ref([]);
// 弹窗中的物料分页
const itemPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
// BOM物料选择弹窗，搜索按钮事件
const handleSearch = function (){
  pagination.page = 1;
  loadItem()
}
// 加载item
const loadItem = async function(){
  const params = {
    ...queryForm,
    pageNum: itemPagination.page,
    pageSize: itemPagination.size,
  }
  const result = await getItemPageForBom(params)
  itemList.value = result.data.list
  itemPagination.total = result.data.total
}
// 弹窗中的分页事件
const handleItemPageChange = function (newPage){
  itemPagination.page = newPage;
  loadItem();
}

// 设置BOM比例确定按钮事件
const handleAddBomOk = async function(){
  if(isEdit.value){
    let response = await updateBom(bomForm.value);
    ElMessage({
      type:"success",
      message:"设置BOM比例完成"
    });
  }else{
    bomForm.value.itemId = props.item.itemId;
    bomForm.value.bomItemId = selectedId.value;
    bomForm.value.bomItemCode = selectedItem.value.itemCode;
    bomForm.value.bomItemSpec = selectedItem.value.specification;
    bomForm.value.itemOrProduct = selectedItem.value.itemOrProduct;
    bomForm.value.bomItemName = selectedItem.value.itemName;
    bomForm.value.unitOfMeasure = selectedItem.value.unitOfMeasure
    let promise = await createBom(bomForm.value);
    ElMessage({
      type:"success",
      message:"添加BOM成功"
    });
  }
  handleAddBomCancle();
  loadBomList();
}
// 取消BOM比例按钮事件
const handleAddBomCancle = function(){
  selectedItem.value = {};
  selectedId.value = undefined;
  bomQuantityDialogVisible.value = false;
}

// 缓存BOM列表
const tableData = ref([]);
/** 分页参数：当前页码 / 每页条数 / 总条数 */
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
// ====加载BOM列表 ==
const loadBomList = async function(){
  console.log(props)
  console.log(props.item)
  let response = await getBomByPage({itemId:props.item.itemId,pageNum:pagination.page,pageSize:pagination.size});
  tableData.value = response.data.list;
  pagination.total = response.data.total;
}
// 分页事件
const handlePageChange = function(newPage){
  pagination.page = newPage;
  loadBomList();
}
// 监听props.item，item发生变化就重新加载列表
watch(() => props.item, (newVal) => {
  if(newVal?.itemId){
    queryForm.excludeItemId = newVal.itemId;
    loadBomList()
  }
}, { deep: true, immediate: true })
// 选择BOM物料单选按钮的选择事件
const handleRadioChange = function(row){
  selectedItem.value = row;
}
// 选择BOM物料的确认按钮处理函数
const handleSelectOk = function(){
  if(selectedId.value){
    bomQuantityDialogVisible.value = true;
  }else{
    ElMessage({
      type:"info",
      message:"为选择任何物料"
    });
  }
  handleCancleSelect();
}
// 选择BOM组成物料的取消按钮处理函数
const handleCancleSelect = function(){
  dialogVisible.value = false;
}
</script>
<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.search-form {
  margin-bottom: 16px;
}

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
  font-size: 14px;
}

.batch-tip b {
  color: #f56c6c;
  font-size: 16px;
  margin: 0 2px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
