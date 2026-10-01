<template>
<el-row>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.produceDateFlag" border label="生产日期" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.expireDateFlag" border label="有效期" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.recptDateFlag" border label="入库日期" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.vendorFlag" border label="供应商" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.clientFlag" border label="客户" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.coCodeFlag" border label="销售订单编号" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.poCodeFlag" border label="采购订单编号" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.workorderFlag" border label="生产工单" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.taskFlag" border label="生产任务" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.workstationFlag" border label="工作站" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.toolFlag" border label="工具" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.moldFlag" border label="模具" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.lotNumberFlag" border label="生产批号" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.qualityStatusFlag" border label="质量状态" false-value="N" true-value="Y"/>
  </el-col>
  <el-col class="col" :span="4">
    <el-checkbox v-model="batchConfig.enableFlag" border label="生效状态" false-value="N" true-value="Y"/>
  </el-col>
</el-row>
  <el-row>
    <el-col :span="4">
      <el-button @click="handleSubmitChange" :disabled="btnIsDisabled" type="primary">确定</el-button>
    </el-col>
    <el-col :span="4">
      <el-button @click="resetConfig" type="info">重置</el-button>
    </el-col>
  </el-row>
</template>
<script setup>
import {ref,watch} from 'vue';
import {getItemBatchConfigByItemId,updateItemBatchConfig,createItemBatchConfig} from "@/api/md/itemBatchConfig.js";
import {ElMessage} from "element-plus";

let props = defineProps(["item"]);

// 确定按钮是否可用标记
const btnIsDisabled = ref(true);
// 缓存批次配置数据，用于重置操作
const batchConfigCech = ref({});
// 批次配置对象
let batchConfig = ref({
  configId:null,
  produceDateFlag:'N',
  expireDateFlag:'N',
  recptDateFlag:'N',
  vendorFlag:'N',
  clientFlag:'N',
  coCodeFlag:'N',
  poCodeFlag:'N',
  workorderFlag:'N',
  taskFlag:'N',
  workstationFlag:'N',
  toolFlag:'N',
  moldFlag:'N',
  enableFlag:'N'
});
// 加载当前物料的批次配置
const loadBatchConfig = async function(){
  let response = await getItemBatchConfigByItemId(props.item.itemId);
  if(response.data){
    batchConfig.value = response.data;
    // 缓存原始数据
    batchConfigCech.value = {...response.data};
  }
}
// 提交按钮事件
const handleSubmitChange = async function(){
  // 设置产品物料的编号
  batchConfig.value.itemId = props.item.itemId;
  if(batchConfig.value.configId){
    let response = await updateItemBatchConfig(batchConfig.value);
  }else{
    let response = await createItemBatchConfig(batchConfig.value);
  }
  ElMessage({
    type:"success",
    message:"修改已经保存"
  });
  loadBatchConfig();
}
// 重置按钮事件
const resetConfig = function(){
  batchConfig.value = {...batchConfigCech.value}
}
// 监听props.item，item发生变化就重新加载列表
watch(() => props.item, (newVal) => {
  if(newVal?.itemId){
    loadBatchConfig()
  }
}, { deep: true, immediate: true })

// 监听props.item，item发生变化就重新加载列表
watch(() => batchConfig, (newVal) => {
  btnIsDisabled.value = false;
}, { deep: true, immediate: true })
</script>
<style>
 .col{
   margin: 10px;
 }
</style>