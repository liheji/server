package top.yilee.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yilee.server.constant.CaptchaTypeEnum;
import top.yilee.server.pojo.Account;
import top.yilee.server.service.AccountService;
import top.yilee.server.service.CaptchaService;
import top.yilee.server.util.*;
import top.yilee.server.vo.ForgetVo;
import top.yilee.server.vo.RegisterVo;
import top.yilee.server.vo.SendCaptchaVo;

/**
 * @author : Galaxy
 * @time : 2022/2/4 10:38
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现登录后非实体相关接口
 */
@Slf4j
@RestController
@RequestMapping("/before")
public class BeforeController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("forget")
    public R forget(@RequestBody ForgetVo forget) {
        Account account = accountService.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, forget.getUsername())
                        .or()
                        .eq(Account::getEmail, forget.getUsername())
                        .or()
                        .eq(Account::getMobile, forget.getUsername())
        );
        if (account == null || !captchaService.checkCaptcha(null, forget.getKey())) {
            return R.error("校验失败，请检查");
        }
        account.setPassword(forget.bcrypt());
        accountService.updateById(account);

        return R.ok();
    }

    @PostMapping("register")
    public R register(@RequestBody RegisterVo register) {
        if (captchaService.checkCaptcha(null, register.getLicence())) {
            Account account = register.toAccount();
            accountService.save(account);
            return R.ok();
        }

        return R.error();
    }

    @GetMapping("imageCaptcha")
    public R imgCaptcha() {
        String captcha = captchaService.genImgCaptcha(null, CaptchaTypeEnum.GENERAL);
        return R.ok().put("data", captcha);
    }

    @GetMapping("sendCaptcha")
    public R sendCaptcha(SendCaptchaVo sendVo) {
        try {
            CaptchaTypeEnum typeEnum = sendVo.beforeSecurityCheck();
            captchaService.sendCaptcha(null, sendVo.getReceiver(), typeEnum);
            return R.ok();
        } catch (UnsupportedOperationException e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("accountUnique")
    public R accountUnique(String param) {
        long count = accountService.count(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, param)
                        .or()
                        .eq(Account::getMobile, param)
                        .or()
                        .eq(Account::getEmail, param)
        );
        return count > 0 ? R.ok() : R.error();
    }
}
