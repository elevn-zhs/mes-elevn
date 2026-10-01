import request from '@/utils/request'

/**
 * 装箱管理接口
 * 【装箱不动库存】箱里装的是库存行上的货，加行时选库存行即可，
 * 快照（物料/批次/库位）由后端从库存回填
 */

/** 分页查询装箱单（按箱号/销售订单/客户/状态/装箱日期区间筛选） */
export function getPackageByPage(params) {
  return request({ url: '/wm/package/page', method: 'get', params })
}

/** 装箱单详情（表头 + 明细行 + 直接子箱） */
export function getPackageById(packageId) {
  return request({ url: `/wm/package/${packageId}`, method: 'get' })
}

/** 新增箱（parentId 传 0/不传 = 顶层箱；传父箱 ID = 箱套箱） */
export function createPackage(data) {
  return request({ url: '/wm/package', method: 'post', data })
}

/** 编辑箱信息（编号/父级/状态不可改，仅"装箱中"可编辑） */
export function updatePackage(data) {
  return request({ url: '/wm/package', method: 'put', data })
}

/** 完成装箱（PREPARE → PACKED，明细锁定） */
export function finishPackage(packageId) {
  return request({ url: `/wm/package/finish/${packageId}`, method: 'put' })
}

/** 删除箱（有已完成子箱拦截；删除时连子树和明细一起删） */
export function deletePackageById(packageId) {
  return request({ url: `/wm/package/${packageId}`, method: 'delete' })
}

/** 加明细行：materialStockId + quantity，快照后端从库存行回填 */
export function createPackageLine(data) {
  return request({ url: '/wm/package/line', method: 'post', data })
}

/** 改明细行（只允许改数量与备注） */
export function updatePackageLine(data) {
  return request({ url: '/wm/package/line', method: 'put', data })
}

/** 删明细行 */
export function deletePackageLine(lineId) {
  return request({ url: `/wm/package/line/${lineId}`, method: 'delete' })
}
