import request from '@/utils/request.js'

/**
 * 制程物料BOM相关接口
 * 后端 ProRouteProductBomController 的 @RequestMapping 是 "/api/pro/routeProductBom"
 *
 * BOM 行按 (productId, routeId) 维度整批保存：前端整表编辑完一次性提交。
 */

/**
 * 按产品+路线查询全部BOM行
 * @param {number} productId 产品ID
 * @param {number} routeId 路线ID
 */
export function getRouteProductBomList(productId, routeId) {
  return request({
    url: '/pro/routeProductBom/byProductAndRoute',
    method: 'get',
    params: { productId, routeId }
  })
}

/**
 * 整批保存制程BOM
 * productId / routeId 走查询参数，完整BOM列表走请求体
 * @param {number} productId 产品ID
 * @param {number} routeId 路线ID
 * @param {Object[]} bomList 完整BOM列表
 */
export function batchSaveRouteProductBom(productId, routeId, bomList) {
  return request({
    url: '/pro/routeProductBom/batchSave',
    method: 'post',
    params: { productId, routeId },
    data: bomList
  })
}
