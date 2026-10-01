import request from '@/utils/request'

/**
 * 条码接口（规则配置 → 生成 → 解析）
 * 【解析走查表】扫码内容在 wm_barcode 精确匹配，命中即知道类型和指向
 */

// ==================== 规则配置 ====================

export function getBarcodeConfigByPage(params) {
  return request({ url: '/wm/barcode/config/page', method: 'get', params })
}

export function getBarcodeConfigById(configId) {
  return request({ url: `/wm/barcode/config/${configId}`, method: 'get' })
}

/** 新增规则（同一类型只能有一条启用中的规则） */
export function createBarcodeConfig(data) {
  return request({ url: '/wm/barcode/config', method: 'post', data })
}

export function updateBarcodeConfig(data) {
  return request({ url: '/wm/barcode/config', method: 'put', data })
}

export function deleteBarcodeConfig(configId) {
  return request({ url: `/wm/barcode/config/${configId}`, method: 'delete' })
}

// ==================== 生成 ====================

/** 给一个业务对象生成条码（barcodeType: ITEM / BATCH / PACKAGE） */
export function generateBarcode(barcodeType, bizId) {
  return request({ url: `/wm/barcode/generate/${barcodeType}/${bizId}`, method: 'post' })
}

/** 批量生成（body 是 bizId 数组；已生成过的自动跳过） */
export function generateBarcodeBatch(barcodeType, bizIds) {
  return request({ url: `/wm/barcode/generate/${barcodeType}`, method: 'post', data: bizIds })
}

// ==================== 查询 / 解析 ====================

export function getBarcodeByPage(params) {
  return request({ url: '/wm/barcode/page', method: 'get', params })
}

export function getBarcodeById(barcodeId) {
  return request({ url: `/wm/barcode/${barcodeId}`, method: 'get' })
}

/** 扫码解析：content = 扫码枪读到的内容 */
export function parseBarcode(content) {
  return request({ url: '/wm/barcode/parse', method: 'get', params: { content } })
}
