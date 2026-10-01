import request from '@/utils/request.js'

/**
 * 车间相关接口
 * 后端 MdWorkshopController 的 @RequestMapping 是 "/api/md/workshop"
 */

/**
 * 分页查询车间列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getWorkshopByPage(params) {
  return request({
    url: '/md/workshop/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询车间详情
 * @param {number} id 车间ID
 */
export function getWorkshopById(id) {
  return request({
    url: `/md/workshop/${id}`,
    method: 'get'
  })
}

/**
 * 新增车间
 * @param {Object} data 车间对象（不用传主键）
 */
export function createWorkshop(data) {
  return request({
    url: '/md/workshop',
    method: 'post',
    data
  })
}

/**
 * 修改车间
 * @param {Object} data 车间对象（必须带 workshopId）
 */
export function updateWorkshop(data) {
  return request({
    url: '/md/workshop',
    method: 'put',
    data
  })
}

/**
 * 删除车间（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 车间ID
 */
export function deleteWorkshop(id) {
  return request({
    url: `/md/workshop/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除车间
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteWorkshopBatch(ids) {
  return request({
    url: '/md/workshop/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据车间编码查询车间，可用于"车间编码是否已占用"的实时校验
 * @param {string} workshopCode 车间编码
 */
export function getWorkshopByWorkshopCode(workshopCode) {
  return request({
    url: `/md/workshop/byWorkshopCode/${workshopCode}`,
    method: 'get'
  })
}
