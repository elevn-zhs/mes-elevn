# 🧰 通用后台管理系统

> 基于 **Vue 3 + Vue Router 4 + Element Plus + Pinia + Axios + Vite + JavaScript** 的后台管理系统模板。

## 一、快速开始

### 1.1 环境要求

| 依赖 | 版本要求 |
|---|---|
| Node.js | ≥ 18 |
| npm | ≥ 9 |

```bash
node -v   # 查看 Node 版本
npm -v    # 查看 npm 版本
```

### 1.2 安装 & 运行

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:5173）
npm run dev

# 生产构建（产物输出到 dist/）
npm run build

# 本地预览构建产物
npm run preview
```

### 1.3 登录

模板内置了一个 Mock 登录页，默认账号：

| 用户名 | 密码 |
|---|---|
| admin | 123456 |

登录后会跳转首页 Dashboard。

## 二、目录结构

```
ok/
├── index.html                 # 入口 HTML
├── vite.config.js             # Vite 配置（含 @ 路径别名）
├── package.json
├── .gitignore
│
└── src/
    ├── main.js                # 应用入口：注册 ElementPlus / Pinia / Router
    ├── App.vue                # 根组件：只有一个 <router-view />
    │
    ├── api/                   # 👈 后端接口（按模块拆分）
    │   ├── auth.js            #    登录/登出/用户信息
    │   └── user.js            #    用户增删改查
    │
    ├── utils/                 # 👈 工具函数
    │   ├── request.js         #    Axios 封装（拦截器、错误处理）
    │   └── auth.js            #    Token 读写（基于 localStorage）
    │
    ├── stores/                # 👈 Pinia 状态管理
    │   ├── index.js           #    Pinia 实例入口
    │   └── modules/
    │       ├── user.js        #    用户状态（token / userInfo）
    │       ├── app.js         #    应用 UI 状态（侧边栏折叠）
    │       └── permission.js  #    动态路由生成
    │
    ├── router/
    │   └── index.js           # 👈 路由定义 + 全局守卫 + NProgress
    │
    ├── layout/                # 👈 主布局
    │   ├── index.vue          #    容器：Sidebar + Navbar + AppMain
    │   └── components/
    │       ├── Sidebar.vue    #    可折叠侧边栏（自动生成菜单）
    │       ├── Navbar.vue     #    顶部导航栏（面包屑 + 用户下拉）
    │       └── AppMain.vue    #    内容区域（带过渡动画）
    │
    ├── views/                 # 👈 业务页面
    │   ├── login/             #    登录页
    │   ├── dashboard/         #    首页仪表盘
    │   ├── error/404.vue      #    404 页面
    │   └── system/            #    系统管理模块
    │       ├── user/          #      用户管理（完整 CRUD 弹窗）
    │       ├── role/          #      角色管理（占位）
    │       └── menu/          #      菜单管理（占位）
    │
    ├── styles/
    │   └── index.css          # 全局样式重置
    │
    └── assets/                # 静态资源
```



## 三、核心模块详解

### 3.1 Axios 封装 — `src/utils/request.js`

这是所有 HTTP 请求的底层入口，做了三件事：

```
请求拦截器 ──→ 自动注入 Authorization Token
     │
     ▼
发送请求（超时 15s，baseURL 可配）
     │
     ▼
响应拦截器 ──→ 统一错误处理 + 401 自动跳登录
```

**关键配置点：**

```js
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',  // ← 改这里对接后端
  timeout: 15000
})
```

如果后端接口跨域，在 `vite.config.js` 加代理：

```js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://your-backend:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

### 3.2 接口层 — `src/api/*.js`

所有后端请求按模块拆分成独立文件，每个函数返回一个 axios 调用。**新增模块只需在 `api/` 下新建文件即可。**

```js
// src/api/user.js
import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params          // URL 查询参数
  })
}

export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data            // Request Body
  })
}
```

页面中使用：

```js
import { getUserList, createUser } from '@/api/user'

const res = await getUserList({ page: 1, size: 10 })
```

### 3.3 路由 & 权限 — `src/router/index.js` + `src/stores/modules/permission.js`

路由分两种：

| 类型 | 定义位置 | 说明 |
|---|---|---|
| **constantRoutes** | `router/index.js` | 登录页、404 页等无需权限的路由 |
| **asyncRoutes** | `stores/modules/permission.js` | 业务路由，登录后动态 `addRoute` 注入 |

**路由守卫流程：**

```
访问页面
   │
   ▼
有 Token？
   ├─ 是 → 去 /login？ ──→ 重定向首页
   │      │
   │      否 → 路由已加载？ ──→ 直接放行
   │              │
   │              否 → generateRoutes() 生成动态路由
   │                    → router.addRoute() 注册
   │                    → next({ ...to, replace: true }) 重新匹配
   │
   └─ 否 → 白名单？ ──→ 放行 / 跳转登录
```

**侧边栏菜单自动生成：** `Sidebar.vue` 直接遍历 `permissionStore.routes`，用路由的 `meta.title` 和 `meta.icon` 渲染，**新增路由就能自动出现在菜单里**，不用改组件。

### 3.4 Pinia Store — `src/stores/modules/*.js`

采用 Vue 3 推荐的 **Setup Store** 写法（`defineStore` + composition API）。

**三个内置 Store：**

| Store | 文件 | 职责 |
|---|---|---|
| `useUserStore` | `user.js` | 登录/登出/Token/userInfo |
| `useAppStore` | `app.js` | 侧边栏折叠状态 |
| `usePermissionStore` | `permission.js` | 动态路由、菜单生成 |

**使用示例（在组件中）：**

```js
import { useUserStore } from '@/stores/modules/user'

const userStore = useUserStore()
console.log(userStore.userInfo?.nickname)        // 读状态
await userStore.login({ username, password })    // 调 action
```

**新增 Store：** 直接在 `stores/modules/` 下新建文件即可，Pinia 实例已在 `stores/index.js` 创建并在 `main.js` 注册，无需额外配置。

### 3.5 Token 管理 — `src/utils/auth.js`

基于 `localStorage` 的简单封装，只有 5 个函数：

```js
import { getToken, setToken, removeToken, getUser, setUser, clearAuth } from '@/utils/auth'

setToken('abc123')       // 存
getToken()               // 取 → 'abc123'
removeToken()            // 删
clearAuth()              // 一键清 token + user
```

### 3.6 动态菜单 — `src/layout/components/Sidebar.vue`

菜单由路由配置自动驱动，**支持最多两级菜单**（三级及以上需要扩展组件）。菜单项的生成规则：

| 路由结构 | 渲染结果 |
|---|---|
| 有 1 个 child | 直接渲染成一级菜单项 |
| 有 2+ 个 children | 渲染成 `el-sub-menu`（可展开折叠） |
| `meta.hidden = true` | 不显示在菜单中 |

**图标映射：** `meta.icon` 写 Element Plus 图标组件名（字符串），组件内部通过 `<component :is="getIcon('User')">` 动态渲染。



## 四、实战：新增一个业务页面

以新增 **「公告管理」** 为例，完整步骤如下：

### Step 1：创建页面文件

```bash
mkdir src/views/system/notice
```

创建 `src/views/system/notice/index.vue`（最简骨架）：

```vue
<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>公告管理</span>
        <el-button type="primary">新增公告</el-button>
      </div>
    </template>
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作" width="160">
        <template #default>
          <el-button link type="primary">编辑</el-button>
          <el-button link type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
const tableData = ref([])
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

### Step 2：在 permission.js 注册路由

打开 `src/stores/modules/permission.js`，在 `asyncRoutes` 的 `/system` 下追加一个 child：

```js
{
  path: '/system',
  component: () => import('@/layout/index.vue'),
  redirect: '/system/user',
  meta: { title: '系统管理', icon: 'Setting' },
  children: [
    // ...已有的 user / role / menu
    {
      path: 'notice',                            // ← 新增
      name: 'Notice',
      component: () => import('@/views/system/notice/index.vue'),
      meta: { title: '公告管理', icon: 'Bell' }  // ← Bell 是 Element Plus 图标名
    }
  ]
}
```

### Step 3：完成

刷新页面 → 登录后侧边栏「系统管理」下面就多了一个 **公告管理**，点击即可进入新页面。

> 💡 如果你想让这个页面成为顶级菜单（不带父级分组），把它放到 `asyncRoutes` 数组的**第一层**，和 `/system`、`/:pathMatch(.*)*` 平级即可。



## 五、实战：对接真实后端

模板的登录是 Mock 的，对接真实后端只需改 **一个地方** — 把 `src/views/login/index.vue` 里 Mock 登录那段替换为调用 API：

```js
// 原来（Mock）：
// import('@/utils/auth').then(({ setToken, setUser }) => { ... })

// 改成（真实后端）：
const res = await userStore.login(loginForm)
```

前提是你的后端接口符合 `src/api/auth.js` 的约定：

| 接口 | 方法 | 路径 | 期望返回 |
|---|---|---|---|
| 登录 | POST | `/auth/login` | `{ code: 200, data: { token, user } }` |
| 登出 | POST | `/auth/logout` | `{ code: 200 }` |
| 用户信息 | GET | `/auth/userinfo` | `{ code: 200, data: {...} }` |

如果后端接口路径或返回结构不同，直接改 `src/api/*.js` 里的 URL，以及 `request.js` 里的响应拦截器判断逻辑（当前判断 `res.code !== 200`）。



## 六、常用开发技巧

### 6.1 Element Plus 图标全局注册

`main.js` 里做了全局注册：

```js
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
```

所以在模板中可以直接用任意图标组件，不用单独 import：

```vue
<el-icon><Bell /></el-icon>
<el-icon><Edit /></el-icon>
```

### 6.2 路由懒加载

所有路由组件都用了动态 import：

```js
component: () => import('@/views/dashboard/index.vue')
```

Vite 会自动按路由分包，首屏只加载必要代码，其余在切换时才加载。

### 6.3 路径别名 `@`

`vite.config.js` 配置了 `@ → ./src`，所以：

```js
import request from '@/utils/request'      // ✅
import Sidebar from '@/layout/Sidebar.vue'  // ✅
```

不再需要写 `../../utils/request` 这种相对路径。

### 6.4 Pinia vs Vuex

模板选择 Pinia 的原因：
- Vue 3 官方推荐，Vuex 已停止维护
- 支持 TypeScript 更好
- API 更简洁，没有 mutations，只有 state + getters + actions
- 支持 composition API 风格

### 6.5 NProgress

路由切换时自动显示顶部进度条，配置在 `router/index.js`：

```js
router.beforeEach(() => NProgress.start())
router.afterEach(() => NProgress.done())
```

如需禁用小圆圈，已经在配置里设了 `NProgress.configure({ showSpinner: false })`。



## 七、菜单路径拼接原理

Sidebar 遍历路由时会拼接完整路径传给 `el-menu-item` 的 `index`：

```
父路由 path = "/"  +  子路由 path = "dashboard"  →  "/dashboard"  ✅
父路由 path = "/system"  +  子路由 path = "user"  →  "/system/user"  ✅
```

关键是父路由是 `/` 时不能拼出 `//dashboard`，所以有个 `resolvePath` 函数做了特殊处理。**新增路由时如果遇到菜单点了没反应/跳 404，检查一下路径拼接是否正确。**



## 八、npm scripts 速查

```bash
npm run dev       # 启动开发服务器 (port 5173)
npm run build     # 生产构建 → dist/
npm run preview   # 本地预览 dist/
npm run fund      # 查看依赖捐赠情况
```



## 九、扩展建议清单

| 方向 | 建议 |
|---|---|
| 权限控制 | 在 `permission.js` 里根据用户角色过滤 asyncRoutes |
| 国际化 | 集成 `vue-i18n`，给 Element Plus 传 locale |
| 主题切换 | Element Plus 支持动态 CSS 变量，可实现暗黑模式 |
| 多标签页 | 给 AppMain 加一层 Tabs 组件（类似 vue-element-admin） |
| 图表 | 首页 Dashboard 可接入 ECharts 展示真实数据 |
| 持久化 | Pinia 可配合 `pinia-plugin-persistedstate` 做 store 持久化 |
| 请求取消 | Axios 拦截器中配合 `AbortController` 处理快速连续请求 |
| Mock 数据 | 接入 `vite-plugin-mock`，前后端并行开发 |



## 十、依赖版本速查

```json
{
  "vue": "^3.5.13",
  "vue-router": "^4.6.4",
  "pinia": "^4.0.3",
  "element-plus": "^2.14.5",
  "@element-plus/icons-vue": "^2.3.2",
  "axios": "^1.20.0",
  "nprogress": "^0.2.0",
  "vite": "^6.3.5",
  "@vitejs/plugin-vue": "^5.2.3"
}
```