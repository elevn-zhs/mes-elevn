import request from '@/utils/request.js'

/**
 * 仓库相关接口
 * 后端 WmWarehouseController 的 @RequestMapping 是 "/api/wm/warehouse"
 */

/**
 * 分页查询仓库列表
 * @param {Object} params 查询条件（warehouseCode / warehouseName / enableFlag）+ pageNum + pageSize
 */
export function getWarehouseByPage(params) {
  return request({
    url: '/wm/warehouse/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询仓库详情（后端会带库区数、库位数、库存总量、库存物料种数）
 * @param {number} id 仓库ID
 */
export function getWarehouseById(id) {
  return request({
    url: `/wm/warehouse/${id}`,
    method: 'get'
  })
}

/**
 * 新增仓库
 * @param {Object} data 仓库对象（不用传主键）
 */
export function createWarehouse(data) {
  return request({
    url: '/wm/warehouse',
    method: 'post',
    data
  })
}

/**
 * 修改仓库（仓库编码不允许修改，后端会拒绝）
 * @param {Object} data 仓库对象（必须带 warehouseId）
 */
export function updateWarehouse(data) {
  return request({
    url: '/wm/warehouse',
    method: 'put',
    data
  })
}

/**
 * 删除仓库（后端是逻辑删除；下挂库区库位或还有库存时后端会拒绝）
 * @param {number} id 仓库ID
 */
export function deleteWarehouse(id) {
  return request({
    url: `/wm/warehouse/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除仓库
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteWarehouseBatch(ids) {
  return request({
    url: '/wm/warehouse/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 查询全部启用的仓库（库区库位、单据选仓库的下拉数据源）
 */
export function getAllWarehouseList() {
  return request({
    url: '/wm/warehouse/all',
    method: 'get'
  })
}

/**
 * 根据仓库编码查询，可用于"编码是否已占用"的实时校验
 * @param {string} warehouseCode 仓库编码
 */
export function getWarehouseByCode(warehouseCode) {
  return request({
    url: `/wm/warehouse/byWarehouseCode/${warehouseCode}`,
    method: 'get'
  })
}
