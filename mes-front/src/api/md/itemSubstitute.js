import request from '@/utils/request.js'

/**
 * 物料替代品相关接口
 * 后端 MdItemSubstituteController 的 @RequestMapping 是 "/api/md/itemSubstitute"
 */

/**
 * 分页查询替代品列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getItemSubstituteByPage(params) {
  return request({
    url: '/md/itemSubstitute/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询替代品详情
 * @param {number} id 替代品ID
 */
export function getItemSubstituteById(id) {
  return request({
    url: `/md/itemSubstitute/${id}`,
    method: 'get'
  })
}

/**
 * 新增替代品
 * @param {Object} data 替代品对象（不用传主键）
 */
export function createItemSubstitute(data) {
  return request({
    url: '/md/itemSubstitute',
    method: 'post',
    data
  })
}

/**
 * 修改替代品
 * @param {Object} data 替代品对象（必须带 substituteId）
 */
export function updateItemSubstitute(data) {
  return request({
    url: '/md/itemSubstitute',
    method: 'put',
    data
  })
}

/**
 * 删除替代品（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 替代品ID
 */
export function deleteItemSubstitute(id) {
  return request({
    url: `/md/itemSubstitute/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除替代品
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteItemSubstituteBatch(ids) {
  return request({
    url: '/md/itemSubstitute/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据物料ID查询该物料的全部替代关系（物料详情 tab 用）
 * @param {number} itemId 物料ID
 */
export function getItemSubstituteListByItemId(itemId) {
  return request({
    url: `/md/itemSubstitute/byItemId/${itemId}`,
    method: 'get'
  })
}
