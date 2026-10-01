package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysUser;
import com.elevn.mes.sys.options.UpdateOption;
import com.elevn.mes.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 Controller
 *
 * 【关于 @Validated】
 * 这里沿用了 SysDictTypeController 的写法，但要注意：目前 SysUser 实体上一个约束注解都还没加，
 * 所以这个 @Validated 现在是「空转」的 —— 传个空 userName 进来照样能进数据库。
 * 等讲到参数校验那一节，给实体的 userName / password 等字段补上 @NotBlank，这里才会真正生效。
 * 之所以先留着：将来补注解的时候不用回头改 Controller。
 *
 * 【关于密码】
 * 查询接口返回前会把 password 清成 null。没做 userinfo 那种 VO 拆分之前，这是最省事的一层保护。
 * 后续如果 VO 越来越多，再考虑引入 UserVo + BeanUtils.copyProperties。
 *
 */
@RestController
@RequestMapping("/api/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 新增用户
     */
    @PostMapping
    public Result<SysUser> save(@RequestBody @Validated SysUser sysUser){
        // 新增成功返回保存后的对象（主键 id 会被回填进来），失败给一句人话
        return sysUserService.save(sysUser) == 1 ? Result.success(sysUser) : Result.error("保存失败");
    }

    /**
     * 修改用户
     * UpdateOption 分组：给「编辑场景」单独留一组校验规则，
     * 比如将来加 @NotNull(groups = UpdateOption.class) 要求 userId 必传，就只会在编辑时生效，不影响新增。
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) SysUser sysUser){
        return sysUserService.updateById(sysUser) == 1 ? Result.success(sysUser) : Result.error("修改失败");
    }

    /**
     * 根据ID查询用户详情
     * 返回前清掉密码，前端编辑页拿不到密码也就没法回填到表单上
     */
    @GetMapping("/{id}")
    public Result<SysUser> queryById(@PathVariable Long id){
        SysUser sysUser = sysUserService.queryById(id);
        if (sysUser == null){
            return Result.error("用户不存在或已被删除");
        }
        sysUser.setPassword(null);
        return Result.success(sysUser);
    }

    /**
     * 根据登录账号查询用户
     * 跟 /{id} 分开写，因为一个是 Long 一个是 String，
     * 如果两个都用 /{xxx} 会因为路径一模一样产生歧义。
     * 这个接口给登录、以及"注册时实时校验账号是否已被占用"用。
     */
    @GetMapping("/byName/{userName}")
    public Result<SysUser> queryByUserName(@PathVariable String userName){
        SysUser sysUser = sysUserService.queryByUserName(userName);
        if (sysUser == null){
            return Result.error("账号不存在：" + userName);
        }
        sysUser.setPassword(null);
        return Result.success(sysUser);
    }

    /**
     * 分页 + 多条件查询用户列表
     * @param sysUser 查询条件，userName / nickName 走模糊查询，deptId / status 走精确匹配（详见 SysUserMapper.xml）
     */
    @GetMapping("/page")
    public Result<PageInfo<SysUser>> page(SysUser sysUser,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<SysUser> pageInfo = sysUserService.page(pageNum, pageSize, sysUser);
        // 列表同样不含密码 —— 一个列表返回几百条明文密码是典型的安全事故
        pageInfo.getList().forEach(user -> user.setPassword(null));
        return Result.success(pageInfo);
    }

    /**
     * 删除用户（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(sysUserService.deleteById(id));
    }
}
