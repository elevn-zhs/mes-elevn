package com.elevn.mes.pro.options;

/**
 * 新增场景校验分组（pro 模块）
 *
 * 用法：Controller 新增方法上写 @Validated(CreateOption.class)
 * 只标注了 groups = {CreateOption.class, ...} 的约束会在新增时生效；
 * 主键一般只标 UpdateOption，所以新增时不会要求传主键。
 *
 */
public interface CreateOption {
}
