import request from '@/utils/request.js'

/**
 * 生产工单相关接口
 * 后端 ProWorkorderController 的 @RequestMapping 是 "/api/pro/workorder"
 *
 * 状态流转刻意做成四个独立接口（confirm / finish / cancel），
 * 而不是让前端直接 PUT 一个 status —— 每次流转的前置校验和时间字段都不一样，
 * 独立接口能把业务规则收在后端，前端也少一份"哪些状态能跳到哪些状态"的逻辑。
 */

/**
 * 分页查询生产工单列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getWorkorderByPage(params) {
  return request({
    url: '/pro/workorder/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询工单详情
 * @param {number} id 工单ID
 */
export function getWorkorderById(id) {
  return request({
    url: `/pro/workorder/${id}`,
    method: 'get'
  })
}

/**
 * 新增工单（后端会把状态固定成 PREPARE 待下达）
 * @param {Object} data 工单对象（不用传主键）
 */
export function createWorkorder(data) {
  return request({
    url: '/pro/workorder',
    method: 'post',
    data
  })
}

/**
 * 修改工单（只有待下达状态可改）
 * @param {Object} data 工单对象（必须带 workorderId）
 */
export function updateWorkorder(data) {
  return request({
    url: '/pro/workorder',
    method: 'put',
    data
  })
}

/**
 * 删除工单（逻辑删除，只有待下达可删）
 * @param {number} id 工单ID
 */
export function deleteWorkorder(id) {
  return request({
    url: `/pro/workorder/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除工单
 * 后端入参是 @RequestBody Long[] ids，所以直接把数组放进 data
 * @param {number[]} ids 主键数组
 */
export function deleteWorkorderBatch(ids) {
  return request({
    url: '/pro/workorder/deleteBatch',
    method: 'post',
    data: ids
  })
}

/**
 * 根据工单编码查询，可用于"编码是否已占用"的实时校验
 * @param {string} workorderCode 工单编码
 */
export function getWorkorderByCode(workorderCode) {
  return request({
    url: `/pro/workorder/byWorkorderCode/${workorderCode}`,
    method: 'get'
  })
}

// ==================== 状态流转 ====================

/**
 * 下达工单：PREPARE → CONFIRMED，并按产品BOM展开工单用料
 * @param {number} id 工单ID
 * @returns data 为展开出的用料行数
 */
export function confirmWorkorder(id) {
  return request({
    url: `/pro/workorder/confirm/${id}`,
    method: 'post'
  })
}

/**
 * 完工：CONFIRMED → FINISHED
 * @param {number} id 工单ID
 */
export function finishWorkorder(id) {
  return request({
    url: `/pro/workorder/finish/${id}`,
    method: 'post'
  })
}

/**
 * 取消：PREPARE / CONFIRMED → CANCELED
 * @param {number} id 工单ID
 */
export function cancelWorkorder(id) {
  return request({
    url: `/pro/workorder/cancel/${id}`,
    method: 'post'
  })
}

// ==================== 工单用料 ====================

/**
 * 查询工单的用料明细（详情页 Tab）
 * @param {number} workorderId 工单ID
 */
export function getWorkorderBomList(workorderId) {
  return request({
    url: `/pro/workorder/bomList/${workorderId}`,
    method: 'get'
  })
}

/**
 * 重新展开工单用料（产品BOM调整后手工同步）
 * @param {number} id 工单ID
 */
export function rebuildWorkorderBom(id) {
  return request({
    url: `/pro/workorder/rebuildBom/${id}`,
    method: 'post'
  })
}
