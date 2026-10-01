import request from '@/utils/request.js'

export function getDictDataListByType(dictType){
  return request({
    url:`/dictData/queryByType`,
    method:"get",
    params:{dictType:dictType}
  });
}

export function getDictDataByPage(params) {
  return request({
    url: '/dictData/page',
    method: 'get',
    params
  })
}

export function createDictData(data) {
  return request({
    url: '/dictData',
    method: 'post',
    data
  })
}

export function updateDictData( data) {
  return request({
    url: `/dictData`,
    method: 'put',
    data
  })
}

export function deleteDictData(id) {
  return request({
    url: `/dictData/${id}`,
    method: 'delete'
  })
}

export function getDictDataById(id){
  return request({
    url:`/dictData/${id}`,
    method:"get"
  });
};