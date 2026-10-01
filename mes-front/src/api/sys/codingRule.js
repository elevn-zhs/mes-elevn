import request from '@/utils/request.js'

/**
 * 根据编码类型自动生成编码
 * @param type
 */
export function autoCode(type){
  return request({
    url:'codingRule/auto?ruleCode=' + type,
    method:"get"
  });
}

export function getCodingRuleByPage(params) {
  return request({
    url: '/codingRule/page',
    method: 'get',
    params
  })
}

export function createCodingRule(data) {
  return request({
    url: '/codingRule',
    method: 'post',
    data
  })
}

export function updateCodingRule( data) {
  return request({
    url: `/codingRule`,
    method: 'put',
    data
  })
}

export function deleteCodingRule(id) {
  return request({
    url: `/codingRule/${id}`,
    method: 'delete'
  })
}

export function getCodingRuleById(id){
  return request({
    url:`/codingRule/${id}`,
    method:"get"
  });
};