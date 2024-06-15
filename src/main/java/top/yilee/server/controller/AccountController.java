package top.yilee.server.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.pojo.AuthGroup;
import top.yilee.server.pojo.AuthPermission;
import top.yilee.server.service.AccountService;
import top.yilee.server.pojo.Account;
import top.yilee.server.util.SecurityUtils;
import top.yilee.server.util.page.PageUtils;
import top.yilee.server.util.R;
import top.yilee.server.vo.AccountVo;
import top.yilee.server.vo.PersonalVo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : Galaxy
 * @time : 2021/11/25 23:46
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现用户操作相关功能接口
 */
@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    AccountService accountService;

    /**
     * 自己更新信息使用
     */
    @PutMapping("personal")
    public R changePersonal(@RequestBody PersonalVo personal) {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        personal.securityCheck();
        boolean isSuccess = accountService.update(
                new UpdateWrapper<Account>()
                        .eq("id", current.getId())
                        .set(personal.getProperty(), personal.getValue())
        );
        return isSuccess ? R.ok() : R.error();
    }

    @GetMapping("groups/{accountId}")
    public R queryGroups(@PathVariable Long accountId) {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        if (accountId == 0) {
            accountId = current.getId();
        } else {
            if (!SecurityUtils.hasAuthority("view_group")) {
                return R.error("权限不足");
            }
        }
        List<AuthGroup> groups = accountService.getGroupsByAccountId(accountId);
        List<Long> longList = groups.stream().map(AuthGroup::getId).collect(Collectors.toList());
        return R.ok().put("data", longList);
    }

    /**
     * 获取用户权限
     *
     * @param accountId 用户id
     * @param isCode    是否返回权限码
     * @return 权限列表
     */
    @GetMapping("permissions/{accountId}")
    public R queryPermissions(@PathVariable Long accountId,
                              @RequestParam(required = false, defaultValue = "false") Boolean isCode) {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        if (accountId == 0) {
            accountId = current.getId();
        } else {
            if (!SecurityUtils.hasAuthority("view_permission")) {
                return R.error("权限不足");
            }
        }
        List<AuthPermission> permissions = accountService.getPermissionsByAccountId(accountId);
        if (isCode) {
            List<String> stringList = permissions.stream().map(AuthPermission::getCodename).collect(Collectors.toList());
            return R.ok().put("data", stringList);
        } else {
            List<Long> longList = permissions.stream().map(AuthPermission::getId).collect(Collectors.toList());
            return R.ok().put("data", longList);
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('view_account')")
    public R queryAccount(@RequestParam Map<String, Object> params) {
        PageUtils page = accountService.queryPage(params);
        return R.ok().put("page", page);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('change_account')")
    public R changeAccount(@RequestBody AccountVo accountVo) {
        if (accountVo.getResetPassword()) {
            accountVo.setPassword(SecurityUtils.bcrypt("12345"));
        }
        accountService.updateAccount(accountVo);
        return R.ok();
    }

    @PutMapping("status")
    @PreAuthorize("hasAuthority('delete_account')")
    public R deleteAccount(@RequestBody List<Long> accountIds) {
        accountService.update(
                new LambdaUpdateWrapper<Account>()
                        .setSql("is_enabled = !is_enabled")
                        .in(Account::getId, accountIds)
        );
        return R.ok();
    }
}
