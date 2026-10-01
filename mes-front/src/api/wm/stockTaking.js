import request from '@/utils/request'

/**
 * 盘点接口（计划 → 生成 → 录入 → 过账 → 差异）
 * 【盘点是库存唯一的"非单据"写入口】过账时盈亏差异直接写库存 + 流水
 */

// ==================== 计划 ====================

/** 新增盘点计划（body 带 scopeList：WAREHOUSE/AREA/ITEM_TYPE 三类范围可混选） */
export function createTakingPlan(data) {
  return request({ url: '/wm/stock/taking/plan', method: 'post', data })
}

/** 分页查询盘点计划 */
export function getTakingPlanByPage(params) {
  return request({ url: '/wm/stock/taking/plan/page', method: 'get', params })
}

/** 计划详情（含范围行） */
export function getTakingPlanById(planId) {
  return request({ url: `/wm/stock/taking/plan/${planId}`, method: 'get' })
}

/** 编辑计划（仅"待执行"；范围整批替换） */
export function updateTakingPlan(data) {
  return request({ url: '/wm/stock/taking/plan', method: 'put', data })
}

/** 删除计划（已生成盘点单的禁止删） */
export function deleteTakingPlan(planId) {
  return request({ url: `/wm/stock/taking/plan/${planId}`, method: 'delete' })
}

// ==================== 生成 / 盘点单 ====================

/** 按计划展开范围生成盘点单（明细行带账面数量） */
export function generateTaking(planId) {
  return request({ url: `/wm/stock/taking/generate/${planId}`, method: 'post' })
}

/** 分页查询盘点单 */
export function getTakingByPage(params) {
  return request({ url: '/wm/stock/taking/page', method: 'get', params })
}

/** 盘点单详情（含明细行；盲盘单不回传账面数量） */
export function getTakingById(takingId) {
  return request({ url: `/wm/stock/taking/${takingId}`, method: 'get' })
}

/** 批量录入实盘数（body 是 [{lineId, takingQuantity}]；差异后端算） */
export function saveTakingQuantities(takingId, lines) {
  return request({ url: `/wm/stock/taking/${takingId}/lines`, method: 'put', data: lines })
}

/** 过账：盈亏写库存 + 流水（一个事务） */
export function postTaking(takingId) {
  return request({ url: `/wm/stock/taking/post/${takingId}`, method: 'post' })
}

/** 差异明细（diff != 0 的行） */
export function getTakingDiff(takingId) {
  return request({ url: `/wm/stock/taking/${takingId}/diff`, method: 'get' })
}

/** 删除盘点单（仅"待录入"） */
export function deleteTaking(takingId) {
  return request({ url: `/wm/stock/taking/${takingId}`, method: 'delete' })
}
