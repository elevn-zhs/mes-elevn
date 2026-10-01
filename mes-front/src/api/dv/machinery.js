import request from '@/utils/request.js'

/**
 * 设备相关接口（选料下拉用）
 * 后端 DvMachineryController 的 @RequestMapping 是 "/api/dv/machinery"
 * 设备的增删改属于设备模块（dv 线）的职责，这里只提供查询
 */

/**
 * 分页查询设备列表
 * @param {Object} params 查询条件（machineryCode/machineryName/workshopId/status 等）+ pageNum + pageSize
 */
export function getMachineryByPage(params) {
  return request({
    url: '/dv/machinery/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询设备详情
 * @param {number} id 设备ID
 */
export function getMachineryById(id) {
  return request({
    url: `/dv/machinery/${id}`,
    method: 'get'
  })
}
