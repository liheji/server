package top.liheji.server.util;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.Cleanup;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import top.liheji.server.pojo.AuthDevices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:23
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 网络相关工具类
 */
public class WebUtils {
    /**
     * 尝试获取真实IP的头列表
     */
    public static final String[] IP_FROM = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    };

    /**
     * 获取请求IP地址
     *
     * @param req 请求
     * @return 返回信息
     */
    @Nullable
    public static String parseIp(HttpServletRequest req) {
        String ip = req.getHeader(IP_FROM[0]);
        for (String str : IP_FROM) {
            if (!ObjectUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                break;
            }
            ip = req.getHeader(str);
        }
        if (ObjectUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }

        if (ip.contains(",")) {
            ip = ip.split(",")[0];
        }

        return ip;
    }

    /**
     * 解析UserAgent
     *
     * @param request HttpServletRequest
     */
    public static AuthDevices parseAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        AuthDevices ret = parseAgent(userAgent);
        if (ret != null) {
            ret.setIp(parseIp(request));
        }
        return ret;
    }

    /**
     * 解析UserAgent
     *
     * @param agentStr useragentStr
     */
    private static AuthDevices parseAgent(String agentStr) {
        try {
            UserAgent agent = UserAgent.parseUserAgentString(agentStr);
            Browser browser = agent.getBrowser();
            OperatingSystem operatingSystem = agent.getOperatingSystem();

            final String b = String.format("%s %s %s",
                    toWord(browser.getManufacturer().toString()),
                    browser.getName().replaceAll("\\d+|\\(.*?\\)", "").trim(),
                    agent.getBrowserVersion().getVersion());

            final String os = String.format("%s %s",
                    toWord(operatingSystem.getManufacturer().toString()),
                    operatingSystem.getName());

            String dt;

            DeviceType deviceType = operatingSystem.getDeviceType();
            switch (deviceType) {
                case TABLET:
                case MOBILE:
                case COMPUTER:
                    dt = deviceType.getName();
                    break;
                default:
                    dt = "Other";
                    break;
            }

            if (ObjectUtils.isEmpty(b) ||
                    ObjectUtils.isEmpty(os) ||
                    ObjectUtils.isEmpty(dt)) {
                return null;
            }
            return new AuthDevices(dt, b, os);
        } catch (Exception err) {
            return null;
        }
    }

    private static String toWord(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * 返回JSON数据
     *
     * @param r    响应数据
     * @param resp 响应
     * @throws IOException IO异常
     */
    public static void response(HttpServletResponse resp, R r) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.write(r.toJSON());
        out.flush();
        out.close();
    }

    /**
     * 返回text/html数据
     *
     * @param resp   响应
     * @param msg    信息
     * @param toMain 是否去主页
     * @throws IOException IO异常
     */
    public static void response(HttpServletResponse resp, String msg, boolean toMain) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.write(oauth2Html(msg, toMain));
        out.flush();
        out.close();
    }

    /**
     * 拼接返回的HTML
     *
     * @param msg    信息
     * @param toMain 是否去主页
     * @return HTML文本
     */
    private static String oauth2Html(String msg, boolean toMain) {
        final String path = toMain ? "/#/main/personal" : "/#/login?msg=" + msg;
        StringBuilder builder = new StringBuilder();
        try {
            File oauth2 = FileUtils.resourceFile("templates", "oauth2.html");
            @Cleanup FileInputStream fis = new FileInputStream(oauth2);
            @Cleanup InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            @Cleanup BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception ignored) {
        }
        return String.format(builder.toString(), path);
    }
}
