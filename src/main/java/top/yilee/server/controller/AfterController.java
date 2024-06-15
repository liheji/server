package top.yilee.server.controller;

import cn.hutool.core.codec.Base64;
import com.aspose.slides.exceptions.NotSupportedException;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.scheduled.DeleteWakeUpFileTask;
import top.yilee.server.service.CaptchaService;
import top.yilee.server.constant.CaptchaTypeEnum;
import top.yilee.server.pojo.*;
import top.yilee.server.util.*;
import top.yilee.server.vo.SendCaptchaVo;

import java.io.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : Galaxy
 * @time : 2021/12/30 15:38
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 实现用户登录前访问相关接口
 */
@RestController
public class AfterController {
    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private ScheduledThreadPoolExecutor threadPoolExecutor;

    /**
     * 获取当前登录状态
     *
     * @return 状态信息
     */
    @GetMapping("status")
    public R status() {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        current.setPassword("");

        return R.ok().put("data", Base64.encode(JsonUtils.toJSONBytes(current)));
    }

    @GetMapping("sendCaptcha")
    public R sendCaptcha(SendCaptchaVo sendVo) {
        try {
            Account current = ServerConstant.LOCAL_ACCOUNT.get();
            CaptchaTypeEnum typeEnum = sendVo.afterSecurityCheck();
            captchaService.sendCaptcha(current.getUsername(), sendVo.getReceiver(), typeEnum);
            return R.ok();
        } catch (UnsupportedOperationException e) {
            // 手机号验证暂不支持
            return R.error(e.getMessage());
        }
    }

    @GetMapping("secretCaptcha")
    public R secretCaptcha(String code) {
        CaptchaTypeEnum typeEnum = CaptchaTypeEnum.from(code);
        if (ObjectUtils.isEmpty(typeEnum)) {
            return R.error("验证码类型错误");
        }
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        String captcha;
        switch (typeEnum) {
            case GENERAL:
            case GENERAL_SECRET:
                captcha = captchaService.genCaptcha(current.getUsername(), typeEnum);
                break;
            case REGISTER_SECRET:
                captcha = captchaService.genCaptcha(null, typeEnum);
                break;
            default:
                throw new UnsupportedOperationException("验证码类型错误");
        }
        return R.ok().put("key", captcha);
    }

    @PostMapping("wakeup")
    @ResponseBody
    @PreAuthorize("hasAuthority('use_wakeup')")
    public R uploadAndFormatFile(@RequestParam("file") MultipartFile file) throws Exception {
        @Cleanup InputStream in = file.getInputStream();

        File genFile = HrbeuUtils.dealWakeupSchedule(in, file.getOriginalFilename());
        if (genFile == null) {
            throw new NotSupportedException("仅支持xls,xlsx,html格式");
        }

        threadPoolExecutor.schedule(new DeleteWakeUpFileTask(genFile), 10, TimeUnit.MINUTES);

        return R.ok().put("fileName", genFile.getName());
    }
}
