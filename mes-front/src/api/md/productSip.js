import request from '@/utils/request.js'

/**
 * 产品SIP相关接口
 * 后端 MdProductSipController 的 @RequestMapping 是 "/api/md/productSip"
 */

/**
 * 分页查询产品SIP列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getProductSipByPage(params) {
  return request({
    url: '/md/productSip/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询产品SIP详情
 * @param {number} id 产品SIPID
 */
export function getProductSipById(id) {
  return request({
    url: `/md/productSip/${id}`,
    method: 'get'
  })
}

/**
 * 新增产品SIP
 * @param {Object} data 产品SIP对象（不用传主键）
 */
export function createProductSip(data) {
  return request({
    url: '/md/productSip',
    method: 'post',
    data
  })
}

/**
 * 修改产品SIP
 * @param {Object} data 产品SIP对象（必须带 sipId）
 */
export function updateProductSip(data) {
  return request({
    url: '/md/productSip',
    method: 'put',
    data
  })
}

/**
 * 删除产品SIP（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 产品SIPID
 */
export function deleteProductSip(id) {
  return request({
    url: `/md/productSip/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除产品SIP
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteProductSipBatch(ids) {
  return request({
    url: '/md/productSip/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据物料产品ID查询产品SIP列表
 * @param {number} itemId 物料产品ID
 */
export function getProductSipListByItemId(itemId) {
  return request({
    url: `/md/productSip/byItemId/${itemId}`,
    method: 'get'
  })
}
