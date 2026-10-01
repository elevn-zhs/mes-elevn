import request from '@/utils/request.js'

/**
 * 工序内容相关接口
 * 后端 ProProcessContentController 的 @RequestMapping 是 "/api/pro/processContent"
 *
 * 工序内容是工序的子表，在工序详情页的 Tab 里维护，不分页。
 */

/**
 * 按工序ID查询全部内容（按 order_num 升序）
 * @param {number} processId 工序ID
 */
export function getProcessContentListByProcessId(processId) {
  return request({
    url: `/pro/processContent/byProcessId/${processId}`,
    method: 'get'
  })
}

/**
 * 新增工序内容
 * @param {Object} data 工序内容对象（必须带 processId）
 */
export function createProcessContent(data) {
  return request({
    url: '/pro/processContent',
    method: 'post',
    data
  })
}

/**
 * 修改工序内容
 * @param {Object} data 工序内容对象（必须带 contentId）
 */
export function updateProcessContent(data) {
  return request({
    url: '/pro/processContent',
    method: 'put',
    data
  })
}

/**
 * 删除工序内容
 * @param {number} id 内容ID
 */
export function deleteProcessContent(id) {
  return request({
    url: `/pro/processContent/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除工序内容
 * @param {number[]} ids 主键数组
 */
export function deleteProcessContentBatch(ids) {
  return request({
    url: '/pro/processContent/deleteBatch',
    method: 'post',
    data: ids
  })
}
