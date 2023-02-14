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
import top.liheji.server.service.AccountService;
import top.liheji.server.service.AuthAccountService;
import top.liheji.server.service.AuthPermissionService;
import top.liheji.server.service.CaptchaService;
import top.liheji.server.constant.CaptchaTypeEnum;
import top.liheji.server.pojo.*;
import top.liheji.server.util.*;
import top.liheji.server.vo.IpInfoVo;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Map;

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
    AccountService accountService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private AuthAccountService authAccountService;

    @Autowired
    AuthPermissionService authPermissionService;

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

    @GetMapping("status")
    public R status() {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        current.setPassword("");
        return R.ok().put("data", CypherUtils.encodeToBase64(JSON.toJSONBytes(current)));
    }

    @DeleteMapping("authAccount")
    public R deleteAuthAccount(@RequestBody Map<String, Object> param) {
        String sId = param.get("id").toString();
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        boolean isDelete = authAccountService.remove(
                new LambdaQueryWrapper<AuthAccount>()
                        .eq(AuthAccount::getAccountId, current.getId())
                        .eq(AuthAccount::getId, Long.parseLong(sId))
        );
        return isDelete ? R.ok() : R.error();
    }

    @GetMapping("sendCaptcha")
    public R sendCaptcha(String receiver, String property) throws Exception {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        CaptchaTypeEnum captchaType = CaptchaTypeEnum.from(property);
        if (ObjectUtils.isEmpty(current)
                || ObjectUtils.isEmpty(captchaType)
                || ObjectUtils.isEmpty(receiver)) {
            throw new IllegalArgumentException("参数错误，请检查");
        }
        switch (captchaType) {
            case EMAIL: {
                if (ObjectUtils.isEmpty(current.getEmail()) || receiver.equals(current.getEmail())) {
                    captchaService.sendCaptcha(current.getUsername(), receiver, CaptchaTypeEnum.EMAIL);
                } else {
                    throw new RuntimeException("验证失败");
                }
            }
            break;
            case MOBILE: {
                if (ObjectUtils.isEmpty(current.getMobile()) || receiver.equals(current.getMobile())) {
                    return R.error("暂不支持手机号验证");
                    // 发送手机验证码
                    //  captchaService.sendCaptcha(current.getUsername(), receiver, CaptchaTypeEnum.MOBILE);
                } else {
                    throw new RuntimeException("验证失败");
                }
            }
            default:
                throw new IllegalArgumentException("参数错误，请检查");
        }

        return R.ok();
    }

    @GetMapping("socketCaptcha")
    @PreAuthorize("hasAuthority('use_web_socket')")
    public R genWebSocketCaptcha() {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        return R.ok().put("key", captchaService.genCaptcha(current.getUsername(), CaptchaTypeEnum.GENERAL_SECRET));
    }

    @GetMapping("registerCaptcha")
    @PreAuthorize("hasAuthority('add_account')")
    public R genRegisterCaptcha() throws Exception {
        Account current = ServerConstant.LOCAL_ACCOUNT.get();
        if (current == null) {
            throw new RuntimeException("请先登录");
        }
        return R.ok().put("key", captchaService.genCaptcha(null, CaptchaTypeEnum.REGISTER_SECRET));
    }

    @PostMapping("format")
    @ResponseBody
    @PreAuthorize("hasAuthority('use_format')")
    public R uploadAndFormatFile(@RequestParam("file") MultipartFile file) throws Exception {
        @Cleanup InputStream in = file.getInputStream();

        File genFile = HrbeuUtils.dealWakeupSchedule(in, file.getOriginalFilename());
        if (genFile == null) {
            throw new NotSupportedException("仅支持xls,xlsx,html格式");
        }

        return R.ok().put("fileName", genFile.getName());
    }
}
