import request from '@/utils/request.js'

/**
 * 产品BOM相关接口
 * 后端 MdProductBomController 的 @RequestMapping 是 "/api/md/bom"
 */

/**
 * 分页查询产品BOM列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getBomByPage(params) {
  return request({
    url: '/md/bom/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询产品BOM详情
 * @param {number} id 产品BOMID
 */
export function getBomById(id) {
  return request({
    url: `/md/bom/${id}`,
    method: 'get'
  })
}

/**
 * 新增产品BOM
 * @param {Object} data 产品BOM对象（不用传主键）
 */
export function createBom(data) {
  return request({
    url: '/md/bom',
    method: 'post',
    data
  })
}

/**
 * 修改产品BOM
 * @param {Object} data 产品BOM对象（必须带 bomId）
 */
export function updateBom(data) {
  return request({
    url: '/md/bom',
    method: 'put',
    data
  })
}

/**
 * 删除产品BOM（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 产品BOMID
 */
export function deleteBom(id) {
  return request({
    url: `/md/bom/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除产品BOM
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteBomBatch(ids) {
  return request({
    url: '/md/bom/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据物料产品ID查询产品BOM列表
 * @param {number} itemId 物料产品ID
 */
export function getBomListByItemId(itemId) {
  return request({
    url: `/md/bom/byItemId/${itemId}`,
    method: 'get'
  })
}
