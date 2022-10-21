package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.service.*;

import java.util.*;

/**
 * @author : Galaxy
 * @time : 2021/11/25 23:46
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现用户操作相关功能接口
 */
@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    AuthGroupService authGroupService;

    @Autowired
    AuthPermissionService authPermissionService;

    @Autowired
    AuthAccountGroupsService authAccountGroupsService;

    /**
     * 自己更新信息使用
     */
    @PutMapping("personal")
    public Map<String, Object> changePersonal(String property,
                                              String value,
                                              @RequestParam(required = false) String newPassword,
                                              @ApiIgnore @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 1);
        boolean execute = true;
        switch (property) {
            case "password":
                //重新查询密码h
                current = accountService.getById(current.getId());
                if (current.matchPassword(value)) {
                    value = Account.bcryptPassword(newPassword);
                } else {
                    map.put("msg", "密码错误");
                    execute = false;
                }
                break;
            case "email":
            case "mobile":
                break;
            default:
                throw new RuntimeException("所选类型不存在");
        }

        if (execute && accountService.update(
                new UpdateWrapper<Account>()
                        .eq("id", current.getId())
                        .set(property, value)
        )) {
            map.put("code", 0);
            map.put("msg", "修改成功");
        }

        return map;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('view_account')")
    public Map<String, Object> queryAccount(Integer page, Integer limit,
                                            @RequestParam(required = false) Boolean isEnabled,
                                            @RequestParam(required = false, defaultValue = "") String username) {
        Map<String, Object> map = new HashMap<>(5);

        LambdaQueryWrapper<Account> wrapper =
                new LambdaQueryWrapper<Account>()
                        .like(Account::getUsername, username);
        if (isEnabled != null) {
            wrapper = wrapper.eq(Account::getIsEnabled, isEnabled);
        }
        Page<Account> accountPage = accountService.page(new Page<>(page, limit), wrapper);

        List<Account> accountList = accountPage.getRecords();
        map.put("code", 0);
        map.put("msg", "查询成功");
        map.put("count", accountList.size());
        map.put("total", accountPage.getTotal());
        map.put("data", accountList);

        return map;
    }

    @GetMapping("permissions")
    @PreAuthorize("hasAuthority('change_account')")
    public Map<String, Object> queryPermissions(String accountId) {
        Map<String, Object> map = new HashMap<>(3);

        map.put("code", 0);
        map.put("msg", "OK");
        Account account = accountService.getById(accountId);

        List<AuthGroup> groups = null;
        List<AuthPermission> permissions = null;

        if (account != null) {
            if (!account.getIsSuperuser()) {
                groups = account.getAuthGroups();
                permissions = account.getAuthPermissions();
            }
        }

        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        if (groups == null) {
            groups = new ArrayList<>();
        }

        // 已授权的权限
        Set<Object> auths = new HashSet<>();
        // 已拥有的分组
        Set<Object> packets = new HashSet<>();
        // 分组权限对应map
        for (AuthGroup group : groups) {
            packets.add(group.getId());
            List<AuthPermission> permissions0 = group.getAuthPermissions();
            if (permissions0 != null) {
                for (AuthPermission permission : permissions0) {
                    auths.add(permission.getId());
                }
            }
        }
        for (AuthPermission permission : permissions) {
            auths.add(permission.getId());
        }

        Map<String, Object> data = new HashMap<>(2);
        data.put("groupIds", packets);
        data.put("permissionIds", auths);
        map.put("data", data);

        Map<String, Object> total = new HashMap<>(2);
        total.put("groups", authGroupService.list());
        total.put("permissions", authPermissionService.list());
        map.put("total", total);

        return map;
    }

    @PutMapping
    @PreAuthorize("hasAuthority('change_account')")
    public Map<String, Object> changeAccount(Account account,
                                             @RequestParam(required = false) List<Integer> groupIds,
                                             @RequestParam(required = false) List<Integer> permissionIds,
                                             @RequestParam(required = false, defaultValue = "false") Boolean resetPassword) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 1);
        map.put("msg", "修改失败");

        // 删除其他数据
        account.setPassword(null);
        account.clearOtherExcludeBoolean();

        if (resetPassword) {
            account.setPassword("12345");
            account.bcryptPassword();
        }

        if (accountService.updateById(account) &&
                account.saveBatchGroup(groupIds) &&
                account.saveBatchPermission(permissionIds)) {
            map.put("code", 0);
            map.put("msg", "修改成功");
        }

        return map;
    }

    @PutMapping("status")
    @PreAuthorize("hasAuthority('delete_account')")
    public Map<String, Object> deleteAccount(@RequestParam List<Integer> accountIds) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "操作完成");
        map.put("count", accountService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<Account>()
                        .setSql("is_enabled = !is_enabled")
                        .in(Account::getId, accountIds)
        ));
        map.put("total", accountIds.size());
        return map;
    }
}
