<template>
  <div class="pro-process-detail">
    <el-card shadow="never">
      <span>
        <el-button @click="handleBack"><el-icon><ArrowLeftBold /></el-icon></el-button>
        工序详情
      </span>
    </el-card>

    <!-- =====工序基本信息（只读，改基本信息回列表页点编辑）===== -->
    <el-card shadow="never">
      <el-descriptions
          class="margin-top"
          title="工序基本信息"
          :column="3"
          border
      >
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工序编码</div>
          </template>
          {{ processDetail.processCode }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工序名称</div>
          </template>
          {{ processDetail.processName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">状态</div>
          </template>
          <el-tag :type="processDetail.enableFlag === 'Y' ? 'success' : 'info'">
            {{ processDetail.enableFlag === 'Y' ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">工艺要求</div>
          </template>
          {{ processDetail.attention || '—' }}
        </el-descriptions-item>
        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">备注</div>
          </template>
          {{ processDetail.remark || '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- =====工序下面的子表数据===== -->
    <el-card shadow="never">
      <el-tabs type="border-card">
        <el-tab-pane label="工序步骤">
          <!-- 使用子组件开发内容，防止当前页面代码过多 -->
          <ProcessContent :process="processDetail"/>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {ArrowLeftBold} from "@element-plus/icons-vue";
import {useRoute, useRouter} from 'vue-router';
import {getProcessById} from "@/api/pro/process.js";
import {ElMessage} from "element-plus";
// 导入组件
import ProcessContent from "@/views/pro/process/components/process-content.vue";

const route = useRoute();
const router = useRouter();
// 从请求参数中获取processId
let processId = route.params.processId;
// 缓存工序详情
const processDetail = ref({});

//=====加载工序详情（后端已把工序内容列表带回来）===
const loadProcessDetail = async function () {
  let response = await getProcessById(processId);
  if (response.data) {
    processDetail.value = response.data;
  } else {
    ElMessage({
      message: "数据有误，请刷新页面重试",
      type: "warning"
    });
  }
}
loadProcessDetail();

// ====回退按钮事件====
const handleBack = function () {
  router.back();
}
</script>

<style scoped>
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
