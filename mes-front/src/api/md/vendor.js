import request from '@/utils/request.js'

/**
 * 供应商相关接口
 * 后端 MdVendorController 的 @RequestMapping 是 "/api/md/vendor"
 */

/**
 * 分页查询供应商列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getVendorByPage(params) {
  return request({
    url: '/md/vendor/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询供应商详情
 * @param {number} id 供应商ID
 */
export function getVendorById(id) {
  return request({
    url: `/md/vendor/${id}`,
    method: 'get'
  })
}

/**
 * 新增供应商
 * @param {Object} data 供应商对象（不用传主键）
 */
export function createVendor(data) {
  return request({
    url: '/md/vendor',
    method: 'post',
    data
  })
}

/**
 * 修改供应商
 * @param {Object} data 供应商对象（必须带 vendorId）
 */
export function updateVendor(data) {
  return request({
    url: '/md/vendor',
    method: 'put',
    data
  })
}

/**
 * 删除供应商（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 供应商ID
 */
export function deleteVendor(id) {
  return request({
    url: `/md/vendor/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除供应商
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteVendorBatch(ids) {
  return request({
    url: '/md/vendor/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据供应商编码查询供应商，可用于"供应商编码是否已占用"的实时校验
 * @param {string} vendorCode 供应商编码
 */
export function getVendorByVendorCode(vendorCode) {
  return request({
    url: `/md/vendor/byVendorCode/${vendorCode}`,
    method: 'get'
  })
}
