import request from '@/utils/request.js'

/**
 * 库区库位相关接口
 * 后端 WmLocationController 的 @RequestMapping 是 "/api/wm/location"
 *
 * 【口径提醒】一张表两级：
 *   location_type = 'AREA'     库区（父级，parent_id = 0）
 *   location_type = 'LOCATION' 库位（子级，parent_id 指向库区）
 */

/**
 * 分页查询库区库位列表
 * @param {Object} params 查询条件（warehouseId / locationType / locationCode / locationName / enableFlag）+ 分页
 */
export function getLocationByPage(params) {
  return request({
    url: '/wm/location/page',
    method: 'get',
    params
  })
}

/**
 * 查询库区库位树（库区为父、库位为子）
 * 后端返回的库区节点带 children 数组
 * 【注意】不要传 locationType —— 过滤掉库区就没法挂库位了，树会散架
 * @param {Object} params 过滤条件（warehouseId / enableFlag）
 */
export function getLocationTree(params) {
  return request({
    url: '/wm/location/tree',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询详情（库位会额外带出当前存放的物料与批次 stockList）
 * @param {number} id 库区/库位ID
 */
export function getLocationById(id) {
  return request({
    url: `/wm/location/${id}`,
    method: 'get'
  })
}

/**
 * 新增库区 / 库位（同一个接口，靠 locationType 区分）
 * @param {Object} data 对象（新增库位时必须带 parentId）
 */
export function createLocation(data) {
  return request({
    url: '/wm/location',
    method: 'post',
    data
  })
}

/**
 * 修改库区 / 库位（类型、所属库区、所属仓库均不允许修改）
 * @param {Object} data 对象（必须带 locationId）
 */
export function updateLocation(data) {
  return request({
    url: '/wm/location',
    method: 'put',
    data
  })
}

/**
 * 删除库区 / 库位（后端是逻辑删除；有下级库位或仍有库存时会拒绝）
 * @param {number} id 库区/库位ID
 */
export function deleteLocation(id) {
  return request({
    url: `/wm/location/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除库区库位
 * @param {number[]} ids 主键数组
 */
export function deleteLocationBatch(ids) {
  return request({
    url: '/wm/location/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 按类型 + 编码查询（编码是否已占用的实时校验）
 * @param {string} locationType AREA / LOCATION
 * @param {string} locationCode 编码
 */
export function getLocationByCode(locationType, locationCode) {
  return request({
    url: '/wm/location/byCode',
    method: 'get',
    params: { locationType, locationCode }
  })
}
