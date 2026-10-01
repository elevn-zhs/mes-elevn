import request from '@/utils/request.js'

/**
 * 物料产品分类相关接口
 * 后端 MdItemTypeController 的 @RequestMapping 是 "/api/md/itemType"
 */

/**
 * 分页查询物料产品分类列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getItemTypeByPage(params) {
  return request({
    url: '/md/itemType/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询物料产品分类详情
 * @param {number} id 物料产品分类ID
 */
export function getItemTypeById(id) {
  return request({
    url: `/md/itemType/${id}`,
    method: 'get'
  })
}

/**
 * 新增物料产品分类
 * @param {Object} data 物料产品分类对象（不用传主键）
 */
export function createItemType(data) {
  return request({
    url: '/md/itemType',
    method: 'post',
    data
  })
}

/**
 * 修改物料产品分类
 * @param {Object} data 物料产品分类对象（必须带 itemTypeId）
 */
export function updateItemType(data) {
  return request({
    url: '/md/itemType',
    method: 'put',
    data
  })
}

/**
 * 删除物料产品分类（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 物料产品分类ID
 */
export function deleteItemType(id) {
  return request({
    url: `/md/itemType/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除物料产品分类
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteItemTypeBatch(ids) {
  return request({
    url: '/md/itemType/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据分类编码查询物料产品分类，可用于"分类编码是否已占用"的实时校验
 * @param {string} itemTypeCode 分类编码
 */
export function getItemTypeByItemTypeCode(itemTypeCode) {
  return request({
    url: `/md/itemType/byItemTypeCode/${itemTypeCode}`,
    method: 'get'
  })
}

/**
 * 根据ITEM或者PRODUCT查询树结构分类
 * @param type
 * @returns {*}
 */
export function getItemTypeForTreeByType(type){
  return request({
    url:`/md/itemType/tree?type=` + type,
    method:"get"
  });
}

/**
 * 根据parentId加载自类目列表
 * @param parentId
 * @returns {*}
 */
export function getItemTypeByParentId(parentId){
  return request({
    url:`/md/itemType/queryByParentId?parentId=${parentId}`,
    method:"get"
  });
}