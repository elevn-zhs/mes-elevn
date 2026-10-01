import request from '@/utils/request.js'

/**
 * 生产任务（排产）相关接口
 * 后端 ProTaskController 的 @RequestMapping 是 "/api/pro/task"
 *
 * 排产做成分步接口：
 *   getSchedulePreview() 先拿"要拆几道工序 + 每道能选哪些工作站"
 *   schedule()           用户选完工作站再提交
 * 这样界面上能先给出决策依据，而不是让用户盲填工作站。
 */

/**
 * 分页查询生产任务列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getTaskByPage(params) {
  return request({
    url: '/pro/task/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询任务详情
 * @param {number} id 任务ID
 */
export function getTaskById(id) {
  return request({
    url: `/pro/task/${id}`,
    method: 'get'
  })
}

/**
 * 按工单ID查询任务列表（工单详情页 Tab）
 * @param {number} workorderId 工单ID
 */
export function getTaskListByWorkorder(workorderId) {
  return request({
    url: `/pro/task/listByWorkorder/${workorderId}`,
    method: 'get'
  })
}

/**
 * 条件查询任务列表（不计分页，甘特图数据源）
 *
 * 甘特图必须一次拿到一个时间区间内的全部任务，
 * 分页会把同一张工单的工序拆到不同页，画出来就断了。
 *
 * @param {Object} params 查询条件（workorderCode / processId / workstationId / status / startTime / endTime）
 */
export function getTaskList(params) {
  return request({
    url: '/pro/task/list',
    method: 'get',
    params
  })
}

// ==================== 排产 ====================

/**
 * 排产预览：取该工单要拆的工序、各工序工时、每道工序可选的启用工作站
 * @param {number} workorderId 工单ID
 * @returns data 含 workorder / routeCode / routeName / quantityScheduled /
 *               quantityRemain / processList（每项带 workstations 候选列表）
 */
export function getSchedulePreview(workorderId) {
  return request({
    url: `/pro/task/preview/${workorderId}`,
    method: 'get'
  })
}

/**
 * 执行排产
 * @param {Object} data 排产请求
 *   { workorderId, quantity, planStartTime, items: [{ processId, workstationId }] }
 * @returns data 为拆出的任务条数
 */
export function scheduleTask(data) {
  return request({
    url: '/pro/task/schedule',
    method: 'post',
    data
  })
}

/**
 * 撤销排产：物理删除该工单全部任务，并把工单已排产数量归零
 * @param {number} workorderId 工单ID
 * @returns data 为删除的任务条数
 */
export function cancelSchedule(workorderId) {
  return request({
    url: `/pro/task/cancelSchedule/${workorderId}`,
    method: 'post'
  })
}

// ==================== 人工微调 ====================

/**
 * 修改任务（换工作站 / 改计划时间 / 改备注）
 * @param {Object} data 任务对象（必须带 taskId）
 */
export function updateTask(data) {
  return request({
    url: '/pro/task',
    method: 'put',
    data
  })
}

/**
 * 删除单条任务（已报工的不允许删）
 * @param {number} id 任务ID
 */
export function deleteTask(id) {
  return request({
    url: `/pro/task/${id}`,
    method: 'delete'
  })
}
