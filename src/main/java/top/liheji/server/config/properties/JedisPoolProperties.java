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
@ConfigurationProperties(prefix = "jedis.pool")
@Data
public class JedisPoolProperties {
    /**
     * 连接池最大连接数（使用负值表示没有限制）
     */
    private int maxActive = 8;
    /**
     * 连接池中的最大空闲连接
     */
    private int maxIdle = 8;
    /**
     * 连接池中的最小空闲连接
     */
    private int minIdle = 0;
    /**
     * 连接池最大阻塞等待时间
     */
    private int maxWaitMillis = -1;
    /**
     * 是否在向连接池申请连接时，连接池会判断这条连接是否是可用的
     */
    private boolean testOnBorrow;
}
