import request from '@/utils/request.js'

/**
 * 工作站设备资源相关接口
 * 后端 MdWorkstationMachineController 的 @RequestMapping 是 "/api/md/workstationMachine"
 */

/**
 * 分页查询工作站设备资源列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getWorkstationMachineByPage(params) {
  return request({
    url: '/md/workstationMachine/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询工作站设备资源详情
 * @param {number} id 工作站设备资源ID
 */
export function getWorkstationMachineById(id) {
  return request({
    url: `/md/workstationMachine/${id}`,
    method: 'get'
  })
}

/**
 * 新增工作站设备资源
 * @param {Object} data 工作站设备资源对象（不用传主键）
 */
export function createWorkstationMachine(data) {
  return request({
    url: '/md/workstationMachine',
    method: 'post',
    data
  })
}

/**
 * 修改工作站设备资源
 * @param {Object} data 工作站设备资源对象（必须带 recordId）
 */
export function updateWorkstationMachine(data) {
  return request({
    url: '/md/workstationMachine',
    method: 'put',
    data
  })
}

/**
 * 删除工作站设备资源（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 工作站设备资源ID
 */
export function deleteWorkstationMachine(id) {
  return request({
    url: `/md/workstationMachine/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除工作站设备资源
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteWorkstationMachineBatch(ids) {
  return request({
    url: '/md/workstationMachine/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据工作站ID查询工作站设备资源列表
 * @param {number} workstationId 工作站ID
 */
export function getWorkstationMachineListByWorkstationId(workstationId) {
  return request({
    url: `/md/workstationMachine/byWorkstationId/${workstationId}`,
    method: 'get'
  })
}
