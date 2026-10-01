import request from '@/utils/request'

/**
 * SN（一物一码）接口：生成 → 绑定 → 追溯
 * 追溯读 A 线的 pro_sn_process（A 线写入，B 线只读）
 */

/** 分页查询（按 SN/产品/批次/状态/工单筛选） */
export function getSnByPage(params) {
  return request({ url: '/wm/sn/page', method: 'get', params })
}

/** SN 详情 */
export function getSnById(snId) {
  return request({ url: `/wm/sn/${snId}`, method: 'get' })
}

/**
 * 批量生成 SN 并绑定
 * body: { batchCode, itemId, workorderId, count }
 */
export function generateSn(data) {
  return request({ url: '/wm/sn/generate', method: 'post', data })
}

/** 重新绑定批次/工单（仅"在库"） */
export function bindSn(data) {
  return request({ url: '/wm/sn/bind', method: 'put', data })
}

/** 改状态（IN_STOCK / SHIPPED / FROZEN） */
export function updateSnStatus(snId, status) {
  return request({ url: `/wm/sn/status/${snId}`, method: 'put', params: { status } })
}

/** 删除（仅"在库"） */
export function deleteSnById(snId) {
  return request({ url: `/wm/sn/${snId}`, method: 'delete' })
}

/** 单件全流程追溯（过站记录按工序顺序；A 线未接数据时为空） */
export function traceSn(snId) {
  return request({ url: `/wm/sn/${snId}/trace`, method: 'get' })
}
