import request from '@/utils/request.js'

/**
 * 菜单管理相关接口
 * 后端 SysMenuController 的 @RequestMapping 是 "/api/menu"
 */

/**
 * 分页查询菜单列表（平铺，一般管理页用）
 * @param {Object} params 查询条件 + pageNum + pageSize
 */
export function getMenuByPage(params) {
  return request({
    url: '/menu/page',
    method: 'get',
    params
  })
}

/**
 * 查询菜单树（一次性查全部，后端在内存里组装层级）
 * 列表展示、上级菜单下拉、角色授权树都调这个
 * @param {Object} params 查询条件，传空对象表示查全部
 */
export function getMenuTree(params) {
  return request({
    url: '/menu/tree',
    method: 'get',
    params: params || {}
  })
}

/**
 * 根据主键查询菜单详情
 * @param {number} id 菜单ID
 */
export function getMenuById(id) {
  return request({
    url: `/menu/${id}`,
    method: 'get'
  })
}

/**
 * 新增菜单
 * @param {Object} data 菜单对象
 */
export function createMenu(data) {
  return request({
    url: '/menu',
    method: 'post',
    data
  })
}

/**
 * 修改菜单
 * @param {Object} data 菜单对象（必须带 menuId）
 */
export function updateMenu(data) {
  return request({
    url: '/menu',
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 * @param {number} id 菜单ID
 */
export function deleteMenu(id) {
  return request({
    url: `/menu/${id}`,
    method: 'delete'
  })
}
