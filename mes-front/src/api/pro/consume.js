import request from '@/utils/request.js'

/**
 * 物料消耗相关接口
 * 后端 ProTransConsumeController 的 @RequestMapping 是 "/api/pro/consume"
 *
 * 注意：消耗记录没有新增/修改接口 —— 它是报工倒冲（backflush）自动生成的，
 * 报工成功的那一刻，料账就已经跟着落好了。
 * 前端只提供"看"的入口 + 删单条误录的口子。
 */

/**
 * 分页查询消耗记录列表
 * @param {Object} params 查询条件（taskId / workorderId / processId / itemCode /
 *                        itemName / workorderCode / taskCode / transOrderCode /
 *                        batchCode / consumeDateFrom / consumeDateTo）+ pageNum + pageSize
 */
export function getConsumeByPage(params) {
  return request({
    url: '/pro/consume/page',
    method: 'get',
    params
  })
}

/**
 * 消耗记录详情
 * @param {number} recordId 记录ID
 */
export function getConsumeById(recordId) {
  return request({
    url: `/pro/consume/${recordId}`,
    method: 'get'
  })
}

/**
 * 按任务查消耗明细（任务展开看"这道工序吃掉了什么料"）
 * @param {number} taskId 生产任务ID
 */
export function getConsumeListByTask(taskId) {
  return request({
    url: `/pro/consume/listByTask/${taskId}`,
    method: 'get'
  })
}

/**
 * 按工单查消耗明细
 * @param {number} workorderId 生产工单ID
 */
export function getConsumeListByWorkorder(workorderId) {
  return request({
    url: `/pro/consume/listByWorkorder/${workorderId}`,
    method: 'get'
  })
}

/**
 * 按工单汇总各物料消耗（同一物料合并成一行）
 * @param {number} workorderId 生产工单ID
 */
export function getConsumeSummaryByWorkorder(workorderId) {
  return request({
    url: `/pro/consume/summaryByWorkorder/${workorderId}`,
    method: 'get'
  })
}

/**
 * 工序用料需求：这道工序该用哪些料、应耗多少、已耗多少、还差多少
 * 报工弹窗打开时调用，让报工人先看清"这一批要吃掉什么料"
 * @param {number} taskId 生产任务ID
 * @returns data 每行含 unitQty 单位用量 / planQty 应耗 / consumedQty 已耗 / diffQty 差额 / overFlag 是否超耗
 */
export function getMaterialRequire(taskId) {
  return request({
    url: `/pro/consume/materialRequire/${taskId}`,
    method: 'get'
  })
}

/**
 * 工单用料比对：工单BOM 预计使用量（应耗） vs 实际累计消耗（实耗）
 * @param {number} workorderId 生产工单ID
 */
export function getWorkorderMaterialCompare(workorderId) {
  return request({
    url: `/pro/consume/compareByWorkorder/${workorderId}`,
    method: 'get'
  })
}

/**
 * 删除单条误录的消耗记录（逻辑删除）
 * @param {number} recordId 记录ID
 */
export function deleteConsume(recordId) {
  return request({
    url: `/pro/consume/${recordId}`,
    method: 'delete'
  })
}
