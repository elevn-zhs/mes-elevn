import request from '@/utils/request.js'

/**
 * 用户-角色授权相关接口
 * 后端 SysUserRoleController 的 @RequestMapping 是 "/api/userRole"
 *
 * 和 roleMenu 一个套路：中间表，业务含义是「给某个用户分配哪些角色」。
 */

/**
 * 给用户分配角色（全量覆盖）
 *
 * 后端签名是 assignRole(@RequestParam Long userId, @RequestBody Long[] roleIds)，
 * userId 走 URL 查询参数，roleIds 数组走请求体。
 *
 * @param {number} userId 用户ID
 * @param {number[]} roleIds 本次勾选的角色ID数组
 */
export function assignRole(userId, roleIds) {
  return request({
    url: '/userRole/assign',
    method: 'post',
    params: { userId },
    data: roleIds
  })
}

/**
 * 查询某用户已经拥有的角色ID，用于授权弹窗的勾选回显
 * @param {number} userId 用户ID
 */
export function getRoleIdsByUserId(userId) {
  return request({
    url: `/userRole/roleIds/${userId}`,
    method: 'get'
  })
}
