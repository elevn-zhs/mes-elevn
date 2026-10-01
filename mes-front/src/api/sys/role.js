import request from '@/utils/request.js'

export function getRoleByPage(params) {
  return request({
    url: '/role/page',
    method: 'get',
    params
  })
}

export function createRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: `/role`,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}

export function getRoleById(id){
  return request({
    url:`/role/${id}`,
    method:"get"
  });
};


export function getAllRoleList(){
  return request({
    url:"/role/all",
    method:"get"
  });
}