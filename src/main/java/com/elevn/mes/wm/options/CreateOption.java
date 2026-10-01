package com.elevn.mes.wm.options;

/**
 * 新增场景校验分组（wm 仓储模块）
 *
 * 用法：Controller 新增方法上写 @Validated(CreateOption.class)
 * 只标注了 groups = {CreateOption.class, ...} 的约束会在新增时生效；
 * 主键一般只标 UpdateOption，所以新增时不会要求传主键。
 *
 * 【为什么不复用 pro 模块的 CreateOption】
 * 校验分组是分组"标记"，跨模块共用也能跑，但会让模块之间产生无谓耦合：
 * 改一个模块的分组语义会波及另一个模块。每个模块留一份，边界更清楚。
 *
 */
public interface CreateOption {
}
