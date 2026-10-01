import request from '@/utils/request.js'

/**
 * 角色-菜单授权相关接口
 * 后端 SysRoleMenuController 的 @RequestMapping 是 "/api/roleMenu"
 *
 * 这张表是角色和菜单的中间表，业务含义是「给某个角色分配哪些菜单」，
 * 所以页面上不会把它做成一张普通的 CRUD 表格，而是在角色管理页里点「分配权限」弹出菜单树来勾选。
 */

/**
 * 给角色分配菜单（全量覆盖）
 *
 * 后端签名是 assignMenu(@RequestParam Long roleId, @RequestBody Long[] menuIds)，
 * 所以 roleId 挂在 URL 的查询参数上，menuIds 数组放在请求体里。
 * 「全量覆盖」的意思是：先删掉这个角色原来的全部关联，再把本次勾选的重新插一遍。
 *
 * @param {number} roleId 角色ID
 * @param {number[]} menuIds 本次勾选的菜单ID数组，一个都不勾就传空数组（等于清空权限）
 */
export function assignMenu(roleId, menuIds) {
  return request({
    url: '/roleMenu/assign',
    method: 'post',
    params: { roleId },
    data: menuIds
  })
}

/**
 * 查询某角色已经分配的菜单ID，用于授权弹窗的勾选回显
 * @param {number} roleId 角色ID
 */
export function getMenuIdsByRoleId(roleId) {
  return request({
    url: `/roleMenu/menuIds/${roleId}`,
    method: 'get'
  })
}
