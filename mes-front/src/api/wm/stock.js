import request from '@/utils/request.js'

/**
 * 库存查询相关接口
 * 后端 WmStockController 的 @RequestMapping 是 "/api/wm/stock"
 *
 * 【这一整块都是只读的】
 *   里面没有新增/修改/删除接口，不是没写完，是故意不写。
 *   库存是"结果"，单据才是"原因" —— 想改库存，去开出入库单据过账。
 *   放开了手改，账和单据对不上，出了问题谁也说不清货是怎么少的。
 *
 * 【三个视图，别混着用】
 *   /query              库存明细：一行 = 一个「物料+批次+仓库+库区+库位+容器」的现存记录
 *   /summary/byItem     按物料汇总：把散在各库位的数量加起来
 *   /summary/byWarehouse 按仓库汇总：每个仓里有几种料、总量多少
 *
 * 汇总视图返回的 quantityOnhand 是【求和结果】，不是某一行的数量，
 * 界面上别拿它去做出库校验，那个只能用明细行的 quantityAvailable。
 */

/**
 * 库存明细查询（分页）
 * @param {Object} params 查询条件（itemId / itemCode / batchId / batchCode / warehouseId /
 *                        areaId / locationId / onlyStock）+ pageNum + pageSize
 *                        onlyStock 传 'Y' 只看还有货的行
 */
export function getStockByPage(params) {
  return request({
    url: '/wm/stock/query',
    method: 'get',
    params
  })
}

/**
 * 库存汇总 - 按物料（不分页，一次全带回来）
 * @param {Object} params 查询条件（itemId / itemCode / warehouseId 等）
 */
export function getStockSummaryByItem(params) {
  return request({
    url: '/wm/stock/summary/byItem',
    method: 'get',
    params
  })
}

/**
 * 库存汇总 - 按仓库
 * 返回的 itemKindCount 是该仓库存了多少种物料
 * @param {Object} params 查询条件
 */
export function getStockSummaryByWarehouse(params) {
  return request({
    url: '/wm/stock/summary/byWarehouse',
    method: 'get',
    params
  })
}

/**
 * 库存预警列表
 *
 * 安全库存（最低 min_stock / 最高 max_stock）取自 E 线的物料档案，
 * 比较的是【按物料汇总后】的数量，不是单行数量：
 * 一个物料散在 3 个库位各 10，逐行看着都不低，加起来只有 30，其实早该补货了。
 *
 * 返回的 warningType：LOW 低于最低库存 / HIGH 高于最高库存 / 空串 正常
 */
export function getStockWarningList() {
  return request({
    url: '/wm/stock/warning',
    method: 'get'
  })
}

/**
 * 查询单条库存记录
 * @param {number} id 库存记录ID
 */
export function getStockById(id) {
  return request({
    url: `/wm/stock/${id}`,
    method: 'get'
  })
}
