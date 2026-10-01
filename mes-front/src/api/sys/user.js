import request from '@/utils/request.js'

export function getUserById(id){
  return request({
    url:`/user/${id}`,
    method:"get"
  });
}

export function getUserByPage(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

export function createUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser( data) {
  return request({
    url: `/user`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}