import request from '@/utils/request.js'

/**
 * 计量单位相关接口
 * 后端 MdUnitMeasureController 的 @RequestMapping 是 "/api/md/unitMeasure"
 */

/**
 * 分页查询计量单位列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getUnitMeasureByPage(params) {
  return request({
    url: '/md/unitMeasure/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询计量单位详情
 * @param {number} id 计量单位ID
 */
export function getUnitMeasureById(id) {
  return request({
    url: `/md/unitMeasure/${id}`,
    method: 'get'
  })
}

/**
 * 新增计量单位
 * @param {Object} data 计量单位对象（不用传主键）
 */
export function createUnitMeasure(data) {
  return request({
    url: '/md/unitMeasure',
    method: 'post',
    data
  })
}

/**
 * 修改计量单位
 * @param {Object} data 计量单位对象（必须带 measureId）
 */
export function updateUnitMeasure(data) {
  return request({
    url: '/md/unitMeasure',
    method: 'put',
    data
  })
}

/**
 * 删除计量单位（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 计量单位ID
 */
export function deleteUnitMeasure(id) {
  return request({
    url: `/md/unitMeasure/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除计量单位
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteUnitMeasureBatch(ids) {
  return request({
    url: '/md/unitMeasure/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据单位编码查询计量单位，可用于"单位编码是否已占用"的实时校验
 * @param {string} measureCode 单位编码
 */
export function getUnitMeasureByMeasureCode(measureCode) {
  return request({
    url: `/md/unitMeasure/byMeasureCode/${measureCode}`,
    method: 'get'
  })
}
