<template>
  <div class="md-workstation-detail">
    <el-card shadow="never">
      <span>
        <el-button @click="handleBack"><el-icon><ArrowLeftBold /></el-icon></el-button>
        工作站详情
      </span>
    </el-card>

    <!-- =====工作站基本信息（只读，改基本信息回列表页点编辑）===== -->
    <el-card shadow="never">
      <el-descriptions
          class="margin-top"
          title="工作站基本信息"
          :column="3"
          border
      >
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工作站编码</div>
          </template>
          {{ workstationDetail.workstationCode }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工作站名称</div>
          </template>
          {{ workstationDetail.workstationName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工作站地点</div>
          </template>
          {{ workstationDetail.workstationAddress }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">所在车间</div>
          </template>
          {{ workstationDetail.workshopName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工序</div>
          </template>
          {{ workstationDetail.processName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">是否启用</div>
          </template>
          <el-tag :type="workstationDetail.enableFlag === 'Y' ? 'success' : 'info'">
            {{ workstationDetail.enableFlag === 'Y' ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">线边库</div>
          </template>
          {{ workstationDetail.warehouseName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">库区</div>
          </template>
          {{ workstationDetail.areaName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">库位</div>
          </template>
          {{ workstationDetail.locationName }}
        </el-descriptions-item>
        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">备注</div>
          </template>
          {{ workstationDetail.remark }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- =====工作站下面的三类资源配置===== -->
    <el-card shadow="never">
      <el-tabs type="border-card">
        <el-tab-pane label="设备资源">
          <!-- 使用子组件开发内容，防止当前页面代码过多 -->
          <WorkstationMachine :workstation="workstationDetail"/>
        </el-tab-pane>
        <el-tab-pane label="人力配置">
          <workstation-worker :workstation="workstationDetail"/>
        </el-tab-pane>
        <el-tab-pane label="工装夹具">
          <workstation-tool :workstation="workstationDetail"/>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {ArrowLeftBold} from "@element-plus/icons-vue";
import {useRoute, useRouter} from 'vue-router';
import {getWorkstationById} from "@/api/md/workstation.js";
import {ElMessage} from "element-plus";
// 导入组件
import WorkstationMachine from "@/views/md/workstation/components/workstation-machine.vue";
import WorkstationWorker from "@/views/md/workstation/components/workstation-worker.vue";
import WorkstationTool from "@/views/md/workstation/components/workstation-tool.vue";

const route = useRoute();
const router = useRouter();
// 从请求参数中获取workstationId
let workstationId = route.params.workstationId;
// 缓存工作站详情
const workstationDetail = ref({});

//=====加载工作站详情 ===
const loadWorkstationDetail = async function () {
  let response = await getWorkstationById(workstationId);
  if (response.data) {
    workstationDetail.value = response.data;
  } else {
    ElMessage({
      message: "数据有误，请刷新页面重试",
      type: "warning"
    });
  }
}
loadWorkstationDetail();

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
