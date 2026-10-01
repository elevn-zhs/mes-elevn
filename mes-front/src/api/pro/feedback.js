import request from '@/utils/request.js'

/**
 * 生产报工相关接口
 * 后端 ProFeedbackController 的 @RequestMapping 是 "/api/pro/feedback"
 *
 * 报工做成分步接口：
 *   getFeedbackPreview() 先拿"这道工序排了多少 / 已报多少 / 还能报多少"
 *   submitFeedback()     用户填完数量再提交
 * 这样界面上能先给出上限，而不是让用户盲填一个数字再被打回。
 */

/**
 * 分页查询报工列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getFeedbackByPage(params) {
  return request({
    url: '/pro/feedback/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询报工详情
 * @param {number} id 报工记录ID
 */
export function getFeedbackById(id) {
  return request({
    url: `/pro/feedback/${id}`,
    method: 'get'
  })
}

/**
 * 按任务ID查询报工明细（任务行展开看"报过几次工"）
 * @param {number} taskId 生产任务ID
 */
export function getFeedbackListByTask(taskId) {
  return request({
    url: `/pro/feedback/listByTask/${taskId}`,
    method: 'get'
  })
}

/**
 * 条件查询报工列表（不分页）
 * @param {Object} params 查询条件
 */
export function getFeedbackList(params) {
  return request({
    url: '/pro/feedback/list',
    method: 'get',
    params
  })
}

/**
 * 报工预览：取该任务已报工数量、剩余可报数量与历史报工明细
 * @param {number} taskId 生产任务ID
 * @returns data 含 task / quantityScheduled / quantityProduced /
 *               quantityRemain / feedbackList
 */
export function getFeedbackPreview(taskId) {
  return request({
    url: `/pro/feedback/preview/${taskId}`,
    method: 'get'
  })
}

/**
 * 执行报工
 * @param {Object} data 报工请求
 *   { taskId, feedbackType, feedbackChannel, quantityFeedback,
 *     quantityQualified, quantityUnqualified, quantityUncheck, lotNumber, feedbackTime, remark }
 * @returns data 为报工结果摘要（报工编号 / 任务状态与数量 / 工单已生产数量）
 */
export function submitFeedback(data) {
  return request({
    url: '/pro/feedback',
    method: 'post',
    data
  })
}

/**
 * 冲销报工（红冲）—— 报错了怎么撤
 *
 * 后端不会删掉这条报工，而是把它置为 REVERSED 并留痕，同时在一个事务里
 * 回退任务数量、工单产量与状态、流转卡过站、以及这次报工倒冲出来的物料消耗。
 *
 * 会被拒绝的几种情况，前端拿到 message 直接提示即可：
 *   已冲销过 / 下游工序已产出成品 / 任务或工单的累计产量小于要冲回的量
 *
 * @param {number} id 报工记录ID
 * @param {string} reason 冲销原因（必填，要留痕）
 * @returns data 含 reversedQuantity / revertConsumeRows / taskStatus /
 *               taskProduced / workorderProduced / workorderReverted
 */
export function reverseFeedback(id, reason) {
  return request({
    url: `/pro/feedback/${id}/reverse`,
    method: 'post',
    data: { reason }
  })
}

/**
 * 删除报工记录 —— 已封禁
 *
 * 报工提交后必然回写过任务与工单数量、并生成过物料消耗，直接删会留下
 * 对不上账的孤儿数据，所以后端一律拒绝并提示改用「冲销」。
 * 保留这个函数只为兼容旧调用，新代码请用 reverseFeedback。
 *
 * @deprecated 请改用 reverseFeedback
 */
export function deleteFeedback(id) {
  return request({
    url: `/pro/feedback/${id}`,
    method: 'delete'
  })
}
