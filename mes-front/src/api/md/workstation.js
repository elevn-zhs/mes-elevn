import request from '@/utils/request.js'

/**
 * 工作站相关接口
 * 后端 MdWorkstationController 的 @RequestMapping 是 "/api/md/workstation"
 */

/**
 * 分页查询工作站列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getWorkstationByPage(params) {
  return request({
    url: '/md/workstation/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询工作站详情
 * @param {number} id 工作站ID
 */
export function getWorkstationById(id) {
  return request({
    url: `/md/workstation/${id}`,
    method: 'get'
  })
}

/**
 * 新增工作站
 * @param {Object} data 工作站对象（不用传主键）
 */
export function createWorkstation(data) {
  return request({
    url: '/md/workstation',
    method: 'post',
    data
  })
}

/**
 * 修改工作站
 * @param {Object} data 工作站对象（必须带 workstationId）
 */
export function updateWorkstation(data) {
  return request({
    url: '/md/workstation',
    method: 'put',
    data
  })
}

/**
 * 删除工作站（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 工作站ID
 */
export function deleteWorkstation(id) {
  return request({
    url: `/md/workstation/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除工作站
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteWorkstationBatch(ids) {
  return request({
    url: '/md/workstation/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据工作站编码查询工作站，可用于"工作站编码是否已占用"的实时校验
 * @param {string} workstationCode 工作站编码
 */
export function getWorkstationByWorkstationCode(workstationCode) {
  return request({
    url: `/md/workstation/byWorkstationCode/${workstationCode}`,
    method: 'get'
  })
}

/**
 * 按工序ID查询可承担该工序的启用工作站（排产选工作站的下拉数据源）
 *
 * 一道工序在车间里可能对应多个工作站（如"焊接"既有手工焊接站也有波峰焊线），
 * 排产时让用户选，所以返回列表。
 *
 * @param {number} processId 工序ID
 */
export function getWorkstationByProcessId(processId) {
  return request({
    url: `/md/workstation/byProcessId/${processId}`,
    method: 'get'
  })
}
