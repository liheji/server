package top.liheji.server.util;

import com.alibaba.fastjson.JSONObject;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.lang.Nullable;
import top.liheji.server.pojo.PersistentDevices;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2021/10/29 22:23
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 网络相关工具类
 */
public class WebUtils {
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
     * 获取请求IP地址
     *
     * @param req 请求
     * @return 返回信息
     */
    public static Map<String, Object> getIp(HttpServletRequest req) {
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

        Map<String, Object> map = new HashMap<>();
        map.put("ip", ip);
        map.put("from", getStr);

        return map;
    }


    /**
     * 获取请求IP地址
     *
     * @param ip    请求IP
     * @param query 查询IP
     * @return 返回信息
     */
    public static Map<String, Object> getIpInfo(String ip, @Nullable String query) {
        Map<String, Object> map = new HashMap<>(10);

        String url = "https://whois.pconline.com.cn/ipJson.jsp?level=3&json=true";

        map.put("code", 0);
        if (isRegularIp(query)) {
            url += "&ip=" + query.trim();
            map.put("msg", "查询成功");
            map.put("query", query.trim());
        } else {
            url += "&ip=" + ip.trim();
            map.put("msg", "查询请求IP");
            map.put("query", ip.trim());
        }

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();

            HashMap tmpMap = JSONObject.parseObject(response.body().string(), HashMap.class);

            map.put("addr", tmpMap.get("addr"));
            map.put("query", tmpMap.get("ip"));
        } catch (Exception e) {
            map.put("code", 1);
            map.put("msg", "查询错误");
        }

        return map;
    }

    /**
     * IP地址验证
     *
     * @param ip IP地址
     * @return IP地址是否正确
     */
    public static boolean isRegularIp(@Nullable String ip) {
        if (ip == null || "".equals(ip.trim())) {
            return false;
        }
        try {
            return InetAddress.getByName(ip.trim()) instanceof Inet4Address;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将POST数据转化为JS代码
     *
     * @param req 请求
     * @return 返回转化完成的字符串
     */
    public static String paramToPostJs(HttpServletRequest req) {
        StringBuilder ret = new StringBuilder();
        ret.append(String.format("const form = document.createElement('form');form.method = 'post';form.action = '%s';", req.getRequestURL().toString()));
        String paramStr = "const filed%d = document.createElement('input');filed%d.name = '%s';filed%d.value = '%s';form.appendChild(filed%d);";
        String back = "document.body.appendChild(form);form.submit();";

        int i = 0;
        for (Map.Entry<String, String[]> it : req.getParameterMap().entrySet()) {
            ret.append(String.format(paramStr, i, i, it.getKey(), i, it.getValue()[0], i));
            i++;
        }

        ret.append(back);
        return ret.toString();
    }

    /**
     * 将GET数据转化为JS代码
     *
     * @param req 请求
     * @return 返回转化完成的字符串
     */
    public static String paramToGetStr(HttpServletRequest req) {
        List<String> ret = new ArrayList<>();

        for (Map.Entry<String, String[]> it : req.getParameterMap().entrySet()) {
            ret.add(String.format("%s=%s", it.getKey(), it.getValue()[0]));
        }

        return String.join("&", ret);
    }

    /**
     * 解析UserAgent
     *
     * @param request HttpServletRequest
     */
    public static PersistentDevices parseAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        PersistentDevices ret = parseAgent(userAgent);
        if (ret != null) {
            Map<String, Object> ipInfo = getIp(request);
            ret.setIp((String) ipInfo.get("ip"));
        }
        return ret;
    }

    /**
     * 解析UserAgent
     *
     * @param agentStr useragentStr
     */
    private static PersistentDevices parseAgent(String agentStr) {
        try {
            UserAgent agent = UserAgent.parseUserAgentString(agentStr);
            Browser browser = agent.getBrowser();
            OperatingSystem operatingSystem = agent.getOperatingSystem();

            final String b = String.format("%s %s %s",
                    StringUtils.toWord(browser.getManufacturer().toString()),
                    browser.getName().replaceAll("\\d+|\\(.*?\\)", "").trim(),
                    agent.getBrowserVersion().getVersion());

            final String os = String.format("%s %s",
                    StringUtils.toWord(operatingSystem.getManufacturer().toString()),
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

            if (StringUtils.isEmpty(b) ||
                    StringUtils.isEmpty(os) ||
                    StringUtils.isEmpty(dt)) {
                return null;
            }
            return new PersistentDevices(dt, b, os);
        } catch (Exception err) {
            return null;
        }
    }
}
