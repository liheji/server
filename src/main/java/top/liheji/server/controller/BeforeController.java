package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.AccountService;
import top.liheji.server.service.CaptchaService;

import java.util.HashMap;
import java.util.Map;

/**
 * @Time : 2022/2/4 10:38
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverPlus
 * @Description :
 */
@RestController
@RequestMapping("/before")
public class BeforeController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("forget")
    public Map<String, Object> forget(String email, String key, String password) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "用户不存在");

        Account account = accountService.getOne(new QueryWrapper<Account>().eq("email", email));
        if (account != null) {
            if (captchaService.checkCaptcha(key)) {
                account.setPassword(password);
                account.bcryptPassword();
                if (accountService.saveOrUpdate(account)) {
                    map.put("code", 0);
                    map.put("msg", "OK");
                }
            } else {
                map.put("code", 1);
                map.put("msg", "校验码错误");
            }
        }
        return map;
    }


    @PostMapping("register")
    public Map<String, Object> register(Account account, String licence) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "授权码错误");
        if (captchaService.checkSecret(null, licence)) {
            account.bcryptPassword();
            if (accountService.save(account)) {
                map.put("code", 0);
                map.put("msg", "注册成功");
            } else {
                map.put("code", 1);
                map.put("msg", "系统错误");
            }
        }

        return map;
    }

    @GetMapping("imageCaptcha")
    public Map<String, Object> imgCaptcha(@RequestParam(required = false, defaultValue = "100") Integer width,
                                          @RequestParam(required = false, defaultValue = "38") Integer height) throws Exception {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "获取成功");
        map.put("data", captchaService.genImgCaptcha(4, width, height));

        return map;
    }

    @GetMapping("emailCaptcha")
    public Map<String, Object> emailCaptcha(String receiver) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "用户不存在");

        Account account = accountService.getOne(new QueryWrapper<Account>().eq("email", receiver));
        if (account != null) {
            captchaService.sendEmailCaptcha(receiver);
            map.put("code", 0);
            map.put("msg", "发送成功");
        }

        return map;
    }

    @GetMapping("usernameCheck")
    public Map<String, Object> usernameCheck(String username) {
        long count = accountService.count(
                new QueryWrapper<Account>()
                        .eq("username", username)
                        .or()
                        .eq("email", username)
        );
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 0);
        map.put("msg", "OK");
        map.put("result", count > 0);
        return map;
    }
}
