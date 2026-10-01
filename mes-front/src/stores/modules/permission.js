import { defineStore } from 'pinia'
import { ref } from 'vue'

const WHITE_LIST = ['/login', '/404']

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([])

  const asyncRoutes = [
    {
      path: '/ai',
      component: () => import('@/layout/index.vue'),
      children: [
        {
          path: 'chat',
          name: 'chat',
          component: () => import('@/views/chat/index.vue'),
          meta: { title: 'AI助手', icon: 'Odometer' }
        }
      ]
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '首页', icon: 'Odometer' }
        }
      ]
    },
    {
      path: '/system',
      component: () => import('@/layout/index.vue'),
      redirect: '/system/user',
      meta: { title: '系统管理', icon: 'Setting' },
      children: [
        {
          path: 'user',
          name: 'User',
          component: () => import('@/views/system/user/index.vue'),
          meta: { title: '用户管理', icon: 'User' }
        },
        {
          path: 'dept',
          name: 'Dept',
          component: () => import('@/views/system/dept/index.vue'),
          meta: { title: '部门管理', icon: 'OfficeBuilding' }
        },
        {
          path: 'post',
          name: 'Post',
          component: () => import('@/views/system/post/index.vue'),
          meta: { title: '岗位管理', icon: 'Postcard' }
        },
        {
          path: 'role',
          name: 'Role',
          component: () => import('@/views/system/role/index.vue'),
          meta: { title: '角色管理', icon: 'Avatar' }
        },
        {
          path: 'menu',
          name: 'Menu',
          component: () => import('@/views/system/menu/index.vue'),
          meta: { title: '菜单管理', icon: 'Menu' }
        },
        {
          path: 'dictType',
          name: 'DictType',
          component: () => import('@/views/system/dictType/index.vue'),
          meta: { title: '字典管理', icon: 'Collection' }
        },
        {
          path: 'dictData/:dictType',
          name: 'DictData',
          hidden:true,
          component: () => import('@/views/system/dictData/index.vue'),
          meta: { title: '字典数据', icon: 'Grid' }
        },
        {
          path: 'log',
          name: 'Log',
          component: () => import('@/views/system/log/index.vue'),
          meta: { title: '操作日志', icon: 'Document' }
        },
        {
          path: 'codingRule',
          name: 'codingRule',
          component: () => import('@/views/system/codingRule/index.vue'),
          meta: { title: '编码规则', icon: 'Code' }
        }
      ]
    },
    {
      path: '/md',
      component: () => import('@/layout/index.vue'),
      redirect: '/md/item',
      meta: { title: '主数据管理', icon: 'Grid' },
      children: [
        {
          path: 'item',
          name: 'MdItem',
          component: () => import('@/views/md/item/index.vue'),
          meta: { title: '物料产品', icon: 'Box' }
        },
        {
          path: 'itemType',
          name: 'MdItemType',
          component: () => import('@/views/md/itemType/index.vue'),
          meta: { title: '物料分类', icon: 'Files' }
        },
        {
          path: 'vendor',
          name: 'MdVendor',
          component: () => import('@/views/md/vendor/index.vue'),
          meta: { title: '供应商', icon: 'Shop' }
        },
        {
          path: 'client',
          name: 'MdClient',
          component: () => import('@/views/md/client/index.vue'),
          meta: { title: '客户', icon: 'UserFilled' }
        },
        {
          path: 'unitMeasure',
          name: 'MdUnitMeasure',
          component: () => import('@/views/md/unitMeasure/index.vue'),
          meta: { title: '计量单位', icon: 'Coin' }
        },
        {
          path: 'workshop',
          name: 'MdWorkshop',
          component: () => import('@/views/md/workshop/index.vue'),
          meta: { title: '车间', icon: 'OfficeBuilding' }
        },
        {
          path: 'workstation',
          name: 'MdWorkstation',
          component: () => import('@/views/md/workstation/index.vue'),
          meta: { title: '工作站', icon: 'Monitor' }
        },
        {
          path: 'itemDetail/:itemId',
          name: 'itemDetail',
          hidden:true,
          component: () => import('@/views/md/item/item-detail.vue'),
          meta: { title: '物料详情', icon: 'Tools' }
        },
        {
          path: 'workstationDetail/:workstationId',
          name: 'workstationDetail',
          hidden:true,
          component: () => import('@/views/md/workstation/workstation-detail.vue'),
          meta: { title: '工作站详情', icon: 'Monitor' }
        }
      ]
    },
    {
      path: '/pro',
      component: () => import('@/layout/index.vue'),
      redirect: '/pro/process',
      meta: { title: '生产管理', icon: 'Operation' },
      children: [
        {
          path: 'process',
          name: 'ProProcess',
          component: () => import('@/views/pro/process/index.vue'),
          meta: { title: '工序设置', icon: 'SetUp' }
        },
        {
          path: 'processDetail/:processId',
          name: 'processDetail',
          hidden:true,
          component: () => import('@/views/pro/process/process-detail.vue'),
          meta: { title: '工序详情', icon: 'SetUp' }
        },
        {
          path: 'route',
          name: 'ProRoute',
          component: () => import('@/views/pro/route/index.vue'),
          meta: { title: '工艺路线', icon: 'Guide' }
        },
        {
          path: 'routeDetail/:routeId',
          name: 'routeDetail',
          hidden:true,
          component: () => import('@/views/pro/route/route-detail.vue'),
          meta: { title: '路线详情', icon: 'Guide' }
        },
        {
          path: 'workorder',
          name: 'ProWorkorder',
          component: () => import('@/views/pro/workorder/index.vue'),
          meta: { title: '生产工单', icon: 'Tickets' }
        },
        {
          path: 'workorderDetail/:workorderId',
          name: 'workorderDetail',
          hidden:true,
          component: () => import('@/views/pro/workorder/workorder-detail.vue'),
          meta: { title: '工单详情', icon: 'Tickets' }
        },
        {
          path: 'task',
          name: 'ProTask',
          component: () => import('@/views/pro/task/index.vue'),
          meta: { title: '生产任务', icon: 'Calendar' }
        },
        {
          path: 'feedback',
          name: 'ProFeedback',
          component: () => import('@/views/pro/feedback/index.vue'),
          meta: { title: '生产报工', icon: 'DocumentAdd' }
        },
        {
          path: 'card',
          name: 'ProCard',
          component: () => import('@/views/pro/card/index.vue'),
          meta: { title: '工序流转', icon: 'Sort' }
        },
        {
          path: 'consume',
          name: 'ProConsume',
          component: () => import('@/views/pro/consume/index.vue'),
          meta: { title: '物料消耗', icon: 'Coin' }
        }
      ]
    },
    {
      path: '/wm',
      component: () => import('@/layout/index.vue'),
      redirect: '/wm/warehouse',
      meta: { title: '仓储管理', icon: 'Box' },
      children: [
        {
          path: 'warehouse',
          name: 'WmWarehouse',
          component: () => import('@/views/wm/warehouse/index.vue'),
          meta: { title: '仓库设置', icon: 'OfficeBuilding' }
        },
        {
          path: 'location',
          name: 'WmLocation',
          component: () => import('@/views/wm/location/index.vue'),
          meta: { title: '库区库位', icon: 'Grid' }
        },
        {
          path: 'batch',
          name: 'WmBatch',
          component: () => import('@/views/wm/batch/index.vue'),
          meta: { title: '批次管理', icon: 'Collection' }
        },
        {
          path: 'doc',
          name: 'WmDoc',
          component: () => import('@/views/wm/doc/index.vue'),
          meta: { title: '出入库单据', icon: 'Document' }
        },
        {
          path: 'stock',
          name: 'WmStock',
          component: () => import('@/views/wm/stock/index.vue'),
          meta: { title: '库存查询', icon: 'Coin' }
        },
        {
          path: 'transaction',
          name: 'WmTransaction',
          component: () => import('@/views/wm/transaction/index.vue'),
          meta: { title: '库存事务', icon: 'List' }
        },
        {
          path: 'notice',
          name: 'WmNotice',
          component: () => import('@/views/wm/notice/index.vue'),
          meta: { title: '仓储通知单', icon: 'Bell' }
        },
        {
          path: 'package',
          name: 'WmPackage',
          component: () => import('@/views/wm/package/index.vue'),
          meta: { title: '装箱管理', icon: 'Box' }
        },
        {
          path: 'taking',
          name: 'WmTaking',
          component: () => import('@/views/wm/taking/index.vue'),
          meta: { title: '盘点管理', icon: 'Files' }
        },
        {
          path: 'barcode',
          name: 'WmBarcode',
          component: () => import('@/views/wm/barcode/index.vue'),
          meta: { title: '条码管理', icon: 'Postcard' }
        },
        {
          path: 'sn',
          name: 'WmSn',
          component: () => import('@/views/wm/sn/index.vue'),
          meta: { title: 'SN管理', icon: 'Stamp' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/404'
    }
  ]

  function generateRoutes() {
    routes.value = asyncRoutes
    return asyncRoutes
  }

  return {
    routes,
    asyncRoutes,
    WHITE_LIST,
    generateRoutes
  }
})