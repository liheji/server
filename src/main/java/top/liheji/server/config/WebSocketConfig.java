package top.liheji.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import top.liheji.server.config.handler.WebsocketHandler;
import top.liheji.server.config.intercept.WebSocketInterceptor;

/**
 * @Time : 2021/11/25 23:46
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : server
 * @Description :
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
