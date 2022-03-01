package top.liheji.server.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Time : 2022/2/28 17:56
 * @Author : Galaxy
 * @Create : IdeaJ
 * @Project : serverPlus
 * @Description :
 */
@Component
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "jedis")
@Data
public class JedisProperties {
    /**
     * Redis服务器地址
     */
    private String host = "localhost";
    /**
     * Redis服务器连接端口
     */
    private int port = 6379;
    /**
     * Redis服务器连接超时时间
     */
    private int timeout = 300000;
    /**
     * Redis服务器连接密码（默认为空）
     */
    private String password = null;
    /**
     * Redis数据库
     */
    private int database;
    /**
     * 客户端名称
     */
    private String clientName = null;

    /**
     * 客户端名称
     */
    private boolean ssl;
}
