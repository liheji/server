package top.liheji.server.config.websocket.handler;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import top.liheji.server.config.websocket.vo.SocketSessionVo;
import top.liheji.server.config.websocket.vo.SshConnectVo;
import top.liheji.server.config.websocket.vo.SshServerVo;
import top.liheji.server.util.FileUtils;
import top.liheji.server.util.R;
import top.liheji.server.util.SshUtils;

import java.io.IOException;
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
    public final Map<String, SocketSessionVo> clientMap = new ConcurrentHashMap<>();

    /**
     * 相当于注解 @OnOpen
     *
     * @param session WebSocketSession
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SocketSessionVo sessionVo = new SocketSessionVo();
        sessionVo.setClient(session.getId());
        clientMap.put(session.getId(), sessionVo);
    }

    /**
     * 相当于注解 @OnClose
     *
     * @param session WebSocketSession
     * @param status  关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SshUtils ssh = clientMap.get(session.getId()).getSsh();
        if (ssh != null) {
            ssh.close();
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
        SocketSessionVo sessionVo = clientMap.get(session.getId());

        String payload = message.getPayload();
        JSONObject obj = JSONObject.parseObject(payload);
        Action action = Action.from(obj.get("action"));
        if (action == null) {
            session.sendMessage(new TextMessage(R.error("无效的操作").toJSON()));
            return;
        }

        R r = R.ok().put("action", action.getCode());
        switch (action) {
            case CONNECT:
                try {
                    SshConnectVo connect = JSON.parseObject(payload, SshConnectVo.class);
                    SshUtils ssh = new SshUtils(connect.getUsername(), connect.getHost(), connect.getPort());
                    if (connect.getUse() == 0) {
                        ssh.setPassword(connect.getPassword());
                    } else {
                        ssh.setAuthFile(FileUtils.staticFile("uploads", connect.getAuthFile()).getAbsolutePath());
                    }
                    ssh.connect();
                    sessionVo.setSsh(ssh);
                } catch (Exception err) {
                    err.printStackTrace();
                    r = R.error(err.getMessage()).put("action", action.getCode());
                }
                break;
            case FILE_LIST:
                try {
                    SshUtils ssh = sessionVo.getSsh();
                    SshServerVo serverVo = JSON.parseObject(payload, SshServerVo.class);
                    r.put("data", ssh.genInfoList(serverVo.getPath()));
                } catch (Exception err) {
                    err.printStackTrace();
                    r = R.error(err.getMessage()).put("action", action.getCode());
                }
                break;
            case FILE_VIEW:
                try {
                    SshUtils ssh = sessionVo.getSsh();
                    SshServerVo serverVo = JSON.parseObject(payload, SshServerVo.class);
                    String str = ssh.view(serverVo.getPath());
                    if (str == null) {
                        r = R.error("文件不支持预览").put("action", action.getCode());
                        break;
                    }
                    r.put("data", str);
                } catch (Exception err) {
                    err.printStackTrace();
                    r = R.error(err.getMessage()).put("action", action.getCode());
                }
                break;
            case SOCKET_TEST:
                r.put("msg", "服务器已收到：" + obj.get("msg"));
                break;
            default:
                break;
        }
        session.sendMessage(new TextMessage(r.toJSON()));
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
        TextMessage textMessage = new TextMessage("Server has received your message.");
        session.sendMessage(textMessage);
    }

    @Getter
    enum Action {
        /**
         * Server连接
         */
        CONNECT(0, "连接请求"),
        /**
         * 文件列表
         */
        FILE_LIST(1, "列出文件信息"),
        /**
         * socket 测试
         */
        SOCKET_TEST(2, "socket 测试"),
        /**
         * 文件预览
         */
        FILE_VIEW(3, "文件预览");

        final Integer code;
        final String msg;

        Action(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public static Action from(Object code) {
            for (Action action : Action.values()) {
                if (action.code.equals(code)) {
                    return action;
                }
            }
            return null;
        }
    }
}
