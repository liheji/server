package top.liheji.server.config.websocket.vo;

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
public class SshServerVo implements Serializable {
    private static final long serialVersionUID = -1024254732983686117L;
    private Integer action;
    private String path;
}
