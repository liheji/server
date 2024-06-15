package top.yilee.server.config.websocket.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Galaxy
 * @time : 2023/2/11 23:13
 * @create : IdeaJ
 * @project : server
 * @description :
 */
@Data
public class SshConnectVo implements Serializable {
    private static final long serialVersionUID = 5574182046261081599L;
    private Integer action;
    private String username;
    private String host;
    private Integer port;
    /**
     * 连接方式
     */
    private Integer use;

    private String authFile;
    private String password;
}
