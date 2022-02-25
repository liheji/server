package top.liheji.server.controller;

import com.alibaba.fastjson.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import top.liheji.server.pojo.Account;
import top.liheji.server.pojo.FileAttr;
import top.liheji.server.service.FileAttrService;
import top.liheji.server.task.CloseDriverTask;
import top.liheji.server.task.DeleteFileTask;
import top.liheji.server.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @Time : 2021/12/30 15:38
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverVue
 * @Description : 不提供页面的统一放在这里
 */
@RestController
public class AfterController {

    @Autowired
    FileAttrService fileAttrService;

    @Autowired
    private JavaMailSender javaMailSender;

    public static final String[] IP_STR = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "Proxy-Client-IP",
            "Proxy-Client-IP",
            "X-Real-IP"
    };

    /**
     * 代理请求
     * 使用selenium代理客户端请求
     *
     * @param rM   请求方法
     * @param req  Request
     * @param resp Response
     * @throws Exception 异常
     */
    @GetMapping("proxy/**")
    public void proxy(@RequestParam(value = "r_m", required = false) String rM,
                      HttpServletRequest req,
                      HttpServletResponse resp) throws Exception {

        final String path = req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern = req.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        String url = "http://" + new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);

        WebDriver driver = DriverUtils.getInstance().getWebDriver();
        if ("post".equalsIgnoreCase(rM)) {
            URL obj = new URL(url);
            driver.get("http://" + obj.getHost());
            ((JavascriptExecutor) driver).executeScript(WebUtils.paramToPostJs(req));
        } else {
            url = String.format("%s?%s", url, WebUtils.paramToGetStr(req));
            driver.get(url);
        }

        new Timer().schedule(new CloseDriverTask(), 10 * 60 * 1000);

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
        String getStr = "";
        String ip = req.getHeader(IP_STR[0]);
        for (int i = 1; i < IP_STR.length; i++) {
            if (ip == null || "".equals(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getHeader(IP_STR[i]);
            } else {
                getStr = IP_STR[i - 1];
                break;
            }
        }

        if (ip == null || "".equals(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
            getStr = "Remote-Addr";
        } else if (ip.contains(",")) {
            ip = ip.split(",")[0];
        }

        Map<String, Object> map = WebUtils.getIpInfo(ip, query);
        map.put("ip", ip);
        map.put("from", getStr);

        return map;
    }

    @GetMapping("status")
    public Map<String, Object> status(@RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(3);

        map.put("code", 0);
        map.put("msg", "OK");
        map.put("data", CypherUtils.getBase64Str(JSONObject.toJSONBytes(current)));

        return map;
    }

    @GetMapping("secretCaptcha")
    public Map<String, Object> genSecret(@RequestAttribute("account") Account current,
                                         @RequestParam(required = false, defaultValue = "false") Boolean type) {
        Map<String, Object> map = new HashMap<>(4);

        map.put("code", 0);
        map.put("msg", "生成成功");
        map.put("key", SecretUtils.genSecret(current, type));

        return map;
    }

    @GetMapping("emailCaptcha")
    public Map<String, Object> bindCaptcha(String receiver, @RequestAttribute("account") Account current) {
        Map<String, Object> map = new HashMap<>(2);

        String code = SecretUtils.genCaptcha(current, 6);
        EmailUtils.sendCaptcha(javaMailSender, receiver, code);
        map.put("code", 0);
        map.put("msg", "发送成功");

        return map;
    }

    @PostMapping("format")
    @ResponseBody
    public Map<String, Object> uploadAndFormatFile(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(required = false, defaultValue = "") String software) throws Exception {
        Map<String, Object> map = new HashMap<>(4);
        // 文件存放服务端的位置
        FileAttr attr = FileUtils.uploadFile(file);
        File f = FileUtils.resourceFile("files", attr.getFileName());
        //处理excel文件
        map.put("code", 1);
        map.put("msg", "参数或格式错误(注意使用UTF-8编码)");

        File genFile = null;
        switch (software.trim()) {
            case "com.suda.yzune.wakeupschedule":
                genFile = HrbeuUtils.dealWakeupSchedule(f);
                if (genFile == null) {
                    map.put("msg", "仅支持xls,xlsx,html格式");
                } else {
                    map.put("code", 0);
                    map.put("msg", "格式化完成");
                    map.put("fileName", genFile.getName());
                }
                break;
            case "com.strivexj.timetable":
                genFile = HrbeuUtils.dealTimeTable(f);
                if (genFile == null) {
                    map.put("msg", "仅支持xls,xlsx,html格式");
                } else {
                    map.put("code", 0);
                    map.put("msg", "格式化完成");
                    map.put("fileName", genFile.getName());
                }
                break;
            default:
                break;
        }

        //删除源文件
        f.deleteOnExit();

        //定时删除生成的文件
        if (genFile != null) {
            new Timer().schedule(new DeleteFileTask(genFile.getName()), 10 * 60 * 1000);
        }

        return map;
    }
}
