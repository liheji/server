package top.yilee.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import top.yilee.server.config.websocket.handler.WebsocketHandler;
import top.yilee.server.config.websocket.interceptor.WebSocketInterceptor;

/**
 * @author : Galaxy
 * @time : 2021/11/25 23:46
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 设置 WebSocket 相关配置
 * 设置连接URL，并允许跨域访问
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebsocketHandler websocketHandler;

    @Autowired
    private WebSocketInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketHandler, "/websocket/{tid}")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");

        log.info("WebSocket拦截器注册成功");
    }
}
