import request from '@/utils/request.js'

/**
 * 岗位管理相关接口
 * 后端 SysPostController 的 @RequestMapping 是 "/api/post"
 */

/**
 * 分页查询岗位列表
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getPostByPage(params) {
  return request({
    url: '/post/page',
    method: 'get',
    params
  })
}

/**
 * 根据主键查询岗位详情
 * @param {number} id 岗位ID
 */
export function getPostById(id) {
  return request({
    url: `/post/${id}`,
    method: 'get'
  })
}

/**
 * 新增岗位
 * @param {Object} data 岗位对象
 */
export function createPost(data) {
  return request({
    url: '/post',
    method: 'post',
    data
  })
}

/**
 * 修改岗位
 * @param {Object} data 岗位对象（必须带 postId）
 */
export function updatePost(data) {
  return request({
    url: '/post',
    method: 'put',
    data
  })
}

/**
 * 删除岗位
 * @param {number} id 岗位ID
 */
export function deletePost(id) {
  return request({
    url: `/post/${id}`,
    method: 'delete'
  })
}
