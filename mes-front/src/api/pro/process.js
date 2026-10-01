import request from '@/utils/request.js'

/**
 * 工序相关接口
 * 后端 ProProcessController 的 @RequestMapping 是 "/api/pro/process"
 */

/**
 * 分页查询工序列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getProcessByPage(params) {
  return request({
    url: '/pro/process/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询工序详情（后端会把工序内容列表一起带回来）
 * @param {number} id 工序ID
 */
export function getProcessById(id) {
  return request({
    url: `/pro/process/${id}`,
    method: 'get'
  })
}

/**
 * 新增工序
 * @param {Object} data 工序对象（不用传主键）
 */
export function createProcess(data) {
  return request({
    url: '/pro/process',
    method: 'post',
    data
  })
}

/**
 * 修改工序
 * @param {Object} data 工序对象（必须带 processId）
 */
export function updateProcess(data) {
  return request({
    url: '/pro/process',
    method: 'put',
    data
  })
}

/**
 * 删除工序（后端是逻辑删除；被工作站/工艺路线引用时后端会拒绝）
 * @param {number} id 工序ID
 */
export function deleteProcess(id) {
  return request({
    url: `/pro/process/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除工序
 * 后端 deleteBatch 的入参是 @RequestBody Long[] ids，所以这里直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteProcessBatch(ids) {
  return request({
    url: '/pro/process/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 查询全部启用的工序（工艺路线、工作站选工序的下拉数据源）
 */
export function getAllProcessList() {
  return request({
    url: '/pro/process/all',
    method: 'get'
  })
}

/**
 * 根据工序编码查询工序，可用于"工序编码是否已占用"的实时校验
 * @param {string} processCode 工序编码
 */
export function getProcessByCode(processCode) {
  return request({
    url: `/pro/process/byProcessCode/${processCode}`,
    method: 'get'
  })
}
