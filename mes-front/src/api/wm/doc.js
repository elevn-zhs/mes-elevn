import request from '@/utils/request.js'

/**
 * 出入库单据相关接口
 * 后端 WmDocController 的 @RequestMapping 是 "/api/wm/doc"
 *
 * 【14 类单据共用这一套接口】
 *   采购入库 / 生产领料 / 销售出库 / 调拨…… 单据本身是同一张表（wm_doc），
 *   靠 docType 区分。所以查询列表时【必须带 docType】，
 *   不带的话会把所有类型的单据都倒出来。
 *
 * 【只有一个接口会真的动库存】
 *   executeDoc(docId) —— 过账。
 *   点它之前请想清楚：货真的就动了，库存会加/减，还会产生流水。
 *   过账后单据不能改也不能删，想反悔只能开一张反方向的单子（红冲）。
 */

/**
 * 分页查询单据（params 里必须带 docType）
 * @param {Object} params 查询条件（docType 必填 / docCode / status / partnerName /
 *                        workorderCode / 业务日期区间 bizDateStart、bizDateEnd）+ 分页
 */
export function getDocByPage(params) {
  return request({
    url: '/wm/doc/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询单据详情（后端一次带出 lineList 明细行、detailList 落位明细）
 * @param {number} docId 单据ID
 */
export function getDocById(docId) {
  return request({
    url: `/wm/doc/${docId}`,
    method: 'get'
  })
}

/**
 * 查询单据的行列表
 * @param {number} docId 单据ID
 */
export function getDocLines(docId) {
  return request({
    url: `/wm/doc/lines/${docId}`,
    method: 'get'
  })
}

/**
 * 查询单据的落位明细（只有过账后才有数据）
 * @param {number} docId 单据ID
 */
export function getDocDetails(docId) {
  return request({
    url: `/wm/doc/details/${docId}`,
    method: 'get'
  })
}

/**
 * 新增单据（含明细行）
 * 单据编号由后端按编码规则生成，ioFlag 由 docType 推导，两者都不用传
 * @param {Object} data 单据对象（含 lineList）
 */
export function createDoc(data) {
  return request({
    url: '/wm/doc',
    method: 'post',
    data
  })
}

/**
 * 修改单据（含明细行，整批替换）。只有【待过账】状态能改。
 * @param {Object} data 单据对象（必须带 docId）
 */
export function updateDoc(data) {
  return request({
    url: '/wm/doc',
    method: 'put',
    data
  })
}

/**
 * 【过账】唯一会改动库存的接口
 *
 * 服务端在一个事务里做四件事：写落位明细 → 改库存 → 记流水 → 领料单回写投料。
 * 库存不足、单据已被别人过账，都会在这里被拒绝并整体回滚，不会出现半截数据。
 *
 * @param {number} docId 单据ID
 */
export function executeDoc(docId) {
  return request({
    url: `/wm/doc/execute/${docId}`,
    method: 'post'
  })
}

/**
 * 取消单据（待过账 → 已取消）。已过账的不能取消，只能开反向单红冲。
 * @param {number} docId 单据ID
 * @param {string} reason 取消原因（必填，后端会留痕）
 */
export function cancelDoc(docId, reason) {
  return request({
    url: `/wm/doc/cancel/${docId}`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 调拨送达确认（已过账 → 已完成），只有调拨单需要
 * @param {number} docId 单据ID
 */
export function finishDoc(docId) {
  return request({
    url: `/wm/doc/finish/${docId}`,
    method: 'post'
  })
}

/**
 * 删除单据（逻辑删除，同时删掉明细行）。只有【待过账】状态能删。
 * @param {number} docId 单据ID
 */
export function deleteDoc(docId) {
  return request({
    url: `/wm/doc/${docId}`,
    method: 'delete'
  })
}

/**
 * 批量删除单据
 * @param {number[]} docIds 主键数组
 */
export function deleteDocBatch(docIds) {
  return request({
    url: '/wm/doc/deleteBatch',
    method: 'post',
    data: docIds
  })
}
