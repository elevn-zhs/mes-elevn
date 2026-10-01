import request from '@/utils/request.js'

/**
 * 客户相关接口
 * 后端 MdClientController 的 @RequestMapping 是 "/api/md/client"
 */

/**
 * 分页查询客户列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getClientByPage(params) {
  return request({
    url: '/md/client/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询客户详情
 * @param {number} id 客户ID
 */
export function getClientById(id) {
  return request({
    url: `/md/client/${id}`,
    method: 'get'
  })
}

/**
 * 新增客户
 * @param {Object} data 客户对象（不用传主键）
 */
export function createClient(data) {
  return request({
    url: '/md/client',
    method: 'post',
    data
  })
}

/**
 * 修改客户
 * @param {Object} data 客户对象（必须带 clientId）
 */
export function updateClient(data) {
  return request({
    url: '/md/client',
    method: 'put',
    data
  })
}

/**
 * 删除客户（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 客户ID
 */
export function deleteClient(id) {
  return request({
    url: `/md/client/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除客户
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteClientBatch(ids) {
  return request({
    url: '/md/client/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据客户编码查询客户，可用于"客户编码是否已占用"的实时校验
 * @param {string} clientCode 客户编码
 */
export function getClientByClientCode(clientCode) {
  return request({
    url: `/md/client/byClientCode/${clientCode}`,
    method: 'get'
  })
}
