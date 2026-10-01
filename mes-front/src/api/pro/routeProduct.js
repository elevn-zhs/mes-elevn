import request from '@/utils/request.js'

/**
 * 产品制程相关接口
 * 后端 ProRouteProductController 的 @RequestMapping 是 "/api/pro/routeProduct"
 *
 * 产品制程在物料详情页的「工艺路线」Tab 里维护，不分页。
 */

/**
 * 按产品ID查询全部制程
 * @param {number} itemId 产品ID
 */
export function getRouteProductListByItemId(itemId) {
  return request({
    url: `/pro/routeProduct/byItemId/${itemId}`,
    method: 'get'
  })
}

/**
 * 新增产品制程（挂接路线）
 * @param {Object} data 制程对象（itemId + routeId + quantity + productionTime + timeUnitType）
 */
export function createRouteProduct(data) {
  return request({
    url: '/pro/routeProduct',
    method: 'post',
    data
  })
}

/**
 * 修改产品制程（只能改数量/用时/备注，路线和产品不能换）
 * @param {Object} data 制程对象（必须带 recordId）
 */
export function updateRouteProduct(data) {
  return request({
    url: '/pro/routeProduct',
    method: 'put',
    data
  })
}

/**
 * 删除产品制程（后端会联动清理该产品该路线的制程BOM）
 * @param {number} id 记录ID
 */
export function deleteRouteProduct(id) {
  return request({
    url: `/pro/routeProduct/${id}`,
    method: 'delete'
  })
}
