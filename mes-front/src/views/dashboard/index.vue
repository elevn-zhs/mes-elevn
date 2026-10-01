<template>
  <div class="dashboard-wrapper">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">欢迎使用</span>
          </template>
          <div class="welcome-content">
            <h3>通用后台管理系统</h3>
            <p>这是一个基于 Vue3 + VueRouter4 + ElementPlus + Axios + Pinia 搭建的通用后台管理系统模板。</p>
            <el-divider />
            <h4>技术栈</h4>
            <div class="tech-tags">
              <el-tag v-for="tag in techStack" :key="tag" type="info" effect="plain" class="tag">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">快捷操作</span>
          </template>
          <div class="quick-actions">
            <div
              v-for="action in quickActions"
              :key="action.title"
              class="action-item"
              @click="action.handler"
            >
              <el-icon :size="24"><component :is="action.icon" /></el-icon>
              <span>{{ action.title }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  User,
  ShoppingCart,
  Document,
  Money,
  Setting,
  Plus,
  RefreshRight,
  Right
} from '@element-plus/icons-vue'

const router = useRouter()

const stats = [
  { title: '用户总数', value: '1,286', icon: User, color: '#409EFF' },
  { title: '今日订单', value: '342', icon: ShoppingCart, color: '#67C23A' },
  { title: '文章数', value: '5,892', icon: Document, color: '#E6A23C' },
  { title: '收入金额', value: '¥ 98,765', icon: Money, color: '#F56C6C' }
]

const techStack = ['Vue 3', 'Vue Router 4', 'Element Plus', 'Axios', 'Pinia', 'Vite']

const quickActions = [
  { title: '用户管理', icon: User, handler: () => router.push('/system/user') },
  { title: '角色管理', icon: Setting, handler: () => router.push('/system/role') },
  { title: '刷新页面', icon: RefreshRight, handler: () => window.location.reload() },
  { title: '查看文档', icon: Right, handler: () => window.open('https://element-plus.org') }
]
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.welcome-content h3 {
  margin: 0 0 12px;
  color: #303133;
}

.welcome-content h4 {
  margin: 16px 0 12px;
  color: #606266;
}

.welcome-content p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  margin: 0;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 10px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  color: #606266;
  transition: all 0.2s;
}

.action-item:hover {
  border-color: #409EFF;
  color: #409EFF;
  transform: translateY(-2px);
}
</style>