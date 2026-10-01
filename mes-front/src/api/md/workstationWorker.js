import request from '@/utils/request.js'

/**
 * 工作站人力资源相关接口
 * 后端 MdWorkstationWorkerController 的 @RequestMapping 是 "/api/md/workstationWorker"
 */

/**
 * 分页查询工作站人力资源列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getWorkstationWorkerByPage(params) {
  return request({
    url: '/md/workstationWorker/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询工作站人力资源详情
 * @param {number} id 工作站人力资源ID
 */
export function getWorkstationWorkerById(id) {
  return request({
    url: `/md/workstationWorker/${id}`,
    method: 'get'
  })
}

/**
 * 新增工作站人力资源
 * @param {Object} data 工作站人力资源对象（不用传主键）
 */
export function createWorkstationWorker(data) {
  return request({
    url: '/md/workstationWorker',
    method: 'post',
    data
  })
}

/**
 * 修改工作站人力资源
 * @param {Object} data 工作站人力资源对象（必须带 recordId）
 */
export function updateWorkstationWorker(data) {
  return request({
    url: '/md/workstationWorker',
    method: 'put',
    data
  })
}

/**
 * 删除工作站人力资源（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 工作站人力资源ID
 */
export function deleteWorkstationWorker(id) {
  return request({
    url: `/md/workstationWorker/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除工作站人力资源
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteWorkstationWorkerBatch(ids) {
  return request({
    url: '/md/workstationWorker/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据工作站ID查询工作站人力资源列表
 * @param {number} workstationId 工作站ID
 */
export function getWorkstationWorkerListByWorkstationId(workstationId) {
  return request({
    url: `/md/workstationWorker/byWorkstationId/${workstationId}`,
    method: 'get'
  })
}
