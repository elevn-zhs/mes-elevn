import request from '@/utils/request.js'

/**
 * 工装夹具类型相关接口（选料下拉用）
 * 后端 TmToolTypeController 的 @RequestMapping 是 "/api/tm/toolType"
 * 工装类型的增删改属于工装模块（tm 线）的职责，这里只提供查询
 */

/**
 * 分页查询工装夹具类型列表
 * @param {Object} params 查询条件（toolTypeCode/toolTypeName/codeFlag 等）+ pageNum + pageSize
 */
export function getToolTypeByPage(params) {
  return request({
    url: '/tm/toolType/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询工装夹具类型详情
 * @param {number} id 工装夹具类型ID
 */
export function getToolTypeById(id) {
  return request({
    url: `/tm/toolType/${id}`,
    method: 'get'
  })
}
