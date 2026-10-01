import request from '@/utils/request.js'

/**
 * 工艺路线工序明细相关接口
 * 后端 ProRouteProcessController 的 @RequestMapping 是 "/api/pro/routeProcess"
 *
 * 明细保存走「整批替换」：前端把整张明细表编辑完，一次性提交完整列表，
 * 后端校验整条链通过后旧删新插。没有单独的增/改/删接口。
 */

/**
 * 按路线ID查询全部明细（按 order_num 升序）
 * @param {number} routeId 路线ID
 */
export function getRouteProcessListByRouteId(routeId) {
  return request({
    url: `/pro/routeProcess/byRouteId/${routeId}`,
    method: 'get'
  })
}

/**
 * 整批保存路线工序明细
 * routeId 走查询参数（后端 @RequestParam），完整明细列表走请求体
 * @param {number} routeId 路线ID
 * @param {Object[]} processList 完整明细列表
 */
export function batchSaveRouteProcess(routeId, processList) {
  return request({
    url: '/pro/routeProcess/batchSave',
    method: 'post',
    params: { routeId },
    data: processList
  })
}
