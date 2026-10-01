import request from '@/utils/request.js'

export function getDictTypeByPage(params) {
  return request({
    url: '/dictType/page',
    method: 'get',
    params
  })
}

/**
 * 新增字典信息
 * @param data
 * @returns {*}
 */
export function createDictType(data) {
  return request({
    url: '/dictType',
    method: 'post',
    data
  })
}

export function updateDictType(data) {
  return request({
    url: `/dictType`,
    method: 'put',
    data
  })
}

export function deleteDictType(id) {
  return request({
    url: `/dictType/${id}`,
    method: 'delete'
  })
}

export function getDictTypeById(id){
  return request({
    url:`/dictType/${id}`,
    method:"get"
  });
};

/**
 * 批量删除操作
 * @param ids
 * @returns {*}
 */
export function deleteBatch(ids){
  return request({
    url:`/dictType/deleteBatch`,
    method:"post",
    data:ids
  });
}

export function getDictTypeByType(type){
  return request({
    url:`/dictType/queryByType`,
    method:"get",
    params:{dictType:type}
  })
}