package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.AccountService;
import top.liheji.server.util.SecretUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Time : 2021/10/29 22:19
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : server
 * @Description :
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * 自己更新信息使用
     */
    @PutMapping("personal")
    public Map<String, Object> updateAccountPersonal(Account account,
                                                     @RequestParam(required = false) String key,
                                                     @RequestParam(required = false) String newPassword,
                                                     @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(2);

        //重新查询密码
        current = accountService.getById(current.getId());
        account.setId(current.getId());
        account.setIsEnabled(null);
        account.clearOthers();

        boolean execute = true;
        if (account.notMatchEmail()) {
            map.put("msg", "邮箱格式错误");
            execute = false;
        }

        if (account.getEmail() != null && !SecretUtils.check(key)) {
            map.put("msg", "校验码错误");
            execute = false;
        }

        if (account.notMatchMobile()) {
            map.put("msg", "手机号格式错误");
            execute = false;
        }

        if (account.getPassword() != null) {
            if (account.matchPassword(current.getPassword())) {
                account.setPassword(newPassword);
                account.bcryptPassword();
            } else {
                map.put("msg", "密码错误");
                execute = false;
            }
        }

        if (execute && accountService.updateById(account)) {
            map.put("code", 0);
            map.put("msg", "修改成功");
        }

        return map;
    }


    @GetMapping
    public Map<String, Object> queryAccount(Integer page, Integer limit,
                                            @RequestParam(required = false) String username,
                                            @RequestParam(required = false) Boolean isEnabled,
                                            @RequestAttribute("account") Account current) {

        Map<String, Object> map = new HashMap<>(5);

        QueryWrapper<Account> wrapper = new QueryWrapper<Account>()
                .like("username", username)
                .ne("username", current.getUsername());

        if (isEnabled != null) {
            wrapper = wrapper.eq("is_enabled", isEnabled);
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

    @PutMapping
    public Map<String, Object> updateAccount(Account account, @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(2);

        //重新查询密码
        account.setPassword(null);
        account.clearOthers();

        boolean execute = true;
        if (account.notMatchEmail()) {
            map.put("msg", "邮箱格式错误");
            execute = false;
        }

        if (account.notMatchMobile()) {
            map.put("msg", "手机号格式错误");
            execute = false;
        }

        if (execute && accountService.update(account,
                new QueryWrapper<Account>()
                        .eq("id", account.getId())
                        .ne("username", current.getUsername()))
        ) {
            map.put("code", 0);
            map.put("msg", "修改成功");
        }

        return map;
    }

    @PutMapping("enable")
    public Map<String, Object> lockAccount(String id) {
        String[] ids = id.split(",");
        int del = 0;
        for (String sp : ids) {
            Account cur = accountService.getById(Integer.parseInt(sp));
            cur.setIsEnabled(!cur.getIsEnabled());
            if (accountService.updateById(cur)) {
                del++;
            }
        }
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "操作完成");
        map.put("count", del);
        map.put("total", ids.length);
        return map;
    }
}
