import request from '@/utils/request.js'

/**
 * 库存事务（流水账）相关接口
 * 后端 WmTransactionController 的 @RequestMapping 是 "/api/wm/transaction"
 *
 * 【这张表是"账"，库存表是"余额"】
 *   库存表只记当前还剩多少；流水表每次变动都留一条，永远不倒推、不修改。
 *   对账逻辑：某个物料某段时间的流水加起来 == 库存的变化。
 *
 * 【同样全是只读接口】
 *   流水只能由单据过账生成，没有新增/修改/删除。
 *   发现流水不对，说明是单据错了 —— 去查那张单据，而不是改流水。
 *
 * 【两个易错点，页面上要标注清楚】
 *   1) 方向记在 transactionFlag 上（1 入库 / -1 出库），数量永远是正数。
 *      所以显示负数不要用 -quantity，要看 flag。
 *   2) 调拨会生成【一进一出配对的两条】，用 relatedTransactionId 互指。
 *      页面上如果只看到一条，不是数据丢了，是筛选条件把另一条过滤掉了。
 */

/**
 * 分页查询流水（按主键倒序 —— 看的是"最近发生了什么"）
 * @param {Object} params 查询条件（itemId / itemCode / batchId / batchCode / warehouseId /
 *                        sourceDocCode / transactionType / transactionDateStart / transactionDateEnd）
 *                        + pageNum + pageSize
 */
export function getTransactionByPage(params) {
  return request({
    url: '/wm/transaction/page',
    method: 'get',
    params
  })
}

/**
 * 按物料 + 批次查流水（批次追溯）
 *
 * 和列表查询的区别只有排序：这里按时间【正序】，
 * 因为追溯看的是"这批货一路怎么走过来的" —— 什么时候入库、什么时候被谁领走。
 * itemId 必填。
 *
 * @param {Object} params 查询条件（itemId 必填，可带 batchId）
 */
export function getTransactionByItemAndBatch(params) {
  return request({
    url: '/wm/transaction/byItem',
    method: 'get',
    params
  })
}

/**
 * 按事务类型统计（每类单据一共入了多少、出了多少、净变动多少）
 * @param {Object} params 查询条件（warehouseId / 日期区间等）
 */
export function getTransactionStatByType(params) {
  return request({
    url: '/wm/transaction/statByType',
    method: 'get',
    params
  })
}

/**
 * 查看流水详情（含来源单据信息，软引用）
 * @param {number} id 事务ID
 */
export function getTransactionById(id) {
  return request({
    url: `/wm/transaction/${id}`,
    method: 'get'
  })
}
