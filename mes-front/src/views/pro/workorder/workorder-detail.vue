<template>
  <div class="pro-workorder-detail">
    <el-card shadow="never">
      <span>
        <el-button @click="handleBack"><el-icon><ArrowLeftBold /></el-icon></el-button>
        生产工单详情
      </span>
    </el-card>

    <!-- =====工单基本信息（只读，改基本信息回列表页点编辑）===== -->
    <el-card shadow="never">
      <el-descriptions
          class="margin-top"
          title="工单基本信息"
          :column="3"
          border
      >
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工单编码</div>
          </template>
          {{ workorderDetail.workorderCode }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工单名称</div>
          </template>
          {{ workorderDetail.workorderName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">单据状态</div>
          </template>
          <el-tag :type="statusTagType(workorderDetail.status)">
            {{ statusLabel(workorderDetail.status) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item>
          <template #label>
            <div class="cell-item">工单类型</div>
          </template>
          <el-tag :type="typeTagType(workorderDetail.workorderType)" effect="plain">
            {{ typeLabel(workorderDetail.workorderType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">来源类型</div>
          </template>
          <el-tag :type="workorderDetail.orderSource === 'ORDER' ? 'primary' : 'success'" effect="plain">
            {{ sourceLabel(workorderDetail.orderSource) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">是否需排产</div>
          </template>
          <!-- 只有自产工单排产，这里直接把判断结果摆出来，省得看的人去翻规则 -->
          <el-tag v-if="workorderDetail.workorderType === 'SELF'" type="success" effect="plain">需排产</el-tag>
          <span v-else class="text-muted">不排产（外协/外购）</span>
        </el-descriptions-item>

        <el-descriptions-item>
          <template #label>
            <div class="cell-item">订单编号</div>
          </template>
          {{ workorderDetail.sourceCode || '—' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">客户</div>
          </template>
          {{ workorderDetail.clientName || '—' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">供应商</div>
          </template>
          {{ workorderDetail.vendorName || '—' }}
        </el-descriptions-item>

        <el-descriptions-item>
          <template #label>
            <div class="cell-item">批次号</div>
          </template>
          {{ workorderDetail.batchCode || '—' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">需求日期</div>
          </template>
          {{ workorderDetail.requestDate || '—' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">父工单</div>
          </template>
          {{ workorderDetail.parentId ? workorderDetail.parentId : '顶层工单' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- =====产品与数量（单独一张卡，数量是工单的核心）===== -->
    <el-card shadow="never">
      <el-descriptions
          class="margin-top"
          title="产品与数量"
          :column="3"
          border
      >
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">生产产品</div>
          </template>
          {{ workorderDetail.productCode }} {{ workorderDetail.productName }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">规格型号</div>
          </template>
          {{ workorderDetail.productSpc || '—' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">单位</div>
          </template>
          {{ workorderDetail.unitOfMeasure || '—' }}
        </el-descriptions-item>

        <el-descriptions-item>
          <template #label>
            <div class="cell-item">生产数量</div>
          </template>
          <b>{{ formatQty(workorderDetail.quantity) }}</b> {{ workorderDetail.unitOfMeasure }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">已排产数量</div>
          </template>
          <!-- 不排产的工单这一栏没意义，直接写"不排产"比显示 0 更清楚 -->
          <span v-if="workorderDetail.workorderType === 'SELF'">
            <b class="qty-scheduled">{{ formatQty(workorderDetail.quantityScheduled) }}</b>
            {{ workorderDetail.unitOfMeasure }}
          </span>
          <span v-else class="text-muted">不排产</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">已生产数量</div>
          </template>
          <b class="qty-produced">{{ formatQty(workorderDetail.quantityProduced) }}</b>
          {{ workorderDetail.unitOfMeasure }}
        </el-descriptions-item>

        <el-descriptions-item :span="3">
          <template #label>
            <div class="cell-item">备注</div>
          </template>
          {{ workorderDetail.remark || '—' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 状态流转信息：有就显示，没有就不占位置 -->
      <div v-if="showStatusFlow" class="status-flow">
        <el-divider content-position="left">状态流转</el-divider>
        <el-descriptions :column="3" border>
          <el-descriptions-item v-if="workorderDetail.createTime">
            <template #label>
              <div class="cell-item">创建</div>
            </template>
            {{ workorderDetail.createBy || '—' }} · {{ workorderDetail.createTime }}
          </el-descriptions-item>
          <el-descriptions-item v-if="workorderDetail.finishDate">
            <template #label>
              <div class="cell-item">完工时间</div>
            </template>
            {{ workorderDetail.finishDate }}
          </el-descriptions-item>
          <el-descriptions-item v-if="workorderDetail.cancelDate">
            <template #label>
              <div class="cell-item">取消时间</div>
            </template>
            {{ workorderDetail.cancelDate }}
          </el-descriptions-item>
          <el-descriptions-item v-if="workorderDetail.updateTime">
            <template #label>
              <div class="cell-item">最后更新</div>
            </template>
            {{ workorderDetail.updateBy || '—' }} · {{ workorderDetail.updateTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- =====工单下面的子表数据===== -->
    <el-card shadow="never">
      <el-tabs type="border-card">
        <el-tab-pane label="工单用料">
          <!-- 使用子组件开发内容，防止当前页面代码过多 -->
          <WorkorderBom :workorder="workorderDetail" @rebuild="reloadDetail"/>
        </el-tab-pane>
        <el-tab-pane label="工序任务">
          <!-- 自产工单排产后才有数据，其余情况子组件会给空状态提示 -->
          <WorkorderTask :workorder="workorderDetail" @refresh="reloadDetail"/>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import {ref, computed} from 'vue';
import {ArrowLeftBold} from "@element-plus/icons-vue";
import {useRoute, useRouter} from 'vue-router';
import {getWorkorderById} from "@/api/pro/workorder.js";
import {getDictDataListByType} from "@/api/sys/dictData.js";
import {ElMessage} from "element-plus";
// 导入组件
import WorkorderBom from "@/views/pro/workorder/components/workorder-bom.vue";
import WorkorderTask from "@/views/pro/workorder/components/workorder-task.vue";

const route = useRoute();
const router = useRouter();
// 从请求参数中获取工单ID
let workorderId = route.params.workorderId;
// 缓存工单详情
const workorderDetail = ref({});

// ==================== 字典（只看不改，单独拉一份不依赖列表页） ====================

const workorderTypeOptions = ref([]);
const orderSourceOptions = ref([]);
const statusOptions = ref([]);

/** 按字典类型拉数据，只取启用的 */
const loadDict = async function (type, target) {
  const result = await getDictDataListByType(type);
  target.value = (result.data || []).filter(d => d.status === '0');
};

/** 字典值转标签，查不到就原样显示，别显示成空白 */
const dictLabel = function (options, value) {
  const hit = options.value.find(d => d.dictValue === value);
  return hit ? hit.dictLabel : (value || '—');
};

const typeLabel = (v) => dictLabel(workorderTypeOptions, v);
const sourceLabel = (v) => dictLabel(orderSourceOptions, v);
const statusLabel = (v) => dictLabel(statusOptions, v);

/** 标签颜色跟字典的 list_class 走 */
const dictTagType = function (options, value) {
  const hit = options.value.find(d => d.dictValue === value);
  const cls = hit?.listClass;
  if (cls === 'primary' || !cls) return '';
  if (['success', 'info', 'warning', 'danger'].includes(cls)) return cls;
  return '';
};

const typeTagType = (v) => dictTagType(workorderTypeOptions, v);
const statusTagType = (v) => dictTagType(statusOptions, v);

/** 有没有状态流转痕迹，没有就整块不显示 */
const showStatusFlow = computed(() => {
  const d = workorderDetail.value;
  return !!(d.createTime || d.finishDate || d.cancelDate || d.updateTime);
});

// ==================== 数据加载 ====================

/** 加载工单详情 */
const loadWorkorderDetail = async function () {
  let response = await getWorkorderById(workorderId);
  if (response.data) {
    workorderDetail.value = response.data;
  } else {
    ElMessage({
      message: "数据有误，请刷新页面重试",
      type: "warning"
    });
  }
};

/** 重新展开用料后调用：工单的已排产/已生产数量没变，但用料行会变，需要子组件自己刷新，这里只刷新主表 */
const reloadDetail = function () {
  loadWorkorderDetail();
};

// ==================== 回退按钮事件 ====================

const handleBack = function () {
  router.back();
};

// ==================== 小工具 ====================

/** 数量去掉无意义的尾部 0，1.000000 显示成 1 */
const formatQty = function (v) {
  if (v === null || v === undefined || v === '') return '0';
  const n = Number(v);
  if (Number.isNaN(n)) return String(v);
  return String(Number(n.toFixed(6)));
};

// 字典和详情并行拉，少一轮等待
Promise.all([
  loadDict('workorder_type', workorderTypeOptions),
  loadDict('order_source', orderSourceOptions),
  loadDict('production_order_status', statusOptions),
  loadWorkorderDetail()
]);
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

.status-flow {
  margin-top: 4px;
}

.qty-scheduled {
  color: #409EFF;
}

.qty-produced {
  color: #67C23A;
}

.text-muted {
  color: #909399;
}
</style>
