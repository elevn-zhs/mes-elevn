import request from '@/utils/request.js'

/**
 * 工艺路线相关接口
 * 后端 ProRouteController 的 @RequestMapping 是 "/api/pro/route"
 */

/**
 * 分页查询工艺路线列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getRouteByPage(params) {
  return request({
    url: '/pro/route/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询工艺路线详情
 * @param {number} id 路线ID
 */
export function getRouteById(id) {
  return request({
    url: `/pro/route/${id}`,
    method: 'get'
  })
}

/**
 * 新增工艺路线
 * @param {Object} data 路线对象（不用传主键）
 */
export function createRoute(data) {
  return request({
    url: '/pro/route',
    method: 'post',
    data
  })
}

/**
 * 修改工艺路线
 * @param {Object} data 路线对象（必须带 routeId）
 */
export function updateRoute(data) {
  return request({
    url: '/pro/route',
    method: 'put',
    data
  })
}

/**
 * 删除工艺路线（后端是逻辑删除；被产品制程引用时后端会拒绝）
 * @param {number} id 路线ID
 */
export function deleteRoute(id) {
  return request({
    url: `/pro/route/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除工艺路线
 * @param {number[]} ids 主键数组
 */
export function deleteRouteBatch(ids) {
  return request({
    url: '/pro/route/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 查询全部启用的路线（产品制程挂路线的下拉数据源）
 */
export function getAllRouteList() {
  return request({
    url: '/pro/route/all',
    method: 'get'
  })
}

/**
 * 根据路线编码查询路线，可用于"路线编码是否已占用"的实时校验
 * @param {string} routeCode 路线编码
 */
export function getRouteByCode(routeCode) {
  return request({
    url: `/pro/route/byRouteCode/${routeCode}`,
    method: 'get'
  })
}
