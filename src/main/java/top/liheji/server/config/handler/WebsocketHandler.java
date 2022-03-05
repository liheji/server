package top.liheji.server.config.handler;


import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import top.liheji.server.util.FileUtils;
import top.liheji.server.util.SshUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Galaxy
 * @time : 2022/1/10 10:29
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 设置 Websocket的实现类，处理socket传输的数据
 */
@Component
public class WebsocketHandler extends AbstractWebSocketHandler {
    public final Map<String, Map<String, Object>> clientMap = new ConcurrentHashMap<>();

    /**
     * 相当于注解 @OnOpen
     *
     * @param session WebSocketSession
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> obj = new ConcurrentHashMap<>(5);
        obj.put("client", session);
        clientMap.put(session.getId(), obj);
    }

    /**
     * 相当于注解 @OnClose
     *
     * @param session WebSocketSession
     * @param status  关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object tmp = clientMap.get(session.getId()).get("ssh");
        if (tmp != null) {
            ((SshUtils) tmp).close();
        }
        clientMap.remove(session.getId());
    }

    /**
     * 相当于注解 @OnError
     *
     * @param session   WebSocketSession
     * @param exception 异常
     * @throws Exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        clientMap.remove(session.getId());
        if (session.isOpen()) {
            session.close(CloseStatus.BAD_DATA.withReason("Messages transport error"));
        }
    }

    /**
     * 相当于注解 @OnMessage
     *
     * @param session WebSocketSession
     * @param message 文字信息
     * @throws Exception 异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("code", 0);
        Map<String, Object> objectMap = clientMap.get(session.getId());

        JSONObject obj = JSONObject.parseObject(message.getPayload());

        SshUtils ssh;
        switch (Action.values()[(Integer) obj.get("action")]) {
            case CONNECT:
                tmp.put("action", 0);
                tmp.put("msg", "连接成功");

                try {
                    ssh = new SshUtils((String) obj.get("username"), (String) obj.get("host"), (Integer) obj.get("port"));
                    if ("0".equals(obj.get("use"))) {
                        ssh.setPassword((String) obj.get("password"));
                    } else {
                        ssh.setAuthFile(FileUtils.resourceFile("files", (String) obj.get("authFile")).getAbsolutePath());
                    }
                    ssh.connect();
                    objectMap.put("ssh", ssh);
                } catch (Exception err) {
                    tmp.put("code", 1);
                    tmp.put("msg", err.toString());
                }

                session.sendMessage(new TextMessage(JSONObject.toJSONString(tmp)));
                break;
            case FILE_LIST:
                tmp.put("action", 1);
                tmp.put("msg", "获取成功");

                try {
                    ssh = (SshUtils) objectMap.get("ssh");
                    tmp.put("data", ssh.genInfoList((String) obj.get("path")));
                } catch (Exception err) {
                    tmp.put("code", 1);
                    tmp.put("msg", err.toString());
                }

                session.sendMessage(new TextMessage(JSONObject.toJSONString(tmp)));
                break;
            case SOCKET_TEST:
                tmp.put("action", 2);
                tmp.put("msg", "服务器已收到：" + obj.get("msg"));
                session.sendMessage(new TextMessage(JSONObject.toJSONString(tmp)));
                break;
            default:
                break;
        }
    }

    /**
     * 相当于注解 @OnMessage
     *
     * @param session WebSocketSession
     * @param message 二进制信息
     * @throws Exception 异常
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer s = message.getPayload();
        TextMessage textMessage = new TextMessage("Server has received your message.");
        session.sendMessage(textMessage);
    }

    enum Action {
        /**
         * Server连接
         */
        CONNECT,
        /**
         * 文件列表
         */
        FILE_LIST,
        /**
         * socket 测试
         */
        SOCKET_TEST
    }
}
