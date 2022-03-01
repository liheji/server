package top.liheji.server.config.intercept;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import top.liheji.server.pojo.Account;
import top.liheji.server.service.CaptchaService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Time : 2021/11/25 23:46
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : server
 * @Description :
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
            final Map<String, String> pathVar = (Map<String, String>) req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String tid = pathVar.get("tid");
            Account current = (Account) req.getAttribute("account");
            //这里做一个简单的鉴权，只有符合条件的鉴权才能握手成功
            return captchaService.checkSecret(current, tid);
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}