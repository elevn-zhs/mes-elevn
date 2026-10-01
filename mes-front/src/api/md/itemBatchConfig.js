import request from '@/utils/request.js'

/**
 * 物料批次属性配置相关接口
 * 后端 MdItemBatchConfigController 的 @RequestMapping 是 "/api/md/itemBatchConfig"
 */

/**
 * 分页查询物料批次属性配置列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getItemBatchConfigByPage(params) {
  return request({
    url: '/md/itemBatchConfig/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询物料批次属性配置详情
 * @param {number} id 物料批次属性配置ID
 */
export function getItemBatchConfigById(id) {
  return request({
    url: `/md/itemBatchConfig/${id}`,
    method: 'get'
  })
}

/**
 * 新增物料批次属性配置
 * @param {Object} data 物料批次属性配置对象（不用传主键）
 */
export function createItemBatchConfig(data) {
  return request({
    url: '/md/itemBatchConfig',
    method: 'post',
    data
  })
}

/**
 * 修改物料批次属性配置
 * @param {Object} data 物料批次属性配置对象（必须带 configId）
 */
export function updateItemBatchConfig(data) {
  return request({
    url: '/md/itemBatchConfig',
    method: 'put',
    data
  })
}

/**
 * 删除物料批次属性配置（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 物料批次属性配置ID
 */
export function deleteItemBatchConfig(id) {
  return request({
    url: `/md/itemBatchConfig/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除物料批次属性配置
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteItemBatchConfigBatch(ids) {
  return request({
    url: '/md/itemBatchConfig/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据物料产品ID查询物料批次属性配置，可用于"物料产品ID是否已占用"的实时校验
 * @param {string} itemId 物料产品ID
 */
export function getItemBatchConfigByItemId(itemId) {
  return request({
    url: `/md/itemBatchConfig/byItemId/${itemId}`,
    method: 'get'
  })
}
