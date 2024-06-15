package top.yilee.server.config.websocket.vo;

import lombok.Data;
import top.yilee.server.util.SshUtils;

import java.io.Serializable;

/**
 * @author : Galaxy
 * @time : 2023/2/11 23:19
 * @create : IdeaJ
 * @project : server
 * @description :
 */

@Data
public class SocketSessionVo implements Serializable {
    private static final long serialVersionUID = 6371213416354904721L;
    private String client;
    private SshUtils ssh;
}
