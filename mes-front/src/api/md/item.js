import request from '@/utils/request.js'

/**
 * 物料产品相关接口
 * 后端 MdItemController 的 @RequestMapping 是 "/api/md/item"
 */

/**
 * 分页查询物料产品列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getItemByPage(params) {
  return request({
    url: '/md/item/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询物料产品详情
 * @param {number} id 物料产品ID
 */
export function getItemById(id) {
  return request({
    url: `/md/item/${id}`,
    method: 'get'
  })
}

/**
 * 新增物料产品
 * @param {Object} data 物料产品对象（不用传主键）
 */
export function createItem(data) {
  return request({
    url: '/md/item',
    method: 'post',
    data
  })
}

/**
 * 修改物料产品
 * @param {Object} data 物料产品对象（必须带 itemId）
 */
export function updateItem(data) {
  return request({
    url: '/md/item',
    method: 'put',
    data
  })
}

/**
 * 删除物料产品（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 物料产品ID
 */
export function deleteItem(id) {
  return request({
    url: `/md/item/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除物料产品
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteItemBatch(ids) {
  return request({
    url: '/md/item/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * BOM 选料专用分页查询
 * 后端会自动排除：当前物料自身 + 它的所有上级（祖先）+ 所有下级（子孙）
 * @param {Object} params 查询条件 + excludeItemId（必传）+ pageNum + pageSize
 */
export function getItemPageForBom(params) {
  return request({
    url: '/md/item/pageForBom',
    method: 'get',
    params
  })
}

/**
 * 根据物料编码查询物料产品，可用于"物料编码是否已占用"的实时校验
 * @param {string} itemCode 物料编码
 */
export function getItemByItemCode(itemCode) {
  return request({
    url: `/md/item/byItemCode/${itemCode}`,
    method: 'get'
  })
}
