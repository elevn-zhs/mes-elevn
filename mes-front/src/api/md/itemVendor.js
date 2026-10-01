import request from '@/utils/request.js'

/**
 * 物料供应商（供货关系）相关接口
 * 后端 MdItemVendorController 的 @RequestMapping 是 "/api/md/itemVendor"
 *
 * 注意：这里的「物料供应商」是 md_item 与 md_vendor 的多对多关系表，
 * 跟供应商档案（/md/vendor，MdVendorController）不是一个东西。
 */

/**
 * 分页查询供货关系列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getItemVendorByPage(params) {
  return request({
    url: '/md/itemVendor/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询供货关系详情
 * @param {number} id 记录ID
 */
export function getItemVendorById(id) {
  return request({
    url: `/md/itemVendor/${id}`,
    method: 'get'
  })
}

/**
 * 新增供货关系
 * @param {Object} data 对象（不用传主键）
 */
export function createItemVendor(data) {
  return request({
    url: '/md/itemVendor',
    method: 'post',
    data
  })
}

/**
 * 修改供货关系
 * @param {Object} data 对象（必须带 itemVendorId）
 */
export function updateItemVendor(data) {
  return request({
    url: '/md/itemVendor',
    method: 'put',
    data
  })
}

/**
 * 删除供货关系（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 记录ID
 */
export function deleteItemVendor(id) {
  return request({
    url: `/md/itemVendor/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除供货关系
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteItemVendorBatch(ids) {
  return request({
    url: '/md/itemVendor/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据物料ID查询该物料的全部供货关系（物料详情 tab 用）
 * @param {number} itemId 物料ID
 */
export function getItemVendorListByItemId(itemId) {
  return request({
    url: `/md/itemVendor/byItemId/${itemId}`,
    method: 'get'
  })
}
