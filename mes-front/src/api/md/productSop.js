import request from '@/utils/request.js'

/**
 * 产品SOP相关接口
 * 后端 MdProductSopController 的 @RequestMapping 是 "/api/md/productSop"
 */

/**
 * 分页查询产品SOP列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getProductSopByPage(params) {
  return request({
    url: '/md/productSop/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询产品SOP详情
 * @param {number} id 产品SOPID
 */
export function getProductSopById(id) {
  return request({
    url: `/md/productSop/${id}`,
    method: 'get'
  })
}

/**
 * 新增产品SOP
 * @param {Object} data 产品SOP对象（不用传主键）
 */
export function createProductSop(data) {
  return request({
    url: '/md/productSop',
    method: 'post',
    data
  })
}

/**
 * 修改产品SOP
 * @param {Object} data 产品SOP对象（必须带 sopId）
 */
export function updateProductSop(data) {
  return request({
    url: '/md/productSop',
    method: 'put',
    data
  })
}

/**
 * 删除产品SOP（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 产品SOPID
 */
export function deleteProductSop(id) {
  return request({
    url: `/md/productSop/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除产品SOP
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteProductSopBatch(ids) {
  return request({
    url: '/md/productSop/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据物料产品ID查询产品SOP列表
 * @param {number} itemId 物料产品ID
 */
export function getProductSopListByItemId(itemId) {
  return request({
    url: `/md/productSop/byItemId/${itemId}`,
    method: 'get'
  })
}
