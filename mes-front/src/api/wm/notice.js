import request from '@/utils/request.js'

/**
 * 仓储通知单相关接口
 * 后端 WmNoticeController 的 @RequestMapping 是 "/api/wm/notice"
 *
 * 【通知单和出入库单据有什么不一样】
 *   出入库单据（/api/wm/doc）是"已经发生/正在发生的动作"，过账会改库存；
 *   通知单只是【预告】：供应商说下周送货、客户说明天要提货、产线说这个工单要备料。
 *   整个控制器里没有一处会改库存。
 *
 * 【它的三个作用】
 *   ① 提前告诉仓库做准备
 *   ② 触发质量检验：到货 → C 线 IQC，发货 → C 线 OQC
 *   ③ 作为下游单据的来源：备料申请 → 生产领料单
 *
 * 【3 类合一，靠 noticeType 区分】
 *   必须带 noticeType —— 不带会把三类通知全倒出来。
 *   三类必填字段不同：到货→供应商；发货→客户；备料申请→生产工单。
 */

/**
 * 分页查询通知单（params 里必须带 noticeType）
 * @param {Object} params 查询条件（noticeType 必填 / noticeCode / status / partnerName /
 *                        noticeDateStart、noticeDateEnd）+ pageNum + pageSize
 */
export function getNoticeByPage(params) {
  return request({
    url: '/wm/notice/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询通知单详情（后端一次带出 lineList 明细行）
 * @param {number} noticeId 通知单ID
 */
export function getNoticeById(noticeId) {
  return request({
    url: `/wm/notice/${noticeId}`,
    method: 'get'
  })
}

/**
 * 查询通知单的行列表
 * @param {number} noticeId 通知单ID
 */
export function getNoticeLines(noticeId) {
  return request({
    url: `/wm/notice/lines/${noticeId}`,
    method: 'get'
  })
}

/**
 * 新增通知单（含明细行），编号由后端按编码规则生成
 * @param {Object} data 通知单对象（含 lineList）
 */
export function createNotice(data) {
  return request({
    url: '/wm/notice',
    method: 'post',
    data
  })
}

/**
 * 修改通知单（含明细行，整批替换）。只有【待处理】状态能改。
 * @param {Object} data 通知单对象（必须带 noticeId）
 */
export function updateNotice(data) {
  return request({
    url: '/wm/notice',
    method: 'put',
    data
  })
}

/**
 * 【触发检验】通知单唯一的状态推进动作
 *
 * 到货通知 → IQC 来料检验；发货通知 → OQC 出货检验；备料申请不触发检验。
 * 触发后单据变【已触发检验】，就不能再改、不能再删了。
 *
 * 【注意】C 线（质量管理）的检验单接口目前还没就绪，
 * 后端只会把明细行标记为待检并打一条 WARN 日志，不会真的生成检验单。
 *
 * @param {number} noticeId 通知单ID
 */
export function triggerQc(noticeId) {
  return request({
    url: `/wm/notice/triggerQc/${noticeId}`,
    method: 'post'
  })
}

/**
 * 删除通知单（逻辑删除 + 删掉行）。只有【待处理】且没报过检的能删。
 * @param {number} noticeId 通知单ID
 */
export function deleteNotice(noticeId) {
  return request({
    url: `/wm/notice/${noticeId}`,
    method: 'delete'
  })
}

/**
 * 批量删除通知单
 * @param {number[]} noticeIds 主键数组
 */
export function deleteNoticeBatch(noticeIds) {
  return request({
    url: '/wm/notice/deleteBatch',
    method: 'post',
    data: noticeIds
  })
}
