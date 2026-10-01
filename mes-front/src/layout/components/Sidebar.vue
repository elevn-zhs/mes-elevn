<template>
  <div class="sidebar" :class="{ 'is-collapse': appStore.sidebarCollapsed }">
    <div class="logo">
      <span v-if="!appStore.sidebarCollapsed">后台管理系统</span>
      <span v-else>Admin</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebarCollapsed"
      :unique-opened="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
      class="sidebar-menu"
    >
      <template v-for="route in routes" :key="route.path">
        <el-sub-menu
          v-if="route.children && route.children.length > 1"
          :index="route.path"
        >
          <template #title>
            <el-icon v-if="route.meta?.icon">
              <component :is="getIcon(route.meta.icon)" />
            </el-icon>
            <span>{{ route.meta?.title }}</span>
          </template>
          <template  v-for="child in route.children" :key="child.path">
            <el-menu-item
              v-if="!child.hidden"
              :index="resolvePath(route.path, child.path)"
            >
              <el-icon v-if="child.meta?.icon">
                <component :is="getIcon(child.meta.icon)" />
              </el-icon>
              <span>{{ child.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-sub-menu>
        <el-menu-item
          v-else-if="route.children && route.children.length === 1 && !route.hidden"
          :index="resolvePath(route.path, route.children[0].path)"
        >
          <el-icon v-if="route.children[0].meta?.icon">
            <component :is="getIcon(route.children[0].meta.icon)" />
          </el-icon>
          <template #title>
            <span>{{ route.children[0].meta?.title }}</span>
          </template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/modules/app'
import { usePermissionStore } from '@/stores/modules/permission'

const appStore = useAppStore()
const permissionStore = usePermissionStore()
const route = useRoute()

const routes = computed(() => permissionStore.routes)
const activeMenu = computed(() => route.path)

function resolvePath(parent, child) {
  if (parent === '/') {
    return '/' + child
  }
  return parent + '/' + child
}

function getIcon(name) {
  return ElementPlusIconsVue[name] || 'Menu'
}
</script>

<style scoped>
.sidebar {
  width: 210px;
  height: 100%;
  background-color: #304156;
  overflow: hidden;
  transition: width 0.28s;
  display: flex;
  flex-direction: column;
}

.sidebar.is-collapse {
  width: 64px;
}

.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  background-color: #2b2f3a;
  letter-spacing: 1px;
  overflow: hidden;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}

.sidebar-menu:not(.is-collapse) {
  width: 210px;
}
</style>