package top.liheji.server.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.Cleanup;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.AuthGroup;
import top.liheji.server.pojo.AuthPermission;
import top.liheji.server.service.*;
import top.liheji.server.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

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
    FileAttrService fileAttrService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 代理请求
     * 使用selenium代理客户端请求
     *
     * @param reqMethod 请求方法
     * @param req       Request
     * @param resp      Response
     * @throws Exception 异常
     */
    @GetMapping("proxy/**")
    public void proxy(@RequestParam(value = "req_method", required = false) String reqMethod,
                      HttpServletRequest req,
                      HttpServletResponse resp) throws Exception {

        final String path = req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern = req.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        String url = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);
        @Cleanup("timeClose") DriverUtils driverUtils = DriverUtils.getInstance();

        WebDriver driver = driverUtils.getWebDriver();
        if ("post".equalsIgnoreCase(reqMethod)) {
            URI uri = new URI(url);
            driver.get(uri.getScheme() + "://" + uri.getAuthority());
            ((JavascriptExecutor) driver).executeScript(WebUtils.paramToPostJs(req));
        } else {
            url = String.format("%s?%s", url, WebUtils.paramToGetStr(req));
            driver.get(url);
        }

        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/html");
        resp.getWriter().write(driver.getPageSource());
    }

    /**
     * 格式化IP
     *
     * @param query 查询IP
     * @param req   Request
     * @return 查询结果
     */
    @GetMapping("ip")
    public Map<String, Object> ip(@RequestParam(required = false) String query, HttpServletRequest req) {
        Map<String, Object> map = WebUtils.getIp(req);
        map.putAll(WebUtils.getIpInfo((String) map.get("ip"), query));
        return map;
    }

    @PostMapping("discern")
    public Map<String, Object> discern(String frontImg, String bgImg, String discernType) throws IOException {
        File frontIn = FileUtils.base64SaveToFile(frontImg);
        File bgIn = FileUtils.base64SaveToFile(bgImg);
        Map<String, Object> map = new HashMap<>(2);
        map.put("code", 0);
        map.put("msg", "识别成功");
        SlideUtils side = SlideUtils.getInstance();
        switch (discernType) {
            case "slide":
                map.put("data", side.discernSlideImg(frontIn.getPath(), bgIn.getPath()));
                break;
            case "gap":
                map.put("data", side.discernGapImg(frontIn.getPath(), bgIn.getPath()));
                break;
            default:
                map.put("code", 1);
                map.put("msg", "不支持的类型");
                break;
        }

        frontIn.deleteOnExit();
        bgIn.deleteOnExit();

        return map;
    }

    @GetMapping("status")
    public Map<String, Object> status(@RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(3);

        map.put("code", 0);
        map.put("msg", "OK");
        map.put("data", CypherUtils.encodeToBase64(JSONObject.toJSONBytes(current)));
        return map;
    }

    @GetMapping("permissions")
    public Map<String, Object> queryPermissions(@RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(3);

        map.put("code", 0);
        map.put("msg", "OK");

        List<AuthGroup> groups = null;
        List<AuthPermission> permissions = null;
        if (!current.getIsSuperuser()) {
            groups = current.getAuthGroups();
            permissions = current.getAuthPermissions();
        }

        if (groups == null) {
            groups = new ArrayList<>();
        }
        if (permissions == null) {
            permissions = new ArrayList<>();
        }

        Set<Object> auths = new HashSet<>();
        for (AuthGroup group : groups) {
            List<AuthPermission> permissions0 = group.getAuthPermissions();
            if (permissions0 != null) {
                for (AuthPermission permission : permissions0) {
                    auths.add(permission.getCodename());
                }
            }
        }
        for (AuthPermission permission : permissions) {
            auths.add(permission.getCodename());
        }

        map.put("data", auths);

        return map;
    }

    @GetMapping("emailCaptcha")
    public Map<String, Object> bindCaptcha(String receiver, @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("code", 1);
        map.put("msg", "用户不存在");

        if (current != null) {
            captchaService.sendEmailCaptcha(receiver);
            map.put("code", 0);
            map.put("msg", "发送成功");
        }

        return map;
    }

    @GetMapping("webSocketCaptcha")
    @PreAuthorize("hasAuthority('use_web_socket')")
    public Map<String, Object> genWebSocketCaptcha(@RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(3);

        map.put("code", 0);
        map.put("msg", "密钥生成成功");
        map.put("key", captchaService.genSecret(current, 5 * 60));

        return map;
    }

    @GetMapping("registerCaptcha")
    @PreAuthorize("hasAuthority('add_account')")
    public Map<String, Object> genRegisterCaptcha(@RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(3);

        map.put("code", 0);
        map.put("msg", "注册码生成成功");
        map.put("key", captchaService.genSecret(current, 3 * 24 * 60 * 60));

        return map;
    }

    @PostMapping("format")
    @ResponseBody
    @PreAuthorize("hasAuthority('use_format')")
    public Map<String, Object> uploadAndFormatFile(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Object> map = new HashMap<>(4);
        //处理excel文件
        map.put("code", 1);
        map.put("msg", "参数或格式错误(注意使用UTF-8编码)");

        @Cleanup InputStream in = file.getInputStream();

        File genFile = HrbeuUtils.dealWakeupSchedule(in, file.getOriginalFilename());
        if (genFile == null) {
            map.put("msg", "仅支持xls,xlsx,html格式");
        } else {
            map.put("code", 0);
            map.put("msg", "格式化完成");
            map.put("fileName", genFile.getName());
        }

        return map;
    }
}
