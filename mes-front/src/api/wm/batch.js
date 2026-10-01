import request from '@/utils/request.js'

/**
 * 批次相关接口
 * 后端 WmBatchController 的 @RequestMapping 是 "/api/wm/batch"
 *
 * 【批次要带哪些属性由物料说了算】
 * 新增前先调 /md/itemBatchConfig/byItemId/{itemId} 拿到该物料的批次属性配置，
 * 配置里哪个开关是 'Y'，页面上对应的字段就是必填。
 */

/**
 * 分页查询批次列表
 * @param {Object} params 查询条件（itemId / itemCode / batchCode / lotNumber / qualityStatus / 生产日期区间）+ 分页
 */
export function getBatchByPage(params) {
  return request({
    url: '/wm/batch/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询批次详情（后端会带出库存分布 stockList 与库存总量 totalQuantity）
 * @param {number} id 批次ID
 */
export function getBatchById(id) {
  return request({
    url: `/wm/batch/${id}`,
    method: 'get'
  })
}

/**
 * 新增批次（批次编号由后端按「PC+日期+序列」生成，不用传）
 * @param {Object} data 批次对象
 */
export function createBatch(data) {
  return request({
    url: '/wm/batch',
    method: 'post',
    data
  })
}

/**
 * 修改批次（批次编号与所属物料不允许修改）
 * @param {Object} data 批次对象（必须带 batchId）
 */
export function updateBatch(data) {
  return request({
    url: '/wm/batch',
    method: 'put',
    data
  })
}

/**
 * 删除批次（后端是逻辑删除；还有库存时后端会拒绝）
 * @param {number} id 批次ID
 */
export function deleteBatchById(id) {
  return request({
    url: `/wm/batch/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除批次
 * @param {number[]} ids 主键数组
 */
export function deleteBatchBatch(ids) {
  return request({
    url: '/wm/batch/deleteBatch',
    method: 'post',
    data: ids
  })
}

// 注意：查询物料批次属性配置的接口在 E 线（md 模块）里，
// 直接用 @/api/md/itemBatchConfig.js 的 getItemBatchConfigByItemId(itemId)，这里不重复封装。
