package top.liheji.server.controller;

import com.alibaba.fastjson2.JSON;
import com.aspose.slides.exceptions.NotSupportedException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.liheji.server.constant.ServerConstant;
import top.liheji.server.scheduled.DeleteWakeUpFileTask;
import top.liheji.server.service.AuthAccountService;
import top.liheji.server.service.CaptchaService;
import top.liheji.server.constant.CaptchaTypeEnum;
import top.liheji.server.pojo.*;
import top.liheji.server.util.*;
import top.liheji.server.vo.IpInfoVo;
import top.liheji.server.vo.SendCaptchaVo;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Map;
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
    private AuthAccountService authAccountService;

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
        return R.ok().put("data", CypherUtils.encodeToBase64(JSON.toJSONBytes(current)));
    }

    /**
     * 格式化IP
     *
     * @param query 查询IP
     * @param req   Request
     * @return 查询结果
     */
    @GetMapping("ip")
    public R ip(@RequestParam(required = false) String query, HttpServletRequest req) {
        String ip = WebUtils.parseIp(req);
        IpInfoVo ipInfo = WebUtils.getIpInfo(ip, query);
        if (ipInfo != null) {
            return R.ok().put("data", ipInfo);
        }
        return R.error();
    }

    @DeleteMapping("authAccount")
    public R deleteAuthAccount(@RequestBody Map<String, Object> param) {
        String sId = param.get("id").toString();
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        authAccountService.remove(
                new LambdaQueryWrapper<AuthAccount>()
                        .eq(AuthAccount::getAccountId, current.getId())
                        .eq(AuthAccount::getId, Long.parseLong(sId))
        );
        return R.ok();
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
