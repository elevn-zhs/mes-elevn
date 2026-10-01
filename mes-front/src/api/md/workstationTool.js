import request from '@/utils/request.js'

/**
 * 工作站工装夹具相关接口
 * 后端 MdWorkstationToolController 的 @RequestMapping 是 "/api/md/workstationTool"
 */

/**
 * 分页查询工作站工装夹具列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getWorkstationToolByPage(params) {
  return request({
    url: '/md/workstationTool/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询工作站工装夹具详情
 * @param {number} id 工作站工装夹具ID
 */
export function getWorkstationToolById(id) {
  return request({
    url: `/md/workstationTool/${id}`,
    method: 'get'
  })
}

/**
 * 新增工作站工装夹具
 * @param {Object} data 工作站工装夹具对象（不用传主键）
 */
export function createWorkstationTool(data) {
  return request({
    url: '/md/workstationTool',
    method: 'post',
    data
  })
}

/**
 * 修改工作站工装夹具
 * @param {Object} data 工作站工装夹具对象（必须带 recordId）
 */
export function updateWorkstationTool(data) {
  return request({
    url: '/md/workstationTool',
    method: 'put',
    data
  })
}

/**
 * 删除工作站工装夹具（后端是逻辑删除，del_flag 置 1，记录还在库里）
 * @param {number} id 工作站工装夹具ID
 */
export function deleteWorkstationTool(id) {
  return request({
    url: `/md/workstationTool/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除工作站工装夹具
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteWorkstationToolBatch(ids) {
  return request({
    url: '/md/workstationTool/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据工作站ID查询工作站工装夹具列表
 * @param {number} workstationId 工作站ID
 */
export function getWorkstationToolListByWorkstationId(workstationId) {
  return request({
    url: `/md/workstationTool/byWorkstationId/${workstationId}`,
    method: 'get'
  })
}
