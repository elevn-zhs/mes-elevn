<template>
  <div class="pro-route-detail">
    <el-card shadow="never">
      <span>
        <el-button @click="handleBack"><el-icon><ArrowLeftBold /></el-icon></el-button>
        工艺路线详情
      </span>
    </el-card>

    <!-- =====路线基本信息（只读，改基本信息回列表页点编辑）===== -->
    <el-card shadow="never">
      <el-descriptions
          class="margin-top"
          title="路线基本信息"
          :column="3"
          border
      >
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">路线编码</div>
          </template>
          {{ routeDetail.routeCode }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">路线名称</div>
          </template>
          {{ routeDetail.routeName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">状态</div>
          </template>
          <el-tag :type="routeDetail.enableFlag === 'Y' ? 'success' : 'info'">
            {{ routeDetail.enableFlag === 'Y' ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">路线描述</div>
          </template>
          {{ routeDetail.routeDesc || '—' }}
        </el-descriptions-item>
        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">备注</div>
          </template>
          {{ routeDetail.remark || '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- =====路线下面的子表数据===== -->
    <el-card shadow="never">
      <el-tabs type="border-card">
        <el-tab-pane label="路线工序">
          <!-- 使用子组件开发内容，防止当前页面代码过多 -->
          <RouteProcess :route="routeDetail"/>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {ArrowLeftBold} from "@element-plus/icons-vue";
import {useRoute, useRouter} from 'vue-router';
import {getRouteById} from "@/api/pro/route.js";
import {ElMessage} from "element-plus";
// 导入组件
import RouteProcess from "@/views/pro/route/components/route-process.vue";

const route = useRoute();
const router = useRouter();
// 从请求参数中获取routeId
let routeId = route.params.routeId;
// 缓存路线详情
const routeDetail = ref({});

//=====加载路线详情 ===
const loadRouteDetail = async function () {
  let response = await getRouteById(routeId);
  if (response.data) {
    routeDetail.value = response.data;
  } else {
    ElMessage({
      message: "数据有误，请刷新页面重试",
      type: "warning"
    });
  }
}
loadRouteDetail();

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
