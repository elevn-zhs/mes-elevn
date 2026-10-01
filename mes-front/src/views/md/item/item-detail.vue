<template>
  <div class="md-item">
    <el-card shadow="never">
          <span>
            <el-button @click="handleBack"><el-icon><ArrowLeftBold /></el-icon></el-button>
            物料产品详情
          </span>
    </el-card>
    <!--=====物料/产品详情 ========-->
    <el-card shadow="never">
      <el-descriptions
          class="margin-top"
          title="物料/产品详情"
          :column="3"
          border
      >
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              物料编号
            </div>
          </template>
          {{itemDetail.itemCode}}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              物料名称
            </div>
          </template>
          {{itemDetail.itemName}}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              产品物料标识
            </div>
          </template>
          {{itemDetail.itemOrProduct}}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template #label>
            <div class="cell-item">
              物料规格
            </div>
          </template>
          {{itemDetail.specification}}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              物料单位
            </div>
          </template>
          {{itemDetail.unitName}}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              物料类型
            </div>
          </template>
          {{itemDetail.itemTypeName}}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              是否需要安全库存
            </div>
          </template>
          {{itemDetail.safeStockFlag=='Y'?'是':'否'}}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              高价值物资
            </div>
          </template>
          {{itemDetail.highValue=='Y'?'是':'否'}}
        </el-descriptions-item>
        <el-descriptions-item v-if="itemDetail.safeStockFlag=='Y'">
          <template #label>
            <div class="cell-item">
              最少库存
            </div>
          </template>
          {{itemDetail.minStock}}
        </el-descriptions-item>
        <el-descriptions-item :span="2" v-if="itemDetail.safeStockFlag=='Y'">
          <template #label>
            <div class="cell-item">
              最大库存
            </div>
          </template>
          {{itemDetail.maxStock}}
        </el-descriptions-item>
        <el-descriptions-item :span="3" >
          <template #label>
            <div class="cell-item">
              备注
            </div>
          </template>
          {{itemDetail.remark}}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    <!-- 物料的其他相关配置和操作 -->
    <el-card shadow="naver">
      <el-tabs type="border-card">
        <el-tab-pane label="BOM组成">
          <!-- 使用子组件开发内容，防止当前页面代码过多 -->
          <ItemBom :item="itemDetail"/>
        </el-tab-pane>
        <el-tab-pane v-if="itemDetail.batchFlag == 'Y'" label="批次属性">
          <item-batch-config :item="itemDetail"/>
        </el-tab-pane>
        <el-tab-pane label="替代品">
          <item-substitute :item="itemDetail"/>
        </el-tab-pane>
        <el-tab-pane label="供应商">
          <item-vendor :item="itemDetail"/>
        </el-tab-pane>
        <el-tab-pane label="SIP">
          <item-sip :item="itemDetail"/>
        </el-tab-pane>
        <el-tab-pane label="SOP">
          <item-sop :item="itemDetail"/>
        </el-tab-pane>
        <!-- 产品制程只有产品才有意义，物料不显示这个 Tab -->
        <el-tab-pane v-if="itemDetail.itemOrProduct == 'PRODUCT'" label="工艺路线">
          <item-route :item="itemDetail"/>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
<script setup>
import {ref} from 'vue'
import {Download, Plus} from "@element-plus/icons-vue";
import {useRoute,useRouter} from 'vue-router';
import {getItemById} from "@/api/md/item.js";
import {ElMessage} from "element-plus";
// 导入组件
import ItemBom from '@/views/md/item/components/item-bom.vue'
import ItemBatchConfig from "@/views/md/item/components/item-batch-config.vue";
import ItemSubstitute from "@/views/md/item/components/item-substitute.vue";
import ItemVendor from "@/views/md/item/components/item-vendor.vue";
import ItemSip from "@/views/md/item/components/item-sip.vue";
import ItemSop from "@/views/md/item/components/item-sop.vue";
import ItemRoute from "@/views/md/item/components/item-route.vue";
const route = useRoute();
const router = useRouter();
// 从请求参数中获取itemId
let itemId = route.params.itemId;
// 缓存物料详情
const itemDetail = ref({});
//=====加载物料/产品详情 ===
const loadItemDetail = async function(){
  let response = await getItemById(itemId);
  if(response.data){
    itemDetail.value = response.data;
  }else{
    ElMessage({
      message:"数据有误，请刷新页面重试",
      type:"warning"
    });
  }
}
loadItemDetail();

// ====回退按钮事件====
const handleBack = function(){
  router.back();
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

.el-descriptions {
  margin-top: 20px;
}
.cell-item {
  display: flex;
  align-items: center;
}
.margin-top {
  margin-top: 20px;
}
</style>
