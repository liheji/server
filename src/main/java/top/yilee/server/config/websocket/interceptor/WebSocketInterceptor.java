package top.yilee.server.config.websocket.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import top.yilee.server.constant.ServerConstant;
import top.yilee.server.pojo.Account;
import top.yilee.server.service.CaptchaService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author : Galaxy
 * @time : 2021/11/25 23:46
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 设置 Websocket的拦截器，进行连接认证
 */
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {
    @Autowired
    private CaptchaService captchaService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //拦截器 -> 握手开始之前
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
            @SuppressWarnings("unchecked") final Map<String, String> pathVar = (Map<String, String>) req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String tid = pathVar.get("tid");
            Account current = ServerConstant.LOCAL_ACCOUNT.get();
            //这里做一个简单的鉴权，只有符合条件的鉴权才能握手成功
            return captchaService.checkCaptcha(current.getUsername(), tid);
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}