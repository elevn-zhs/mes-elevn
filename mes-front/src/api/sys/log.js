import request from '@/utils/request.js'

/**
 * 操作日志相关接口
 * 后端 SysLogController 的 @RequestMapping 是 "/api/log"
 *
 * 注意：后端刻意没有提供新增和修改接口 —— 日志是系统自动记的，
 * 不允许人工编造或者事后篡改，从接口层面就把这条路堵死。
 * 所以这里也只有查询和删除（清理过期日志）两个方法。
 */

/**
 * 分页查询日志列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getLogByPage(params) {
  return request({
    url: '/log/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询日志详情（点「详情」看完整请求参数和返回结果）
 * @param {number} id 日志ID
 */
export function getLogById(id) {
  return request({
    url: `/log/${id}`,
    method: 'get'
  })
}

/**
 * 删除日志
 * @param {number} id 日志ID
 */
export function deleteLog(id) {
  return request({
    url: `/log/${id}`,
    method: 'delete'
  })
}
