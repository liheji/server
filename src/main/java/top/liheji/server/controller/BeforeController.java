package top.liheji.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.liheji.server.constant.CaptchaTypeEnum;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.AccountService;
import top.liheji.server.service.CaptchaService;
import top.liheji.server.util.*;
import top.liheji.server.vo.ForgetVo;
import top.liheji.server.vo.RegisterVo;

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
        boolean isUpdate = accountService.updateById(account);

        return isUpdate ? R.ok() : R.error();
    }

    @PostMapping("register")
    public R register(@RequestBody RegisterVo register) {
        if (captchaService.checkCaptcha(null, register.getLicence())) {
            Account account = register.toAccount();
            boolean isSave = accountService.save(account);
            if (isSave) {
                return R.ok();
            }
        }

        return R.error();
    }

    @GetMapping("imageCaptcha")
    public R imgCaptcha() {
        return R.ok().put("data", captchaService.genImgCaptcha(null, CaptchaTypeEnum.GENERAL));
    }

    @GetMapping("sendCaptcha")
    public R sendCaptcha(String receiver, String property) {
        Account account = accountService.getOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, receiver)
                        .or()
                        .eq(Account::getEmail, receiver)
                        .or()
                        .eq(Account::getMobile, receiver)
        );
        CaptchaTypeEnum captchaType = CaptchaTypeEnum.from(property);
        if (account == null || captchaType == null) {
            throw new IllegalArgumentException("参数错误，请检查");
        }
        switch (captchaType) {
            case EMAIL: {
                captchaService.sendCaptcha("", receiver, CaptchaTypeEnum.EMAIL);
            }
            break;
            case MOBILE: {
                return R.error("暂不支持手机号验证");
                // 发送手机验证码
                //  captchaService.sendCaptcha("", receiver, CaptchaTypeEnum.MOBILE);
            }
            default:
                throw new IllegalArgumentException("参数错误，请检查");
        }

        return R.ok();
    }

    @GetMapping("uniqueCheck")
    public R uniqueCheck(String param) {
        long count = accountService.count(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getUsername, param)
                        .or()
                        .eq(Account::getMobile, param)
                        .or()
                        .eq(Account::getEmail, param)
        );

        return R.ok().put("result", count > 0);
    }
}
