import request from '@/utils/request.js'

/**
 * 查询所有部门 结果是树结构
 * @returns {*}
 */
export function getDeptTree(){
  return request({
    url:`/dept/all`,
    method:"get"
  })
}

export function getDeptByPage(params) {
  return request({
    url: '/dept/page',
    method: 'get',
    params
  })
}

export function createDept(data) {
  return request({
    url: '/dept',
    method: 'post',
    data
  })
}

export function updateDept(data) {
  return request({
    url: `/dept`,
    method: 'put',
    data
  })
}

export function deleteDept(id) {
  return request({
    url: `/dept/${id}`,
    method: 'delete'
  })
}

export function getDeptById(id){
  return request({
    url:`/dept/${id}`,
    method:"get"
  });
};