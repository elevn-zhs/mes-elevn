import request from '@/utils/request.js'

/**
 * 工序流转卡相关接口
 * 后端 ProCardController 的 @RequestMapping 是 "/api/pro/card"
 *
 * 注意：流转卡没有新增/修改/删除接口 —— 建卡在排产时自动发生，
 * 过站在报工时自动推进，拆卡在撤销排产时自动完成。
 * 流转是系统行为，前端只提供"看"的入口，不给手动干预的口子。
 */

/**
 * 分页查询流转卡列表
 * @param {Object} params 查询条件（cardCode / workorderCode / itemName / status）+ pageNum + pageSize
 */
export function getCardByPage(params) {
  return request({
    url: '/pro/card/page',
    method: 'get',
    params
  })
}

/**
 * 流转卡详情（含全部过站行）
 * @param {number} cardId 流转卡ID
 * @returns data 含 card（卡本体）/ processes（过站行，按序号排序）
 */
export function getCardById(cardId) {
  return request({
    url: `/pro/card/${cardId}`,
    method: 'get'
  })
}

/**
 * 按工单查流转卡（一工单一卡，没有则 data 为 null）
 * @param {number} workorderId 生产工单ID
 */
export function getCardByWorkorderId(workorderId) {
  return request({
    url: `/pro/card/byWorkorder/${workorderId}`,
    method: 'get'
  })
}
