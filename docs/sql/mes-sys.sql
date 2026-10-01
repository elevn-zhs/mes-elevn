-- =============================================================
-- MES-SYS 系统管理模块 数据库脚本
-- =============================================================
-- 一、表清单（10 张）
--   1. sys_dept        部门表（树结构）
--   2. sys_post        岗位表
--   3. sys_user        用户表
--   4. sys_role        角色表
--   5. sys_menu        权限表（目录 / 菜单 / 按钮 / 接口）
--   6. sys_role_menu   角色-权限关系表
--   7. sys_user_role   用户-角色关系表
--   8. sys_dict_type   字典表（字典类型）
--   9. sys_dict_data   字典条目表（字典数据）
--  10. sys_log         日志表（操作 / 登录 / 异常 / 接口）
--
-- =============================================================


-- ----------------------------
-- 1、部门表（树结构）
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id                     bigint(20)      not null auto_increment    comment '部门ID',
  parent_id                   bigint(20)      default 0                   comment '父部门ID（0=顶级）',
  ancestors                   varchar(500)    default ''                  comment '祖级路径（如 0,100,101）',
  dept_code                   varchar(64)     default ''                  comment '部门编码',
  dept_name                   varchar(128)    default ''                  comment '部门名称',
  order_num                   int(11)         default 0                   comment '显示顺序',
  leader                      varchar(64)     default ''                  comment '负责人',
  phone                       varchar(20)     default ''                  comment '联系电话',
  email                       varchar(64)     default ''                  comment '邮箱',
  status                      char(1)         default '0'                 comment '部门状态（0正常 1停用）',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (dept_id),
  key idx_dept_parent (parent_id),
  key idx_dept_code (dept_code)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '部门表';



-- ----------------------------
-- 2、岗位表
-- ----------------------------
drop table if exists sys_post;
create table sys_post (
  post_id                     bigint(20)      not null auto_increment    comment '岗位ID',
  post_code                   varchar(64)     not null                    comment '岗位编码',
  post_name                   varchar(64)     not null                    comment '岗位名称',
  post_sort                   int(11)         default 0                   comment '显示顺序',
  status                      char(1)         default '0'                 comment '岗位状态（0正常 1停用）',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (post_id),
  key idx_post_code (post_code)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '岗位表';



-- ----------------------------
-- 3、用户表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id                     bigint(20)      not null auto_increment    comment '用户ID',
  dept_id                     bigint(20)      default 0                   comment '所属部门ID',
  post_id                     bigint(20)      default 0                   comment '所属岗位ID（一人一主岗，如需多岗位另建中间表）',
  user_name                   varchar(64)     not null                    comment '登录账号',
  nick_name                   varchar(64)     default ''                  comment '用户昵称',
  user_type                   varchar(20)     default '01'                comment '用户类型（00系统用户 01普通用户）',
  email                       varchar(64)     default ''                  comment '邮箱',
  phonenumber                 varchar(20)     default ''                  comment '手机号码',
  sex                         char(1)         default '2'                 comment '用户性别（0男 1女 2未知）',
  avatar                      varchar(255)    default ''                  comment '头像地址',
  password                    varchar(128)    default ''                  comment '密码（BCrypt 加密，60 位）',
  status                      char(1)         default '0'                 comment '账号状态（0正常 1停用）',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  login_ip                    varchar(128)    default ''                  comment '最后登录IP',
  login_date                  datetime                                    comment '最后登录时间',
  pwd_update_date             datetime                                    comment '密码最后更新时间（用于强制改密策略）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (user_id),
  unique key uk_user_name (user_name),
  key idx_user_dept (dept_id),
  key idx_user_post (post_id),
  key idx_user_status (status),
  key idx_user_del (del_flag)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '用户表';



-- ----------------------------
-- 4、角色表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id                     bigint(20)      not null auto_increment    comment '角色ID',
  role_name                   varchar(64)     not null                    comment '角色名称',
  role_key                    varchar(100)    not null                    comment '角色权限字符（如 admin / wm）',
  role_sort                   int(11)         default 0                   comment '显示顺序',
  data_scope                  char(1)         default '1'                 comment '数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）',
  status                      char(1)         default '0'                 comment '角色状态（0正常 1停用）',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (role_id),
  unique key uk_role_key (role_key),
  key idx_role_status (status),
  key idx_role_del (del_flag)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '角色表';



-- ----------------------------
-- 5、权限表（菜单 / 按钮 / 接口）
--    menu_type：M=目录  C=菜单  F=按钮  A=接口
--    接口级权限说明：menu_type='A' 时填写 api_url + api_method，
--    由后端拦截器在请求进入时校验，避免"绕过页面直接调接口"。
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id                     bigint(20)      not null auto_increment    comment '权限ID',
  menu_name                   varchar(64)     not null                    comment '权限名称',
  parent_id                   bigint(20)      default 0                   comment '父级ID（0=顶级）',
  order_num                   int(11)         default 0                   comment '显示顺序',
  path                        varchar(200)    default ''                  comment '路由地址',
  component                   varchar(255)    default ''                  comment '前端组件路径',
  query                       varchar(255)    default ''                  comment '路由参数',
  is_frame                    char(1)         default '1'                 comment '是否外链（0是 1否）',
  is_cache                    char(1)         default '0'                 comment '是否缓存（0缓存 1不缓存）',
  menu_type                   char(1)         default 'C'                 comment '权限类型（M目录 C菜单 F按钮 A接口）',
  visible                     char(1)         default '0'                 comment '显示状态（0显示 1隐藏）',
  status                      char(1)         default '0'                 comment '权限状态（0正常 1停用）',
  perms                       varchar(128)    default ''                  comment '权限标识（如 sys:user:list）',
  icon                        varchar(128)    default '#'                 comment '菜单图标',
  api_url                     varchar(255)    default ''                  comment '接口地址（menu_type=A 时填写，如 /system/user/**）',
  api_method                  varchar(10)     default ''                  comment '接口方法（GET/POST/PUT/DELETE/*）',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (menu_id),
  key idx_menu_parent (parent_id),
  key idx_menu_perms (perms),
  key idx_menu_type (menu_type),
  key idx_menu_del (del_flag)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '权限表（菜单权限）';



-- ----------------------------
-- 6、角色-权限关系表
--    注意：不建 (role_id, menu_id) 唯一索引，
--    否则"取消授权后再授权"会因逻辑删除行仍在而冲突。
--    业务层做法：先查是否存在同组合的软删记录，有则 update del_flag='0'，无则 insert。
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  id                          bigint(20)      not null auto_increment    comment '主键ID',
  role_id                     bigint(20)      not null                    comment '角色ID',
  menu_id                     bigint(20)      not null                    comment '权限ID',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者（复用通用实体时不会缺列）',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (id),
  key idx_rm_role (role_id),
  key idx_rm_menu (menu_id)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '角色与权限关联表';



-- ----------------------------
-- 7、用户-角色关系表
--    同角色-权限表，不建业务唯一索引，支持"取消后再授予"。
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  id                          bigint(20)      not null auto_increment    comment '主键ID',
  user_id                     bigint(20)      not null                    comment '用户ID',
  role_id                     bigint(20)      not null                    comment '角色ID',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者（复用通用实体时不会缺列）',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (id),
  key idx_ur_user (user_id),
  key idx_ur_role (role_id)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '用户与角色关联表';



-- ----------------------------
-- 8、字典表（字典类型）
--    dict_type 是字典的"英文名"，也是前后端引用的 key，
--    例如 sys_user_sex 下挂着 男/女/未知 三个条目。
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type (
  dict_id                     bigint(20)      not null auto_increment    comment '字典主键',
  dict_name                   varchar(128)    default ''                  comment '字典名称',
  dict_type                   varchar(128)    default ''                  comment '字典类型（英文key）',
  dict_sort                   int(11)         default 0                   comment '显示顺序',
  status                      char(1)         default '0'                 comment '状态（0正常 1停用）',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (dict_id),
  unique key uk_dict_type (dict_type),
  key idx_dict_del (del_flag)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '字典类型表';



-- ----------------------------
-- 9、字典条目表（字典数据）
--    dict_value 存实际存库的值（如 '0'），dict_label 存页面展示的值（如 '正常'）
--    list_class 用于前端回显样式：primary / success / info / warning / danger
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data (
  dict_code                   bigint(20)      not null auto_increment    comment '字典条目主键',
  dict_type                   varchar(128)    default ''                  comment '所属字典类型',
  dict_label                  varchar(128)    default ''                  comment '字典标签（显示值）',
  dict_value                  varchar(128)    default ''                  comment '字典键值（存储值）',
  dict_sort                   int(11)         default 0                   comment '显示顺序',
  is_default                  char(1)         default 'N'                 comment '是否默认（Y是 N否）',
  list_class                  varchar(64)     default ''                  comment '回显样式（primary/success/info/warning/danger）',
  css_class                   varchar(128)    default ''                  comment '自定义样式类',
  status                      char(1)         default '0'                 comment '状态（0正常 1停用）',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (dict_code),
  key idx_dd_type (dict_type),
  key idx_dd_status (status),
  key idx_dd_del (del_flag)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '字典数据表';



-- ----------------------------
-- 10、日志表
--    log_type：1=操作日志  2=登录日志  3=异常日志  4=接口调用日志
--    一张表承载四类，靠 log_type 区分，避免为登录再开一张几乎一样的表。
--    大字段（参数 / 返回结果 / 异常信息）用 text，不计入行长度上限。
-- ----------------------------
drop table if exists sys_log;
create table sys_log (
  log_id                      bigint(20)      not null auto_increment    comment '日志主键',
  log_type                    char(1)         default '1'                 comment '日志类型（1操作 2登录 3异常 4接口）',
  title                       varchar(128)    default ''                  comment '操作模块/日志标题',
  business_type               char(1)         default '0'                 comment '业务类型（0其他 1新增 2修改 3删除 4导出 5导入 6授权 7强退）',
  oper_name                   varchar(64)     default ''                  comment '操作人员账号',
  oper_nick                   varchar(64)     default ''                  comment '操作人员姓名',
  dept_name                   varchar(128)    default ''                  comment '所属部门（快照，部门改名不影响历史）',
  method                      varchar(255)    default ''                  comment '请求方法（类.方法()）',
  request_method              varchar(10)     default ''                  comment 'HTTP 请求方式',
  oper_url                    varchar(255)    default ''                  comment '请求地址',
  oper_ip                     varchar(128)    default ''                  comment '操作IP',
  oper_location               varchar(255)    default ''                  comment '操作地点',
  oper_param                  text                                        comment '请求参数',
  json_result                 text                                        comment '返回结果',
  status                      char(1)         default '0'                 comment '操作状态（0正常 1异常）',
  error_msg                   text                                        comment '异常信息',
  operator_type               char(1)         default '1'                 comment '操作类别（1后台用户 2手机端 3其他）',
  user_agent                  varchar(500)    default ''                  comment 'User-Agent',
  browser                     varchar(64)     default ''                  comment '浏览器',
  os                          varchar(64)     default ''                  comment '操作系统',
  cost_time                   bigint(20)      default 0                   comment '耗时（毫秒）',
  oper_time                   datetime                                    comment '操作时间',
  del_flag                    char(1)         default '0'                 comment '删除标志（0存在 1删除）',
  create_by                   varchar(64)     default ''                  comment '创建者',
  create_time                 datetime                                    comment '创建时间',
  update_by                   varchar(64)     default ''                  comment '更新者',
  update_time                 datetime                                    comment '更新时间',
  remark                      varchar(500)    default ''                  comment '备注',
  primary key (log_id),
  key idx_log_type (log_type),
  key idx_log_oper (oper_name),
  key idx_log_time (oper_time),
  key idx_log_status (status),
  key idx_log_del (del_flag)
) engine=innodb auto_increment=100 default charset=utf8mb4 collate=utf8mb4_general_ci comment = '操作日志表';


-- =============================================================
-- 以下为可选初始化数据（跑通登录所需的最小数据集）
-- 不需要可直接删除；已存在的业务数据请勿重复执行
-- =============================================================

-- ----------------------------
-- 初始化：部门（树）
-- ----------------------------
insert into sys_dept (dept_id, parent_id, ancestors, dept_code, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, remark)
values
  (100, 0,   '0',        'ROOT',   '集团总部', 1, 'admin', '13800000000', 'admin@elevn.com', '0', '0', 'admin', now(), ''),
  (101, 100, '0,100',    'TECH',   '技术部',   1, 'admin', '13800000001', 'tech@elevn.com',  '0', '0', 'admin', now(), ''),
  (102, 100, '0,100',    'PROD',   '生产部',   2, 'admin', '13800000002', 'prod@elevn.com',  '0', '0', 'admin', now(), ''),
  (103, 100, '0,100',    'WH',     '仓储部',   3, 'admin', '13800000003', 'wh@elevn.com',    '0', '0', 'admin', now(), ''),
  (104, 100, '0,100',    'QC',     '品质部',   4, 'admin', '13800000004', 'qc@elevn.com',    '0', '0', 'admin', now(), '');

-- ----------------------------
-- 初始化：岗位
-- ----------------------------
insert into sys_post (post_id, post_code, post_name, post_sort, status, del_flag, create_by, create_time, remark)
values
  (100, 'CEO',      '总经理',   1, '0', '0', 'admin', now(), ''),
  (101, 'MANAGER',  '部门经理', 2, '0', '0', 'admin', now(), ''),
  (102, 'STAFF',    '普通员工', 3, '0', '0', 'admin', now(), '');

-- ----------------------------
-- 初始化：角色
--   data_scope：1全部  3本部门  5仅本人
-- ----------------------------
insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark)
values
  (1, '超级管理员', 'admin',  1, '1', '0', '0', 'admin', now(), '拥有系统全部权限'),
  (2, '仓储专员',   'wm',     2, '3', '0', '0', 'admin', now(), '仅可操作本部门仓储数据'),
  (3, '普通用户',   'common', 3, '5', '0', '0', 'admin', now(), '仅可查看本人相关数据');

-- ----------------------------
-- 初始化：用户（密码均为 admin123 的 BCrypt 密文）
-- ----------------------------
insert into sys_user (user_id, dept_id, post_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, remark)
values
  (1, 100, 100, 'admin', '超级管理员', '00', 'admin@elevn.com', '13800000000', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', now(), now(), 'admin', now(), '系统内置管理员'),
  (2, 103, 102, 'wh01',   '仓储小张',   '01', 'wh01@elevn.com',  '13800000011', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '', null, null, 'admin', now(), '示例：仓储部员工');

-- ----------------------------
-- 初始化：用户-角色关系
-- ----------------------------
insert into sys_user_role (id, user_id, role_id, del_flag, create_by, create_time)
values
  (1, 1, 1, '0', 'admin', now()),
  (2, 2, 2, '0', 'admin', now());

-- ----------------------------
-- 初始化：权限（菜单）
-- ----------------------------
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, api_url, api_method, del_flag, create_by, create_time, remark)
values
  (1,   '系统管理',   0,   1, '/system',        null,                  '', '1', '0', 'M', '0', '0', '',                 'system',   '',                   '',     '0', 'admin', now(), '系统管理目录'),
  (100, '用户管理',   1,   1, 'user',           'system/user/index',   '', '1', '0', 'C', '0', '0', 'sys:user:list',    'user',     '',                   '',     '0', 'admin', now(), '用户管理菜单'),
  (1000,'用户查询',   100, 1, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:user:query',   '#',        '',                   '',     '0', 'admin', now(), ''),
  (1001,'用户新增',   100, 2, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:user:add',     '#',        '',                   '',     '0', 'admin', now(), ''),
  (1002,'用户修改',   100, 3, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:user:edit',    '#',        '',                   '',     '0', 'admin', now(), ''),
  (1003,'用户删除',   100, 4, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:user:remove',  '#',        '',                   '',     '0', 'admin', now(), ''),
  (1004,'重置密码',   100, 5, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:user:resetPwd','#',        '',                   '',     '0', 'admin', now(), ''),
  (101, '角色管理',   1,   2, 'role',           'system/role/index',   '', '1', '0', 'C', '0', '0', 'sys:role:list',    'peoples',  '',                   '',     '0', 'admin', now(), '角色管理菜单'),
  (1010,'角色查询',   101, 1, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:role:query',   '#',        '',                   '',     '0', 'admin', now(), ''),
  (1011,'角色新增',   101, 2, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:role:add',     '#',        '',                   '',     '0', 'admin', now(), ''),
  (1012,'角色修改',   101, 3, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:role:edit',    '#',        '',                   '',     '0', 'admin', now(), ''),
  (1013,'角色删除',   101, 4, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:role:remove',  '#',        '',                   '',     '0', 'admin', now(), ''),
  (102, '菜单管理',   1,   3, 'menu',           'system/menu/index',   '', '1', '0', 'C', '0', '0', 'sys:menu:list',    'tree-table','',                  '',     '0', 'admin', now(), '权限管理菜单'),
  (103, '部门管理',   1,   4, 'dept',           'system/dept/index',   '', '1', '0', 'C', '0', '0', 'sys:dept:list',    'tree',     '',                   '',     '0', 'admin', now(), '部门管理菜单'),
  (104, '岗位管理',   1,   5, 'post',           'system/post/index',   '', '1', '0', 'C', '0', '0', 'sys:post:list',    'post',     '',                   '',     '0', 'admin', now(), '岗位管理菜单'),
  (105, '字典管理',   1,   6, 'dict',           'system/dict/index',   '', '1', '0', 'C', '0', '0', 'sys:dict:list',    'dict',     '',                   '',     '0', 'admin', now(), '字典管理菜单'),
  (106, '日志管理',   1,   7, 'log',            'system/log/index',    '', '1', '0', 'C', '0', '0', 'sys:log:list',     'log',      '',                   '',     '0', 'admin', now(), '日志管理菜单'),
  (1060,'日志删除',   106, 1, '',               null,                  '', '1', '0', 'F', '0', '0', 'sys:log:remove',   '#',        '',                   '',     '0', 'admin', now(), ''),
  (2,   '仓储管理',   0,   2, '/wm',            null,                  '', '1', '0', 'M', '0', '0', '',                 'dashboard','',                   '',     '0', 'admin', now(), '业务示例目录'),
  (200, '出入库单据', 2,   1, 'doc',            'wm/doc/index',        '', '1', '0', 'C', '0', '0', 'wm:doc:list',      'table',    '',                   '',     '0', 'admin', now(), '业务示例菜单'),
  (2000,'单据新增',   200, 1, '',               null,                  '', '1', '0', 'F', '0', '0', 'wm:doc:add',       '#',        '',                   '',     '0', 'admin', now(), ''),
  (2001,'单据审核',   200, 2, '',               null,                  '', '1', '0', 'F', '0', '0', 'wm:doc:audit',     '#',        '',                   '',     '0', 'admin', now(), ''),
  (3000,'用户接口',   100, 9, '',               null,                  '', '1', '0', 'A', '1', '0', 'sys:user:api',     '#',        '/system/user/**',     '*',    '0', 'admin', now(), '接口级权限示例');

-- ----------------------------
-- 初始化：角色-权限关系（超级管理员拥有全部）
-- ----------------------------
insert into sys_role_menu (role_id, menu_id, del_flag, create_by, create_time)
select 1, menu_id, '0', 'admin', now() from sys_menu;

insert into sys_role_menu (role_id, menu_id, del_flag, create_by, create_time)
select 2, menu_id, '0', 'admin', now() from sys_menu where menu_id in (2, 200, 2000);

-- ----------------------------
-- 初始化：字典类型
-- ----------------------------
insert into sys_dict_type (dict_id, dict_name, dict_type, dict_sort, status, del_flag, create_by, create_time, remark)
values
  (1,  '用户性别',   'sys_user_sex',       1, '0', '0', 'admin', now(), '用户性别列表'),
  (2,  '菜单状态',   'sys_normal_disable', 2, '0', '0', 'admin', now(), '菜单状态列表'),
  (3,  '系统开关',   'sys_yes_no',         3, '0', '0', 'admin', now(), '系统是否列表'),
  (4,  '显示状态',   'sys_show_hide',      4, '0', '0', 'admin', now(), '显示状态列表'),
  (5,  '用户类型',   'sys_user_type',      5, '0', '0', 'admin', now(), '用户类型列表'),
  (6,  '数据范围',   'sys_data_scope',     6, '0', '0', 'admin', now(), '角色数据范围列表'),
  (7,  '权限类型',   'sys_menu_type',      7, '0', '0', 'admin', now(), '权限类型列表'),
  (8,  '日志类型',   'sys_log_type',       8, '0', '0', 'admin', now(), '日志类型列表'),
  (9,  '业务操作',   'sys_oper_type',      9, '0', '0', 'admin', now(), '业务操作类型列表');

-- ----------------------------
-- 初始化：字典条目
-- ----------------------------
insert into sys_dict_data (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, status, del_flag, create_by, create_time, remark)
values
  ('sys_user_sex',       '男',     '0', 1, 'Y', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_user_sex',       '女',     '1', 2, 'N', 'danger',  '0', '0', 'admin', now(), ''),
  ('sys_user_sex',       '未知',   '2', 3, 'N', 'info',    '0', '0', 'admin', now(), ''),

  ('sys_normal_disable', '正常',   '0', 1, 'Y', 'success', '0', '0', 'admin', now(), ''),
  ('sys_normal_disable', '停用',   '1', 2, 'N', 'danger',  '0', '0', 'admin', now(), ''),

  ('sys_yes_no',         '是',     'Y', 1, 'N', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_yes_no',         '否',     'N', 2, 'Y', 'danger',  '0', '0', 'admin', now(), ''),

  ('sys_show_hide',      '显示',   '0', 1, 'Y', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_show_hide',      '隐藏',   '1', 2, 'N', 'danger',  '0', '0', 'admin', now(), ''),

  ('sys_user_type',      '系统用户','00',1, 'N', 'danger',  '0', '0', 'admin', now(), ''),
  ('sys_user_type',      '普通用户','01',2, 'Y', 'primary', '0', '0', 'admin', now(), ''),

  ('sys_data_scope',     '全部数据权限',     '1', 1, 'Y', 'danger',  '0', '0', 'admin', now(), ''),
  ('sys_data_scope',     '自定义数据权限',   '2', 2, 'N', 'warning', '0', '0', 'admin', now(), ''),
  ('sys_data_scope',     '本部门数据权限',   '3', 3, 'N', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_data_scope',     '本部门及以下',     '4', 4, 'N', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_data_scope',     '仅本人数据权限',   '5', 5, 'N', 'info',    '0', '0', 'admin', now(), ''),

  ('sys_menu_type',      '目录',   'M', 1, 'N', 'info',    '0', '0', 'admin', now(), ''),
  ('sys_menu_type',      '菜单',   'C', 2, 'Y', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_menu_type',      '按钮',   'F', 3, 'N', 'success', '0', '0', 'admin', now(), ''),
  ('sys_menu_type',      '接口',   'A', 4, 'N', 'warning', '0', '0', 'admin', now(), ''),

  ('sys_log_type',       '操作日志', '1', 1, 'Y', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_log_type',       '登录日志', '2', 2, 'N', 'success', '0', '0', 'admin', now(), ''),
  ('sys_log_type',       '异常日志', '3', 3, 'N', 'danger',  '0', '0', 'admin', now(), ''),
  ('sys_log_type',       '接口日志', '4', 4, 'N', 'info',    '0', '0', 'admin', now(), ''),

  ('sys_oper_type',      '其他',   '0', 1, 'N', 'info',    '0', '0', 'admin', now(), ''),
  ('sys_oper_type',      '新增',   '1', 2, 'N', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_oper_type',      '修改',   '2', 3, 'N', 'success', '0', '0', 'admin', now(), ''),
  ('sys_oper_type',      '删除',   '3', 4, 'N', 'danger',  '0', '0', 'admin', now(), ''),
  ('sys_oper_type',      '导出',   '4', 5, 'N', 'warning', '0', '0', 'admin', now(), ''),
  ('sys_oper_type',      '导入',   '5', 6, 'N', 'warning', '0', '0', 'admin', now(), ''),
  ('sys_oper_type',      '授权',   '6', 7, 'N', 'primary', '0', '0', 'admin', now(), ''),
  ('sys_oper_type',      '强退',   '7', 8, 'N', 'danger',  '0', '0', 'admin', now(), '');
